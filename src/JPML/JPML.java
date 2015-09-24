package JPML;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;


public class JPML {
	// 人
	private static class person implements Comparable<person>{
		// 索引標籤
		private static HashMap<String,Integer> dataIndex = new HashMap<>();
		// 仍未建立的點
		boolean unconstructed = false;
		// 是否是始祖?
		boolean ancestor = false;
		// 是否衝突(兩人共享同code)
		boolean conflict = false;
		// 祖先是否衝突
		boolean ancientConflict = false;
		// 若衝突是否已經輸出至檔案?
		boolean output = false;

		int personid;
		private Row data;
		ArrayList<wife> wifes = new ArrayList<>();
		ArrayList<person> sons = new ArrayList<>();
		ArrayList<person> conflict_persons = new ArrayList<>();
		ArrayList<person> stepson = new ArrayList<>();
		ArrayList<person> waixing = new ArrayList<>();
		person father;
		@Override
		public int compareTo(person other) {
			// TODO Auto-generated method stub
			return this.personid - other.personid;
		}
		public String get(String input){
			try{
			if(dataIndex.containsKey(input))
				return data.getCell(dataIndex.get(input)).toString();
			}
			catch(Exception e){
				
			}
			return "";
		}
		// constructor
		public person(Row r){
			this.personid = personID++;
			this.data = r;
			if(this.get("an")=="y" || this.get("an").compareTo("y")==0)
				this.ancestor=true;
		}
		public person(){
			this.personid = personID++;
			this.unconstructed = true;
		}
		public String getFatherCode() {
			String input = this.get("CODE");
			boolean t = false;
			int i = 0;
			for (i = input.length() - 1; i >= 0; i--) {
				if (t && isDigit(input.charAt(i))) {
					break;
				}
				if (!t && isDigit(input.charAt(i)))
					continue;
				t = true;
			}
			return input.substring(0, i + 1);
		}
		public static boolean isDigit(char c) {
			if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8'
					|| c == '9')
				return true;
			return false;
		}
	}

	// 老婆
	private static class wife extends person{

		private wife(Row r,person h,boolean b) {
			super(r);
			husband = h;
		}
		public static wife getWife(Row r,person h){
			if(h.get("WIFE")=="" || h.get("WIFE")==null)
				return null;
			return new wife(r,h,true);
		}
		person husband;
	}
	
	// 流水編號, 由1開始
	static int personID=1;
	
	// 由上到下標記衝突
	public static void markAncientConflictRecursively(person p) {
		p.ancientConflict = true;
		for (person s : p.sons)
			if (!s.ancientConflict)
				markAncientConflictRecursively(s);
	}

	// 建立個人的JPML資料
	private static String addPerson(person p, boolean ancestor) {
		boolean noname = false;
		boolean nofather = false;
		if(p.father == null || p.father.unconstructed)
			nofather = true;
		if (p.get("NAME") == "" || p.get("NAME") == null || p.get("NAME").length() == 0)
			noname = true;
		// TODO book編號暫定為441
		String r="";
		r += "<person personID=\"sysspBook441_person_" + p.personid + "\">";
		// 父輩的為是
		r += "<admitted>" + "是" + "</admitted>";
		// 嘗試算出年紀 無法則不計
		try{
			r += "<age>" + (Integer.valueOf(p.get("RDEA"))-Integer.valueOf(p.get("RBIR"))) + "</age>" + "<branch/>" + "<description/>";
		}
		catch(Exception e){
			r += "<age>" + "</age>" + "<branch/>" + "<description></description>";
		}
		r += "<familyName>" + "闕" + "</familyName>";
		r += "<gender>" + "男" + "</gender>";
		// TODO 世代 號 諱
		r += "<generation>" + p.get("ERA") + "</generation>" + "<hao colLabel=\"alias\">" + p.get("hao") + "</hao>"
				+ "<hui colLabel=\"alias\">" + p.get("hui") + "</hui>"
				// 地區
				+ "<location/>";
		// 0為父系人物, 1為父系人物之配偶
		r += "<mateFlag>" + 0 + "</mateFlag>";
		// 無名標籤, 若無名, 設定為1且名字為"(無名)"
		if (noname)
			r += "<nonameFlag>" + 1 + "</nonameFlag>";
		else
			r += "<nonameFlag>" + 0 + "</nonameFlag>";
		// note 不知道是啥
		r += "<note>" + "</note>";
		// ming
		r += "<otherAlias colLabel=\"alias\">" + p.get("ming") + "</otherAlias>" + "<page/>";
		if (noname)
			r += "<personName>" + "(無名)" + "</personName>";
		else
			r += "<personName>" + p.get("NAME") + "</personName>";
		// TODO 1為始祖, 2為吳父親資訊, 0其他
		if (p.ancestor)
			r += "<rootFlag>" + 1 + "</rootFlag>";
		else if (nofather)
			r += "<rootFlag>" + 0 + "</rootFlag>";
		else
			r += "<rootFlag>" + 2 + "</rootFlag>";
		// shi 不知道是啥
		r += "<shi colLabel=\"alias\">" + "</shi>";
		// 暫時不管排行
		r += "<siblingOrder/>"
		// sourceNote不知道啥
		+ "<sourceNote/>" 
		// 冊數 暫定為1
		+ "<volume>" + 1 + "</volume>" 
		// 字
		+ "<zi colLabel=\"alias\">" + p.get("zi") + "</zi>"
		// 兒子數量
		+ "<book441_numson>" + p.sons.size() + "</book441_numson>"
		// 乳名
		+ "<book441_rumin colLabel=\"alias\">" + p.get("rumin") + "</book441_rumin>"
		// newedu 意義不明
		+"<book441_newedu/>" 
		// 文武 暫不考慮
		+ "<book441_wenwu/>"
		// 教育 暫不考慮
		+ "<book441_educ/>" 
		// identity 意義不明
		+ "<book441_identity/>" ;
		// 女兒數量 => 把老婆的女兒都加起來
		int dau = 0;
		for(wife w : p.wifes){
			try{
				dau += Integer.valueOf(w.get("dau"));
			}
			catch(Exception e){}
		}
		r += "<book441_numdau>" + dau + "</book441_numdau>"
		// TODO 夭折
		+ "<book441_yaozhe>" + p.get("YAOZHE")  + "</book441_yaozhe>"
		// 生日
		+ "<event type=\"BIRTH\">" 
			+ "<time>" 
				+ "<year>" + p.get("RBIR") + "</year>"
				+ "<dynasty_code>" + 0 + "</dynasty_code>" 
				+ "<nh_code>" + 0 + "</nh_code>" 
				+ "<nh_year/>" 
				+ "<calendar>" + "國" + "</calendar>"
				+ "<intercalary/>" 
				+ "<month>" + p.get("RBM") + "</month>" 
				+ "<day/>" 
				+ "<hr/>" 
				+ "<ganzhi/>" 
				+ "<time_else/>" 
			+ "</time>"
			+ "<description/>" 
		+ "</event>" 
		// 死亡日	
		+ "<event type=\"DEATH\">" 
			+ "<time>" 
				+ "<year>" + p.get("RDEA") + "</year>"
				+ "<dynasty_code>" + 0 + "</dynasty_code>" 
				+ "<nh_code>" + 0 + "</nh_code>" 
				+ "<nh_year/>" 
				+ "<calendar>" + "國" + "</calendar>"
				+ "<intercalary/>" 
				+ "<month>" + p.get("RDM") + "</month>"
				+ "<day/>" 
				+ "<hr/>" 
				+ "<ganzhi/>" 
				+ "<time_else/>" 
			+ "</time>" + "<description/>"
		+ "</event>" 
		// 關係
		+ "<relations>";
		// 加入老爸資訊
		if (!nofather) {
			r += "<relative ID=\"sysspBook441_person_" + p.father.personid ;
			r += "\" kinrel=\"F\">闕" + p.father.get("NAME")
					+ "</relative>";
		}

		// 加入老婆資訊
		for (int i = 0; i < p.wifes.size(); i++)
			r += "<relative ID=\"sysspbook441_person_" + p.wifes.get(i).personid + "\" kinrel=\"W\">"
					+ p.wifes.get(i).get("WIFE") + "(無名)" + "</relative>";
		// 加入兒子資訊
		for (int i = 0; i < p.sons.size(); i++)
			r += "<is_a_relative_of ID=\"sysspbook441_person_" + p.sons.get(i).personid + "\" kinrel=\"F\">" + "闕"
					+ p.sons.get(i).get("NAME") + "</is_a_relative_of>";

		r += "</relations>" + "</person>";
		return r;
	}
	
	// 建立老婆的JPML資料
	private static String addWife(wife p){
		// TODO book編號暫定為441
		String r="";
		r += "<person personID=\"sysspBook441_person_" + p.personid + "\">";
		r += "<admitted>" + "否" + "</admitted>";
		// 嘗試算出年紀 無法則不計
		try{
			r += "<age>" + (Integer.valueOf(p.get("RDEA"))-Integer.valueOf(p.get("RBIR"))) + "</age>" + "<branch/>" + "<description/>";
		}
		catch(Exception e){
			r += "<age>" + "</age>" + "<branch/>" + "<description></description>";
		}
		r += "<familyName>" + p.get("WIFE") + "</familyName>";
		r += "<gender>" + "女" + "</gender>";
		// TODO 世代 號 諱
		r += "<generation>"  + "</generation>" + "<hao colLabel=\"alias\">"  + "</hao>"
				+ "<hui colLabel=\"alias\">"  + "</hui>"
				// 地區
				+ "<location/>";
		// 0為父系人物, 1為父系人物之配偶
		r += "<mateFlag>" + 1 + "</mateFlag>";
		// 無名標籤, 若無名, 設定為1且名字為"(無名)"
		r += "<nonameFlag>" + 1 + "</nonameFlag>";
		// note 不知道是啥
		r += "<note>" + "</note>";
		// ming
		r += "<otherAlias colLabel=\"alias\">" + "</otherAlias>" + "<page/>";
		r += "<personName>" + "(無名)" + "</personName>";
		// TODO 1為始祖, 2為吳父親資訊, 0其他
		r += "<rootFlag>" + 0 + "</rootFlag>";
		// shi 不知道是啥
		r += "<shi colLabel=\"alias\">" + "</shi>";
		// 暫時不管排行
		r += "<siblingOrder/>"
		// sourceNote不知道啥
		+ "<sourceNote/>" 
		// 冊數 暫定為1
		+ "<volume>" + 1 + "</volume>" 
		// 字
		+ "<zi colLabel=\"alias\">" + "</zi>"
		// 兒子數量
		+ "<book441_numson>" + "</book441_numson>"
		// 乳名
		+ "<book441_rumin colLabel=\"alias\">" + "</book441_rumin>"
		// newedu 意義不明
		+"<book441_newedu/>" 
		// 文武 暫不考慮
		+ "<book441_wenwu/>"
		// 教育 暫不考慮
		+ "<book441_educ/>" 
		// identity 意義不明
		+ "<book441_identity/>" ;
		// 女兒數量 => 把老婆的女兒都加起來
		int dau = 0;
			try{
				dau += Integer.valueOf(p.get("dau"));
			}
			catch(Exception e){}
		r += "<book441_numdau>" + dau + "</book441_numdau>"
		// TODO 夭折
		+ "<book441_yaozhe>"  + "</book441_yaozhe>"
		// 生日
		+ "<event type=\"BIRTH\">" 
			+ "<time>" 
				+ "<year>" + p.get("RBIR") + "</year>"
				+ "<dynasty_code>" + 0 + "</dynasty_code>" 
				+ "<nh_code>" + 0 + "</nh_code>" 
				+ "<nh_year/>" 
				+ "<calendar>" + "國" + "</calendar>"
				+ "<intercalary/>" 
				+ "<month>" + p.get("RBM") + "</month>" 
				+ "<day/>" 
				+ "<hr/>" 
				+ "<ganzhi/>" 
				+ "<time_else/>" 
			+ "</time>"
			+ "<description/>" 
		+ "</event>" 
		// 死亡日	
		+ "<event type=\"DEATH\">" 
			+ "<time>" 
				+ "<year>" + p.get("RDEA") + "</year>"
				+ "<dynasty_code>" + 0 + "</dynasty_code>" 
				+ "<nh_code>" + 0 + "</nh_code>" 
				+ "<nh_year/>" 
				+ "<calendar>" + "國" + "</calendar>"
				+ "<intercalary/>" 
				+ "<month>" + p.get("RDM") + "</month>"
				+ "<day/>" 
				+ "<hr/>" 
				+ "<ganzhi/>" 
				+ "<time_else/>" 
			+ "</time>" + "<description/>"
		+ "</event>" 
		// 關係
		+ "<relations>";
		// 加入老公資訊
		if (p.husband != null) {
			r += "<is_a_relative_of ID=\"sysspBook441_person_" + p.husband.personid + "\" kinrel=\"W\">闕"
					+ p.husband.get("NAME") + "</is_a_relative_of>";
		}

		r += "</relations>" + "</person>";
		return r;
	}
	
	// 建立JPML
	private static void build(HashMap<String, person> map) throws UnsupportedEncodingException, FileNotFoundException {
		FileOutputStream fileStream = new FileOutputStream(new File("jpml.xml"));
		OutputStreamWriter fw = new OutputStreamWriter(fileStream, "UTF8");
		try {

			// TODO book編號暫定為441
			String s = "<JPML version=\"1.0\">\n" + "<genealogy_book bookID=\"sysspBook441\">\n" + "<book_basic_info>\n"
					+ "<JPMLbookID>sysspBook441</JPMLbookID>\n" + "<totalvolume>1</totalvolume>\n"
					+ "<bookName>TEST石倉家譜</bookName>\n" + "<familyName>闕</familyName>\n" + "<location>石倉</location>\n"
					+ "<author/>\n" + "<publisher/>\n" + "<publicationtime/>\n" + "<ISBN/>\n" + "<note/>\n"
					+ "</book_basic_info>\n" + "<columns_info>\n"
					+ "<column ID=\"book441_numson\" label=\"no\" type=\"INT_UNSIGNED\">生子數目</column>\n"
					+ "<column ID=\"book441_rumin\" label=\"alias\" type=\"VARCHAR\">乳名</column>\n"
					+ "<column ID=\"book441_newedu\" label=\"no\" type=\"INT_UNSIGNED\">newedu</column>\n"
					+ "<column ID=\"book441_wenwu\" label=\"no\" type=\"VARCHAR\">文武</column>\n"
					+ "<column ID=\"book441_educ\" label=\"no\" type=\"VARCHAR\">教育</column>\n"
					+ "<column ID=\"book441_identity\" label=\"no\" type=\"VARCHAR\">identity</column>\n"
					+ "<column ID=\"book441_numdau\" label=\"no\" type=\"INT_UNSIGNED\">生女數目</column>\n"
					+ "<column ID=\"book441_yaozhe\" label=\"no\" type=\"VARCHAR\">兒女夭折</column>\n"
					+ "</columns_info>\n" + "<!--  ********** [chapters] **********  -->\n" + "<chapters/>\n"
					+ "<!--  ********** [people] **********  -->\n" + "<people>\n";
			fw.append(s);

			// 建立一個sorting過的set
			TreeSet<person> set = new TreeSet<>();
			set.addAll(map.values());
			
			for (person p : set) {
				// 略過有衝突者, 或者無資料者
				if (p.conflict || p.ancientConflict || p.unconstructed)
					continue;
				fw.append(addPerson(p, p.ancestor));
				for (wife w : p.wifes)
					fw.write(addWife(w));
			}

			String e = "</people>\n" + "</genealogy_book>\n" + "</JPML>\n";
			fw.append(e);
			fw.flush();
			fw.close();

		} catch (IOException e2) {
			e2.printStackTrace();
		}

	}
	
	public static void main(String[] args) throws IOException, EncryptedDocumentException, InvalidFormatException {

		// 資料維護<code -> person>
		HashMap<String, person> Genealogy = new HashMap<>();
		// 孤單點
		HashSet<person> lonelyGenealogy = new HashSet<>();

		// 取得頁面(sheet)
		Workbook wb = WorkbookFactory.create(new File("test.xlsx"));
		Sheet sheet = wb.getSheet("人口");
		
		// 建立索引
		Row indexRow = sheet.getRow(0);
		for(int i=0;i<indexRow.getPhysicalNumberOfCells();i++)
			person.dataIndex.put(indexRow.getCell(i).toString(), i);
		
		// (始祖有重新key in)
		// 1 ~ 502 弼文
		// 503 ~ 1227 如祥
		// 1228 ~ 4653 盛宗
		
		// 上一個找到的人
		person lastperson = null;
		// 一個一個找出人
		for (int index = 1228; index <= 4653; index++) {
			Row r = sheet.getRow(index);
			String code = r.getCell(2).toString();
			String name = r.getCell(0).toString();
			person p = null;
			// TODO 若此人沒有code
			if(code == null || code == "" || code.compareTo("")==0){
				// 查詢與上一個人是否同名字 => 是則視為同一個人, 否則視為一個沒有額外資訊的孤單點
				if(lastperson!=null &&(name == lastperson.get("NAME") || name.compareTo(lastperson.get("NAME"))==0)){
					// 新增老婆資訊
					wife w = wife.getWife(r,lastperson);
					if (w != null)
						lastperson.wifes.add(w);
					continue;
				}
				else{
					// TODO 孤單點
					System.out.println(name);
					p = new person(r);
					wife w = wife.getWife(r,p);
					if (w != null)
						p.wifes.add(w);
					lonelyGenealogy.add(p);
				}
			}

			// TODO 若此人是一個提及到但是還未建立基本資訊的點
			if (Genealogy.containsKey(code) && Genealogy.get(code).unconstructed) {
				// 將基本資訊複寫上去
				p = new person(r);
				wife w = wife.getWife(r,p);
				if (w != null)
					p.wifes.add(w);
				// 把親代資訊寫入
				p.sons = Genealogy.get(code).sons;
				// 把親代的father都指向自己
				for(person sp : Genealogy.get(code).sons)
					sp.father = p;
				Genealogy.put(code, p);
			}
			// TODO 檢查是否conflict(此人已經存在而且名字不同) => 確定conflict,
			// 把子代都標記ancient_conflict
			else if (Genealogy.containsKey(code) && Genealogy.get(code).get("NAME").compareTo(name) != 0) {
				boolean done = false;
				// 有conflict但是已經提及過的人
				for (person cp : Genealogy.get(code).conflict_persons) {
					if (cp.get("NAME").compareTo(name) == 0) {
						wife w = wife.getWife(r,cp);
						if (w != null)
							cp.wifes.add(w);
						for (person s : Genealogy.get(code).sons)
							markAncientConflictRecursively(s);
						done = true;
						break;
					}
				}
				// 有conflict且是沒有看過的人
				if (!done) {
					p = new person(r);
					wife w = wife.getWife(r,p);
					if (w != null)
						p.wifes.add(w);
					p.conflict = true;
					Genealogy.get(code).conflict = true;
					Genealogy.get(code).conflict_persons.add(p);
					for (person s : Genealogy.get(code).sons)
						markAncientConflictRecursively(s);
				}
			}
			// 有此人且名字相同 => 同一個人但是有多一個老婆 => 新增老婆資訊
			else if (Genealogy.containsKey(code)) {
				wife w = wife.getWife(r,Genealogy.get(code));
				if (w != null)
					Genealogy.get(code).wifes.add(w);
			}
			// 沒有此人資料 => 新增此人與此人的老婆的資料, 並且與父輩連結
			else {
				p = new person(r);
				wife w = wife.getWife(r, p);
				if (w != null)
					p.wifes.add(w);
				// 非始祖的情況
				if(!p.ancestor){
					// 尋找父輩
					person f = Genealogy.get(p.getFatherCode());
					if (f == null) {// 父輩沒有建立過 => 建立個假的點
						f = new person();
						f.unconstructed = true;
						Genealogy.put(p.getFatherCode(), f);
					}
					f.sons.add(p);
					p.father = f;
					// 若父親是conflict或ancient_conflict => 自己為ancient_conflict
					if (f.conflict || f.ancientConflict) {
						p.ancientConflict = true;
					}
				}
				Genealogy.put(code, p);
			}
			lastperson = p;
		}
		System.out.println(Genealogy.keySet().size());
		build(Genealogy);
		System.out.println("lonely node : "+lonelyGenealogy.size());
	}
}
