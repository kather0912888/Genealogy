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
	// �H
	private static class person implements Comparable<person>{
		// ���޼���
		private static HashMap<String,Integer> dataIndex = new HashMap<>();
		// �����إߪ��I
		boolean unconstructed = false;
		// �O�_�O�l��?
		boolean ancestor = false;
		// �O�_�Ĭ�(��H�@�ɦPcode)
		boolean conflict = false;
		// �����O�_�Ĭ�
		boolean ancientConflict = false;
		// �Y�Ĭ�O�_�w�g��X���ɮ�?
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

	// �ѱC
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
	
	// �y���s��, ��1�}�l
	static int personID=1;
	
	// �ѤW��U�аO�Ĭ�
	public static void markAncientConflictRecursively(person p) {
		p.ancientConflict = true;
		for (person s : p.sons)
			if (!s.ancientConflict)
				markAncientConflictRecursively(s);
	}

	// �إ߭ӤH��JPML���
	private static String addPerson(person p, boolean ancestor) {
		boolean noname = false;
		boolean nofather = false;
		if(p.father == null || p.father.unconstructed)
			nofather = true;
		if (p.get("NAME") == "" || p.get("NAME") == null || p.get("NAME").length() == 0)
			noname = true;
		// TODO book�s���ȩw��441
		String r="";
		r += "<person personID=\"sysspBook441_person_" + p.personid + "\">";
		// ���������O
		r += "<admitted>" + "�O" + "</admitted>";
		// ���պ�X�~�� �L�k�h���p
		try{
			r += "<age>" + (Integer.valueOf(p.get("RDEA"))-Integer.valueOf(p.get("RBIR"))) + "</age>" + "<branch/>" + "<description/>";
		}
		catch(Exception e){
			r += "<age>" + "</age>" + "<branch/>" + "<description></description>";
		}
		r += "<familyName>" + "��" + "</familyName>";
		r += "<gender>" + "�k" + "</gender>";
		// TODO �@�N �� ��
		r += "<generation>" + p.get("ERA") + "</generation>" + "<hao colLabel=\"alias\">" + p.get("hao") + "</hao>"
				+ "<hui colLabel=\"alias\">" + p.get("hui") + "</hui>"
				// �a��
				+ "<location/>";
		// 0�����t�H��, 1�����t�H�����t��
		r += "<mateFlag>" + 0 + "</mateFlag>";
		// �L�W����, �Y�L�W, �]�w��1�B�W�r��"(�L�W)"
		if (noname)
			r += "<nonameFlag>" + 1 + "</nonameFlag>";
		else
			r += "<nonameFlag>" + 0 + "</nonameFlag>";
		// note �����D�Oԣ
		r += "<note>" + "</note>";
		// ming
		r += "<otherAlias colLabel=\"alias\">" + p.get("ming") + "</otherAlias>" + "<page/>";
		if (noname)
			r += "<personName>" + "(�L�W)" + "</personName>";
		else
			r += "<personName>" + p.get("NAME") + "</personName>";
		// TODO 1���l��, 2���d���˸�T, 0��L
		if (p.ancestor)
			r += "<rootFlag>" + 1 + "</rootFlag>";
		else if (nofather)
			r += "<rootFlag>" + 0 + "</rootFlag>";
		else
			r += "<rootFlag>" + 2 + "</rootFlag>";
		// shi �����D�Oԣ
		r += "<shi colLabel=\"alias\">" + "</shi>";
		// �Ȯɤ��ޱƦ�
		r += "<siblingOrder/>"
		// sourceNote�����Dԣ
		+ "<sourceNote/>" 
		// �U�� �ȩw��1
		+ "<volume>" + 1 + "</volume>" 
		// �r
		+ "<zi colLabel=\"alias\">" + p.get("zi") + "</zi>"
		// ��l�ƶq
		+ "<book441_numson>" + p.sons.size() + "</book441_numson>"
		// �ŦW
		+ "<book441_rumin colLabel=\"alias\">" + p.get("rumin") + "</book441_rumin>"
		// newedu �N�q����
		+"<book441_newedu/>" 
		// ��Z �Ȥ��Ҽ{
		+ "<book441_wenwu/>"
		// �Ш| �Ȥ��Ҽ{
		+ "<book441_educ/>" 
		// identity �N�q����
		+ "<book441_identity/>" ;
		// �k��ƶq => ��ѱC���k�ೣ�[�_��
		int dau = 0;
		for(wife w : p.wifes){
			try{
				dau += Integer.valueOf(w.get("dau"));
			}
			catch(Exception e){}
		}
		r += "<book441_numdau>" + dau + "</book441_numdau>"
		// TODO �ԧ�
		+ "<book441_yaozhe>" + p.get("YAOZHE")  + "</book441_yaozhe>"
		// �ͤ�
		+ "<event type=\"BIRTH\">" 
			+ "<time>" 
				+ "<year>" + p.get("RBIR") + "</year>"
				+ "<dynasty_code>" + 0 + "</dynasty_code>" 
				+ "<nh_code>" + 0 + "</nh_code>" 
				+ "<nh_year/>" 
				+ "<calendar>" + "��" + "</calendar>"
				+ "<intercalary/>" 
				+ "<month>" + p.get("RBM") + "</month>" 
				+ "<day/>" 
				+ "<hr/>" 
				+ "<ganzhi/>" 
				+ "<time_else/>" 
			+ "</time>"
			+ "<description/>" 
		+ "</event>" 
		// ���`��	
		+ "<event type=\"DEATH\">" 
			+ "<time>" 
				+ "<year>" + p.get("RDEA") + "</year>"
				+ "<dynasty_code>" + 0 + "</dynasty_code>" 
				+ "<nh_code>" + 0 + "</nh_code>" 
				+ "<nh_year/>" 
				+ "<calendar>" + "��" + "</calendar>"
				+ "<intercalary/>" 
				+ "<month>" + p.get("RDM") + "</month>"
				+ "<day/>" 
				+ "<hr/>" 
				+ "<ganzhi/>" 
				+ "<time_else/>" 
			+ "</time>" + "<description/>"
		+ "</event>" 
		// ���Y
		+ "<relations>";
		// �[�J�Ѫ���T
		if (!nofather) {
			r += "<relative ID=\"sysspBook441_person_" + p.father.personid ;
			r += "\" kinrel=\"F\">��" + p.father.get("NAME")
					+ "</relative>";
		}

		// �[�J�ѱC��T
		for (int i = 0; i < p.wifes.size(); i++)
			r += "<relative ID=\"sysspbook441_person_" + p.wifes.get(i).personid + "\" kinrel=\"W\">"
					+ p.wifes.get(i).get("WIFE") + "(�L�W)" + "</relative>";
		// �[�J��l��T
		for (int i = 0; i < p.sons.size(); i++)
			r += "<is_a_relative_of ID=\"sysspbook441_person_" + p.sons.get(i).personid + "\" kinrel=\"F\">" + "��"
					+ p.sons.get(i).get("NAME") + "</is_a_relative_of>";

		r += "</relations>" + "</person>";
		return r;
	}
	
	// �إߦѱC��JPML���
	private static String addWife(wife p){
		// TODO book�s���ȩw��441
		String r="";
		r += "<person personID=\"sysspBook441_person_" + p.personid + "\">";
		r += "<admitted>" + "�_" + "</admitted>";
		// ���պ�X�~�� �L�k�h���p
		try{
			r += "<age>" + (Integer.valueOf(p.get("RDEA"))-Integer.valueOf(p.get("RBIR"))) + "</age>" + "<branch/>" + "<description/>";
		}
		catch(Exception e){
			r += "<age>" + "</age>" + "<branch/>" + "<description></description>";
		}
		r += "<familyName>" + p.get("WIFE") + "</familyName>";
		r += "<gender>" + "�k" + "</gender>";
		// TODO �@�N �� ��
		r += "<generation>"  + "</generation>" + "<hao colLabel=\"alias\">"  + "</hao>"
				+ "<hui colLabel=\"alias\">"  + "</hui>"
				// �a��
				+ "<location/>";
		// 0�����t�H��, 1�����t�H�����t��
		r += "<mateFlag>" + 1 + "</mateFlag>";
		// �L�W����, �Y�L�W, �]�w��1�B�W�r��"(�L�W)"
		r += "<nonameFlag>" + 1 + "</nonameFlag>";
		// note �����D�Oԣ
		r += "<note>" + "</note>";
		// ming
		r += "<otherAlias colLabel=\"alias\">" + "</otherAlias>" + "<page/>";
		r += "<personName>" + "(�L�W)" + "</personName>";
		// TODO 1���l��, 2���d���˸�T, 0��L
		r += "<rootFlag>" + 0 + "</rootFlag>";
		// shi �����D�Oԣ
		r += "<shi colLabel=\"alias\">" + "</shi>";
		// �Ȯɤ��ޱƦ�
		r += "<siblingOrder/>"
		// sourceNote�����Dԣ
		+ "<sourceNote/>" 
		// �U�� �ȩw��1
		+ "<volume>" + 1 + "</volume>" 
		// �r
		+ "<zi colLabel=\"alias\">" + "</zi>"
		// ��l�ƶq
		+ "<book441_numson>" + "</book441_numson>"
		// �ŦW
		+ "<book441_rumin colLabel=\"alias\">" + "</book441_rumin>"
		// newedu �N�q����
		+"<book441_newedu/>" 
		// ��Z �Ȥ��Ҽ{
		+ "<book441_wenwu/>"
		// �Ш| �Ȥ��Ҽ{
		+ "<book441_educ/>" 
		// identity �N�q����
		+ "<book441_identity/>" ;
		// �k��ƶq => ��ѱC���k�ೣ�[�_��
		int dau = 0;
			try{
				dau += Integer.valueOf(p.get("dau"));
			}
			catch(Exception e){}
		r += "<book441_numdau>" + dau + "</book441_numdau>"
		// TODO �ԧ�
		+ "<book441_yaozhe>"  + "</book441_yaozhe>"
		// �ͤ�
		+ "<event type=\"BIRTH\">" 
			+ "<time>" 
				+ "<year>" + p.get("RBIR") + "</year>"
				+ "<dynasty_code>" + 0 + "</dynasty_code>" 
				+ "<nh_code>" + 0 + "</nh_code>" 
				+ "<nh_year/>" 
				+ "<calendar>" + "��" + "</calendar>"
				+ "<intercalary/>" 
				+ "<month>" + p.get("RBM") + "</month>" 
				+ "<day/>" 
				+ "<hr/>" 
				+ "<ganzhi/>" 
				+ "<time_else/>" 
			+ "</time>"
			+ "<description/>" 
		+ "</event>" 
		// ���`��	
		+ "<event type=\"DEATH\">" 
			+ "<time>" 
				+ "<year>" + p.get("RDEA") + "</year>"
				+ "<dynasty_code>" + 0 + "</dynasty_code>" 
				+ "<nh_code>" + 0 + "</nh_code>" 
				+ "<nh_year/>" 
				+ "<calendar>" + "��" + "</calendar>"
				+ "<intercalary/>" 
				+ "<month>" + p.get("RDM") + "</month>"
				+ "<day/>" 
				+ "<hr/>" 
				+ "<ganzhi/>" 
				+ "<time_else/>" 
			+ "</time>" + "<description/>"
		+ "</event>" 
		// ���Y
		+ "<relations>";
		// �[�J�Ѥ���T
		if (p.husband != null) {
			r += "<is_a_relative_of ID=\"sysspBook441_person_" + p.husband.personid + "\" kinrel=\"W\">��"
					+ p.husband.get("NAME") + "</is_a_relative_of>";
		}

		r += "</relations>" + "</person>";
		return r;
	}
	
	// �إ�JPML
	private static void build(HashMap<String, person> map) throws UnsupportedEncodingException, FileNotFoundException {
		FileOutputStream fileStream = new FileOutputStream(new File("jpml.xml"));
		OutputStreamWriter fw = new OutputStreamWriter(fileStream, "UTF8");
		try {

			// TODO book�s���ȩw��441
			String s = "<JPML version=\"1.0\">\n" + "<genealogy_book bookID=\"sysspBook441\">\n" + "<book_basic_info>\n"
					+ "<JPMLbookID>sysspBook441</JPMLbookID>\n" + "<totalvolume>1</totalvolume>\n"
					+ "<bookName>TEST�ۭܮa��</bookName>\n" + "<familyName>��</familyName>\n" + "<location>�ۭ�</location>\n"
					+ "<author/>\n" + "<publisher/>\n" + "<publicationtime/>\n" + "<ISBN/>\n" + "<note/>\n"
					+ "</book_basic_info>\n" + "<columns_info>\n"
					+ "<column ID=\"book441_numson\" label=\"no\" type=\"INT_UNSIGNED\">�ͤl�ƥ�</column>\n"
					+ "<column ID=\"book441_rumin\" label=\"alias\" type=\"VARCHAR\">�ŦW</column>\n"
					+ "<column ID=\"book441_newedu\" label=\"no\" type=\"INT_UNSIGNED\">newedu</column>\n"
					+ "<column ID=\"book441_wenwu\" label=\"no\" type=\"VARCHAR\">��Z</column>\n"
					+ "<column ID=\"book441_educ\" label=\"no\" type=\"VARCHAR\">�Ш|</column>\n"
					+ "<column ID=\"book441_identity\" label=\"no\" type=\"VARCHAR\">identity</column>\n"
					+ "<column ID=\"book441_numdau\" label=\"no\" type=\"INT_UNSIGNED\">�ͤk�ƥ�</column>\n"
					+ "<column ID=\"book441_yaozhe\" label=\"no\" type=\"VARCHAR\">��k�ԧ�</column>\n"
					+ "</columns_info>\n" + "<!--  ********** [chapters] **********  -->\n" + "<chapters/>\n"
					+ "<!--  ********** [people] **********  -->\n" + "<people>\n";
			fw.append(s);

			// �إߤ@��sorting�L��set
			TreeSet<person> set = new TreeSet<>();
			set.addAll(map.values());
			
			for (person p : set) {
				// ���L���Ĭ��, �Ϊ̵L��ƪ�
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

		// ��ƺ��@<code -> person>
		HashMap<String, person> Genealogy = new HashMap<>();
		// �t���I
		HashSet<person> lonelyGenealogy = new HashSet<>();

		// ���o����(sheet)
		Workbook wb = WorkbookFactory.create(new File("test.xlsx"));
		Sheet sheet = wb.getSheet("�H�f");
		
		// �إ߯���
		Row indexRow = sheet.getRow(0);
		for(int i=0;i<indexRow.getPhysicalNumberOfCells();i++)
			person.dataIndex.put(indexRow.getCell(i).toString(), i);
		
		// (�l�������skey in)
		// 1 ~ 502 �]��
		// 503 ~ 1227 �p��
		// 1228 ~ 4653 ���v
		
		// �W�@�ӧ�쪺�H
		person lastperson = null;
		// �@�Ӥ@�ӧ�X�H
		for (int index = 1228; index <= 4653; index++) {
			Row r = sheet.getRow(index);
			String code = r.getCell(2).toString();
			String name = r.getCell(0).toString();
			person p = null;
			// TODO �Y���H�S��code
			if(code == null || code == "" || code.compareTo("")==0){
				// �d�߻P�W�@�ӤH�O�_�P�W�r => �O�h�����P�@�ӤH, �_�h�����@�ӨS���B�~��T���t���I
				if(lastperson!=null &&(name == lastperson.get("NAME") || name.compareTo(lastperson.get("NAME"))==0)){
					// �s�W�ѱC��T
					wife w = wife.getWife(r,lastperson);
					if (w != null)
						lastperson.wifes.add(w);
					continue;
				}
				else{
					// TODO �t���I
					System.out.println(name);
					p = new person(r);
					wife w = wife.getWife(r,p);
					if (w != null)
						p.wifes.add(w);
					lonelyGenealogy.add(p);
				}
			}

			// TODO �Y���H�O�@�Ӵ��Ψ���O�٥��إ߰򥻸�T���I
			if (Genealogy.containsKey(code) && Genealogy.get(code).unconstructed) {
				// �N�򥻸�T�Ƽg�W�h
				p = new person(r);
				wife w = wife.getWife(r,p);
				if (w != null)
					p.wifes.add(w);
				// ��˥N��T�g�J
				p.sons = Genealogy.get(code).sons;
				// ��˥N��father�����V�ۤv
				for(person sp : Genealogy.get(code).sons)
					sp.father = p;
				Genealogy.put(code, p);
			}
			// TODO �ˬd�O�_conflict(���H�w�g�s�b�ӥB�W�r���P) => �T�wconflict,
			// ��l�N���аOancient_conflict
			else if (Genealogy.containsKey(code) && Genealogy.get(code).get("NAME").compareTo(name) != 0) {
				boolean done = false;
				// ��conflict���O�w�g���ιL���H
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
				// ��conflict�B�O�S���ݹL���H
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
			// �����H�B�W�r�ۦP => �P�@�ӤH���O���h�@�ӦѱC => �s�W�ѱC��T
			else if (Genealogy.containsKey(code)) {
				wife w = wife.getWife(r,Genealogy.get(code));
				if (w != null)
					Genealogy.get(code).wifes.add(w);
			}
			// �S�����H��� => �s�W���H�P���H���ѱC�����, �åB�P�����s��
			else {
				p = new person(r);
				wife w = wife.getWife(r, p);
				if (w != null)
					p.wifes.add(w);
				// �D�l�������p
				if(!p.ancestor){
					// �M�����
					person f = Genealogy.get(p.getFatherCode());
					if (f == null) {// �����S���إ߹L => �إ߭Ӱ����I
						f = new person();
						f.unconstructed = true;
						Genealogy.put(p.getFatherCode(), f);
					}
					f.sons.add(p);
					p.father = f;
					// �Y���ˬOconflict��ancient_conflict => �ۤv��ancient_conflict
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
