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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;


public class JPML2 {

	// 判斷單一字元是否為阿拉伯數字
	private static boolean isDigit(char c) {
		if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8'
				|| c == '9')
			return true;
		return false;
	}	

	// 依據CODE判斷排行
	private static String getSibling(String CODE){
		if(CODE.length()>=1 && isDigit(CODE.charAt(CODE.length()-1))){
			return String.valueOf(CODE.charAt(CODE.length()-1));
		}
		return "";
	}
	
	// 把世代轉為阿拉伯數字
	private static String ChToInt(String input){
		int r = 0 ;
		for(int i=0;i<input.length();i++){
			switch(input.charAt(i)){
			case '一':	r+=1;
						break;
			case '二':	r+=2;
						break;
			case '三':	r+=3;
						break;
			case '四':	r+=4;
			break;
			case '五':	r+=5;
			break;
			case '六':	r+=6;
			break;
			case '七':	r+=7;
			break;
			case '八':	r+=8;
			break;
			case '九':	r+=9;
			break;
			case '十':	if(r==0)
							r=10;
						else
							r*=10;
			break;
			case '百':	if(r==0)
				r=100;
			else
				r*=100;
			break;
			case '零':
			break;
			case '世':
				if(i==input.length()-1)
					return String.valueOf(r);
				else
					return "";
			default:return "";
			}
		}
		return "";
	}
	
	// 由上到下標記衝突
	public static void markAncientConflictRecursively(person p) {
		p.ancientConflict = true;
		for (person s : p.sons)
				markAncientConflictRecursively(s);
		for(person cp : p.conflict_persons)
			markAncientConflictRecursively(cp);
	}

	// 建立個人的JPML資料
	private static String jpmlPerson(person p) {
		boolean noname = false;
		boolean nofather = false;
		if(p.father == null || p.father.unconstructed)
			nofather = true;
		if (p.get("NAME") == "" || p.get("NAME") == null || p.get("NAME").length() == 0)
			noname = true;
		// TODO book編號暫定為442
		String r="";
		r += "<person personID=\"sysspBook442_person_" + p.personid + "\">";
		// 父輩的為是
		r += "<admitted>" + "是" + "</admitted>";
		// 嘗試算出年紀 無法則不計
		try{
			r += "<age>" + (int)(Integer.valueOf(p.get("RDEA"))-Integer.valueOf(p.get("RBIR"))) + "</age>" + "<branch/>" + "<description/>";
		}
		catch(Exception e){
			r += "<age>" + "</age>" + "<branch/>" + "<description></description>";
		}
		r += "<familyName>" + "闕" + "</familyName>";
		r += "<gender>" + "男" + "</gender>";
		// TODO 世代 號 諱
		r += "<generation>" + ChToInt(p.get("ERA")) + "</generation>" + "<hao colLabel=\"alias\">" + p.get("hao") + "</hao>"
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
		if (p.isAcestor())
			r += "<rootFlag>" + 1 + "</rootFlag>";
		else if (nofather)
			r += "<rootFlag>" + 2 + "</rootFlag>";
		else
			r += "<rootFlag>" + 0 + "</rootFlag>";
		// shi 不知道是啥
		r += "<shi colLabel=\"alias\">" + "</shi>";
		// 暫時不管排行
		r += "<siblingOrder>" + getSibling(p.get("CODE")) + "</siblingOrder>"
		// sourceNote不知道啥
		+ "<sourceNote/>" 
		// 冊數 暫定為1
		+ "<volume>" + 1 + "</volume>" 
		// 字
		+ "<zi colLabel=\"alias\">" + p.get("zi") + "</zi>"
		// 兒子數量
		+ "<book442_numson>" + p.sons.size() + "</book442_numson>"
		// 乳名
		+ "<book442_rumin colLabel=\"alias\">" + p.get("rumin") + "</book442_rumin>"
		// newedu 意義不明
		+"<book442_newedu/>" 
		// 文武 暫不考慮
		+ "<book442_wenwu/>"
		// 教育 暫不考慮
		+ "<book442_educ/>" 
		// identity 意義不明
		+ "<book442_identity/>" ;
		// 女兒數量 => 把老婆的女兒都加起來
		int dau = 0;
		for(wife w : p.wifes){
			try{
				dau += Integer.valueOf(w.get("dau"));
			}
			catch(Exception e){}
		}
		r += "<book442_numdau>" + dau + "</book442_numdau>"
		// TODO 夭折
		+ "<book442_yaozhe>" + p.get("YAOZHE")  + "</book442_yaozhe>"
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
			+ "</time>";
		if(p.get("RBIR")=="" || p.get("RBIR").compareTo("")==0)
			r+= "<description>不詳</description>";
		else
			r+= "<description/>";
		r += "</event>" 
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
			+ "</time>";
		if(p.get("RDEA")=="" || p.get("RDEA").compareTo("")==0)
			r+= "<description>不詳</description>";
		else
			r+= "<description/>";
		r += "</event>" 
		// 關係
		+ "<relations>";
		// 加入老爸資訊
		if (!nofather) {
			r += "<relative ID=\"sysspBook442_person_" + p.father.personid ;
			r += "\" kinrel=\"F\" bookID=\"sysspBook442\">闕" + p.father.get("NAME")
					+ "</relative>";
		}

		// 加入老婆資訊
		for (int i = 0; i < p.wifes.size(); i++){
			if(p.wifes.get(i).get("WIFE")==""||p.wifes.get(i).get("WIFE").compareTo("")==0)
			r += "<relative ID=\"sysspbook442_person_" + p.wifes.get(i).personid + "\" kinrel=\"W\" bookID=\"sysspBook442\">"
					 + "(無名)" + "</relative>";
			else
				r += "<relative ID=\"sysspbook442_person_" + p.wifes.get(i).personid + "\" kinrel=\"W\" bookID=\"sysspBook442\">"
						+ p.wifes.get(i).get("WIFE") + "氏" + "</relative>";
		}
		// 加入兒子資訊
		for (int i = 0; i < p.sons.size(); i++)
			r += "<is_a_relative_of ID=\"sysspbook442_person_" + p.sons.get(i).personid + "\" kinrel=\"F\" bookID=\"sysspBook442\">" + "闕"
					+ p.sons.get(i).get("NAME") + "</is_a_relative_of>";

		r += "</relations>" + "</person>";
		return r;
	}
	
	// 建立老婆的JPML資料
	private static String jpmlWife(wife p){
		// TODO book編號暫定為442
		String r="";
		r += "<person personID=\"sysspBook442_person_" + p.personid + "\">";
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
		+ "<book442_numson>" + "</book442_numson>"
		// 乳名
		+ "<book442_rumin colLabel=\"alias\">" + "</book442_rumin>"
		// newedu 意義不明
		+"<book442_newedu/>" 
		// 文武 暫不考慮
		+ "<book442_wenwu/>"
		// 教育 暫不考慮
		+ "<book442_educ/>" 
		// identity 意義不明
		+ "<book442_identity/>" ;
		// 女兒數量 => 把老婆的女兒都加起來
		int dau = 0;
		try{
			dau += Integer.valueOf(p.get("dau"));
		}
		catch(Exception e){}
		r += "<book442_numdau>" + dau + "</book442_numdau>"
		// TODO 夭折
		+ "<book442_yaozhe>"  + "</book442_yaozhe>"
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
			+ "</time>";
		if(p.get("RBIR")=="" || p.get("RBIR").compareTo("")==0)
			r+= "<description>不詳</description>";
		else
			r+= "<description/>";
		r += "</event>" 
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
			+ "</time>";
		if(p.get("RDEA")=="" || p.get("RDEA").compareTo("")==0)
			r+= "<description>不詳</description>";
		else
			r+= "<description/>";
		r+= "</event>" 
		// 關係
		+ "<relations>";
		// 加入老公資訊
		if (p.husband != null) {
			r += "<is_a_relative_of ID=\"sysspBook442_person_" + p.husband.personid + "\" kinrel=\"W\" bookID=\"sysspBook442\">闕"
					+ p.husband.get("NAME") + "</is_a_relative_of>";
		}

		r += "</relations>" + "</person>";
		return r;
	}
	
	// 寫入錯誤資料
	private static void writeDebug(HSSFSheet ws,int index,person p){
		int i=0;
		Row row = ws.createRow(index);
		for (;i<person.dataIndex.size();i++)
		{
			Cell cell = row.createCell(i+3);
			try{
				if(person.indexRow.getCell(i).toString().compareTo("CODE")==0 && p.unconstructed){
					cell.setCellValue(p.CODE);
				}
				else
					cell.setCellValue(p.data.getCell(i).toString());
			}
			catch(Exception e){
				cell.setCellValue("");
			}
		}
		// 寫上原因
		row.createCell(0).setCellValue(p.conflict);
		row.createCell(1).setCellValue(p.ancientConflict);
		row.createCell(2).setCellValue(p.unconstructed);
	}
	
	// 建立JPML與扔出錯誤資訊檔
	private static void build(HashMap<String, person> map) throws UnsupportedEncodingException, FileNotFoundException {
		// 準備寫入JPML
		FileOutputStream jpmlFO = new FileOutputStream(new File("jpml.xml"));
		OutputStreamWriter jpmlFW = new OutputStreamWriter(jpmlFO, "UTF8");
		// 準備寫入Debug檔案
		FileOutputStream debugFO = new FileOutputStream("debug.xls");
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet ws = workbook.createSheet("debug");
		int debugRowIndex=0;
		// 先寫上index
		HSSFRow row = ws.createRow(debugRowIndex++);
		for (int i=0;i<person.dataIndex.size();i++)
		{
			HSSFCell cell = row.createCell(i+3);
			try{
				cell.setCellValue(person.indexRow.getCell(i).toString());
			}
			catch(Exception e){
				cell.setCellValue("");
			}
		}
		row.createCell(0).setCellValue("conflict?");
		row.createCell(1).setCellValue("ancient conflict?");
		row.createCell(2).setCellValue("not mentioned?");
		
		try {

			// JPML前面的部分 book編號暫定為442
			String s = "<JPML version=\"1.11\">\n" + "<genealogy_book bookID=\"sysspBook442\">\n" + "<book_basic_info>\n"
					+ "<JPMLbookID>sysspBook442</JPMLbookID>\n" + "<totalvolume>1</totalvolume>\n"
					+ "<bookName>TEST石倉家譜</bookName>\n" + "<familyName>闕</familyName>\n" + "<location>石倉</location>\n"
					+ "<author/>\n" + "<publisher/>\n" + "<publicationtime/>\n" + "<ISBN/>\n" + "<note/>\n"
					+ "</book_basic_info>\n" + "<columns_info>\n"
					+ "<column ID=\"book442_numson\" label=\"no\" type=\"INT_UNSIGNED\">生子數目</column>\n"
					+ "<column ID=\"book442_rumin\" label=\"alias\" type=\"VARCHAR\">乳名</column>\n"
					+ "<column ID=\"book442_newedu\" label=\"no\" type=\"INT_UNSIGNED\">newedu</column>\n"
					+ "<column ID=\"book442_wenwu\" label=\"no\" type=\"VARCHAR\">文武</column>\n"
					+ "<column ID=\"book442_educ\" label=\"no\" type=\"VARCHAR\">教育</column>\n"
					+ "<column ID=\"book442_identity\" label=\"no\" type=\"VARCHAR\">identity</column>\n"
					+ "<column ID=\"book442_numdau\" label=\"no\" type=\"INT_UNSIGNED\">生女數目</column>\n"
					+ "<column ID=\"book442_yaozhe\" label=\"no\" type=\"VARCHAR\">兒女夭折</column>\n"
					+ "</columns_info>\n" + "<!--  ********** [chapters] **********  -->\n" + "<chapters/>\n"
					+ "<!--  ********** [people] **********  -->\n" + "<people>\n";
			jpmlFW.append(s);

			// 建立一個sorting過的set
			TreeSet<person> set = new TreeSet<>();
			set.addAll(map.values());
			
			for (person p : set) {
				// 略過有衝突者, 或者無資料者 => 寫入debug
				if (p.conflict || p.ancientConflict || p.unconstructed){

					writeDebug(ws,debugRowIndex++,p);
					if(p.unconstructed){
						writeDebug(ws,debugRowIndex++,p.sons.get(0));
					}
					for(person cp : p.conflict_persons){
						if(cp.unconstructed)
							continue;
						writeDebug(ws,debugRowIndex++,cp);
						if(cp.unconstructed){
							writeDebug(ws,debugRowIndex++,cp.sons.get(0));
						}
					}
					continue;
				}
				// 其餘的寫入jpml
				jpmlFW.append(jpmlPerson(p));
				for (wife w : p.wifes)
					jpmlFW.write(jpmlWife(w));
			}

			// 資料表寫入debug檔案
			workbook.write(debugFO);
			// JPML寫入最後一段
			String e = "</people>\n" + "</genealogy_book>\n" + "</JPML>\n";
			jpmlFW.append(e);
			
			// 關閉並儲存JPML
			jpmlFW.flush();
			jpmlFW.close();
			
			
			// 關閉並儲存debug
			debugFO.flush();
			debugFO.close();

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
		person.indexRow = indexRow;
		
		// (始祖有重新key in)
		// 1 ~ 502 弼文
		// 503 ~ 1227 如祥
		// 1229 ~ 4654 盛宗
		
		// 上一個找到的人
		person lastperson = null;
		// 一個一個找出人
		for (int index = 1; index <= 502; index++) {
//			System.out.println("index:"+index);
			Row r = sheet.getRow(index);
			String code="";
			try{
				r.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
				code = r.getCell(2).getStringCellValue();
			}
			catch(Exception e){
				code = "";
			}
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
					if(Genealogy.get(code).ancientConflict)
						p.ancientConflict=true;
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
				if(!p.isAcestor()){
//					if(p.getFatherCode()=="")
//						continue;
					// 尋找父輩
					person f = Genealogy.get(p.getFatherCode());
					if (f == null) {// 父輩沒有建立過 => 建立個假的點
						f = new person();
						f.unconstructed = true;
						f.CODE = p.getFatherCode();
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
		System.out.println("人員數(不含妻子):"+Genealogy.keySet().size());
		build(Genealogy);
		System.out.println("lonely node : "+lonelyGenealogy.size());
	}
}
