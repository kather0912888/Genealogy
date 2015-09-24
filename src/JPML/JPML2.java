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

	// �P�_��@�r���O�_�����ԧB�Ʀr
	private static boolean isDigit(char c) {
		if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8'
				|| c == '9')
			return true;
		return false;
	}	

	// �̾�CODE�P�_�Ʀ�
	private static String getSibling(String CODE){
		if(CODE.length()>=1 && isDigit(CODE.charAt(CODE.length()-1))){
			return String.valueOf(CODE.charAt(CODE.length()-1));
		}
		return "";
	}
	
	// ��@�N�ର���ԧB�Ʀr
	private static String ChToInt(String input){
		int r = 0 ;
		for(int i=0;i<input.length();i++){
			switch(input.charAt(i)){
			case '�@':	r+=1;
						break;
			case '�G':	r+=2;
						break;
			case '�T':	r+=3;
						break;
			case '�|':	r+=4;
			break;
			case '��':	r+=5;
			break;
			case '��':	r+=6;
			break;
			case '�C':	r+=7;
			break;
			case '�K':	r+=8;
			break;
			case '�E':	r+=9;
			break;
			case '�Q':	if(r==0)
							r=10;
						else
							r*=10;
			break;
			case '��':	if(r==0)
				r=100;
			else
				r*=100;
			break;
			case '�s':
			break;
			case '�@':
				if(i==input.length()-1)
					return String.valueOf(r);
				else
					return "";
			default:return "";
			}
		}
		return "";
	}
	
	// �ѤW��U�аO�Ĭ�
	public static void markAncientConflictRecursively(person p) {
		p.ancientConflict = true;
		for (person s : p.sons)
				markAncientConflictRecursively(s);
		for(person cp : p.conflict_persons)
			markAncientConflictRecursively(cp);
	}

	// �إ߭ӤH��JPML���
	private static String jpmlPerson(person p) {
		boolean noname = false;
		boolean nofather = false;
		if(p.father == null || p.father.unconstructed)
			nofather = true;
		if (p.get("NAME") == "" || p.get("NAME") == null || p.get("NAME").length() == 0)
			noname = true;
		// TODO book�s���ȩw��442
		String r="";
		r += "<person personID=\"sysspBook442_person_" + p.personid + "\">";
		// ���������O
		r += "<admitted>" + "�O" + "</admitted>";
		// ���պ�X�~�� �L�k�h���p
		try{
			r += "<age>" + (int)(Integer.valueOf(p.get("RDEA"))-Integer.valueOf(p.get("RBIR"))) + "</age>" + "<branch/>" + "<description/>";
		}
		catch(Exception e){
			r += "<age>" + "</age>" + "<branch/>" + "<description></description>";
		}
		r += "<familyName>" + "��" + "</familyName>";
		r += "<gender>" + "�k" + "</gender>";
		// TODO �@�N �� ��
		r += "<generation>" + ChToInt(p.get("ERA")) + "</generation>" + "<hao colLabel=\"alias\">" + p.get("hao") + "</hao>"
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
		if (p.isAcestor())
			r += "<rootFlag>" + 1 + "</rootFlag>";
		else if (nofather)
			r += "<rootFlag>" + 2 + "</rootFlag>";
		else
			r += "<rootFlag>" + 0 + "</rootFlag>";
		// shi �����D�Oԣ
		r += "<shi colLabel=\"alias\">" + "</shi>";
		// �Ȯɤ��ޱƦ�
		r += "<siblingOrder>" + getSibling(p.get("CODE")) + "</siblingOrder>"
		// sourceNote�����Dԣ
		+ "<sourceNote/>" 
		// �U�� �ȩw��1
		+ "<volume>" + 1 + "</volume>" 
		// �r
		+ "<zi colLabel=\"alias\">" + p.get("zi") + "</zi>"
		// ��l�ƶq
		+ "<book442_numson>" + p.sons.size() + "</book442_numson>"
		// �ŦW
		+ "<book442_rumin colLabel=\"alias\">" + p.get("rumin") + "</book442_rumin>"
		// newedu �N�q����
		+"<book442_newedu/>" 
		// ��Z �Ȥ��Ҽ{
		+ "<book442_wenwu/>"
		// �Ш| �Ȥ��Ҽ{
		+ "<book442_educ/>" 
		// identity �N�q����
		+ "<book442_identity/>" ;
		// �k��ƶq => ��ѱC���k�ೣ�[�_��
		int dau = 0;
		for(wife w : p.wifes){
			try{
				dau += Integer.valueOf(w.get("dau"));
			}
			catch(Exception e){}
		}
		r += "<book442_numdau>" + dau + "</book442_numdau>"
		// TODO �ԧ�
		+ "<book442_yaozhe>" + p.get("YAOZHE")  + "</book442_yaozhe>"
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
			+ "</time>";
		if(p.get("RBIR")=="" || p.get("RBIR").compareTo("")==0)
			r+= "<description>����</description>";
		else
			r+= "<description/>";
		r += "</event>" 
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
			+ "</time>";
		if(p.get("RDEA")=="" || p.get("RDEA").compareTo("")==0)
			r+= "<description>����</description>";
		else
			r+= "<description/>";
		r += "</event>" 
		// ���Y
		+ "<relations>";
		// �[�J�Ѫ���T
		if (!nofather) {
			r += "<relative ID=\"sysspBook442_person_" + p.father.personid ;
			r += "\" kinrel=\"F\" bookID=\"sysspBook442\">��" + p.father.get("NAME")
					+ "</relative>";
		}

		// �[�J�ѱC��T
		for (int i = 0; i < p.wifes.size(); i++){
			if(p.wifes.get(i).get("WIFE")==""||p.wifes.get(i).get("WIFE").compareTo("")==0)
			r += "<relative ID=\"sysspbook442_person_" + p.wifes.get(i).personid + "\" kinrel=\"W\" bookID=\"sysspBook442\">"
					 + "(�L�W)" + "</relative>";
			else
				r += "<relative ID=\"sysspbook442_person_" + p.wifes.get(i).personid + "\" kinrel=\"W\" bookID=\"sysspBook442\">"
						+ p.wifes.get(i).get("WIFE") + "��" + "</relative>";
		}
		// �[�J��l��T
		for (int i = 0; i < p.sons.size(); i++)
			r += "<is_a_relative_of ID=\"sysspbook442_person_" + p.sons.get(i).personid + "\" kinrel=\"F\" bookID=\"sysspBook442\">" + "��"
					+ p.sons.get(i).get("NAME") + "</is_a_relative_of>";

		r += "</relations>" + "</person>";
		return r;
	}
	
	// �إߦѱC��JPML���
	private static String jpmlWife(wife p){
		// TODO book�s���ȩw��442
		String r="";
		r += "<person personID=\"sysspBook442_person_" + p.personid + "\">";
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
		+ "<book442_numson>" + "</book442_numson>"
		// �ŦW
		+ "<book442_rumin colLabel=\"alias\">" + "</book442_rumin>"
		// newedu �N�q����
		+"<book442_newedu/>" 
		// ��Z �Ȥ��Ҽ{
		+ "<book442_wenwu/>"
		// �Ш| �Ȥ��Ҽ{
		+ "<book442_educ/>" 
		// identity �N�q����
		+ "<book442_identity/>" ;
		// �k��ƶq => ��ѱC���k�ೣ�[�_��
		int dau = 0;
		try{
			dau += Integer.valueOf(p.get("dau"));
		}
		catch(Exception e){}
		r += "<book442_numdau>" + dau + "</book442_numdau>"
		// TODO �ԧ�
		+ "<book442_yaozhe>"  + "</book442_yaozhe>"
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
			+ "</time>";
		if(p.get("RBIR")=="" || p.get("RBIR").compareTo("")==0)
			r+= "<description>����</description>";
		else
			r+= "<description/>";
		r += "</event>" 
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
			+ "</time>";
		if(p.get("RDEA")=="" || p.get("RDEA").compareTo("")==0)
			r+= "<description>����</description>";
		else
			r+= "<description/>";
		r+= "</event>" 
		// ���Y
		+ "<relations>";
		// �[�J�Ѥ���T
		if (p.husband != null) {
			r += "<is_a_relative_of ID=\"sysspBook442_person_" + p.husband.personid + "\" kinrel=\"W\" bookID=\"sysspBook442\">��"
					+ p.husband.get("NAME") + "</is_a_relative_of>";
		}

		r += "</relations>" + "</person>";
		return r;
	}
	
	// �g�J���~���
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
		// �g�W��]
		row.createCell(0).setCellValue(p.conflict);
		row.createCell(1).setCellValue(p.ancientConflict);
		row.createCell(2).setCellValue(p.unconstructed);
	}
	
	// �إ�JPML�P���X���~��T��
	private static void build(HashMap<String, person> map) throws UnsupportedEncodingException, FileNotFoundException {
		// �ǳƼg�JJPML
		FileOutputStream jpmlFO = new FileOutputStream(new File("jpml.xml"));
		OutputStreamWriter jpmlFW = new OutputStreamWriter(jpmlFO, "UTF8");
		// �ǳƼg�JDebug�ɮ�
		FileOutputStream debugFO = new FileOutputStream("debug.xls");
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet ws = workbook.createSheet("debug");
		int debugRowIndex=0;
		// ���g�Windex
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

			// JPML�e�������� book�s���ȩw��442
			String s = "<JPML version=\"1.11\">\n" + "<genealogy_book bookID=\"sysspBook442\">\n" + "<book_basic_info>\n"
					+ "<JPMLbookID>sysspBook442</JPMLbookID>\n" + "<totalvolume>1</totalvolume>\n"
					+ "<bookName>TEST�ۭܮa��</bookName>\n" + "<familyName>��</familyName>\n" + "<location>�ۭ�</location>\n"
					+ "<author/>\n" + "<publisher/>\n" + "<publicationtime/>\n" + "<ISBN/>\n" + "<note/>\n"
					+ "</book_basic_info>\n" + "<columns_info>\n"
					+ "<column ID=\"book442_numson\" label=\"no\" type=\"INT_UNSIGNED\">�ͤl�ƥ�</column>\n"
					+ "<column ID=\"book442_rumin\" label=\"alias\" type=\"VARCHAR\">�ŦW</column>\n"
					+ "<column ID=\"book442_newedu\" label=\"no\" type=\"INT_UNSIGNED\">newedu</column>\n"
					+ "<column ID=\"book442_wenwu\" label=\"no\" type=\"VARCHAR\">��Z</column>\n"
					+ "<column ID=\"book442_educ\" label=\"no\" type=\"VARCHAR\">�Ш|</column>\n"
					+ "<column ID=\"book442_identity\" label=\"no\" type=\"VARCHAR\">identity</column>\n"
					+ "<column ID=\"book442_numdau\" label=\"no\" type=\"INT_UNSIGNED\">�ͤk�ƥ�</column>\n"
					+ "<column ID=\"book442_yaozhe\" label=\"no\" type=\"VARCHAR\">��k�ԧ�</column>\n"
					+ "</columns_info>\n" + "<!--  ********** [chapters] **********  -->\n" + "<chapters/>\n"
					+ "<!--  ********** [people] **********  -->\n" + "<people>\n";
			jpmlFW.append(s);

			// �إߤ@��sorting�L��set
			TreeSet<person> set = new TreeSet<>();
			set.addAll(map.values());
			
			for (person p : set) {
				// ���L���Ĭ��, �Ϊ̵L��ƪ� => �g�Jdebug
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
				// ��l���g�Jjpml
				jpmlFW.append(jpmlPerson(p));
				for (wife w : p.wifes)
					jpmlFW.write(jpmlWife(w));
			}

			// ��ƪ�g�Jdebug�ɮ�
			workbook.write(debugFO);
			// JPML�g�J�̫�@�q
			String e = "</people>\n" + "</genealogy_book>\n" + "</JPML>\n";
			jpmlFW.append(e);
			
			// �������x�sJPML
			jpmlFW.flush();
			jpmlFW.close();
			
			
			// �������x�sdebug
			debugFO.flush();
			debugFO.close();

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
		person.indexRow = indexRow;
		
		// (�l�������skey in)
		// 1 ~ 502 �]��
		// 503 ~ 1227 �p��
		// 1229 ~ 4654 ���v
		
		// �W�@�ӧ�쪺�H
		person lastperson = null;
		// �@�Ӥ@�ӧ�X�H
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
					if(Genealogy.get(code).ancientConflict)
						p.ancientConflict=true;
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
				if(!p.isAcestor()){
//					if(p.getFatherCode()=="")
//						continue;
					// �M�����
					person f = Genealogy.get(p.getFatherCode());
					if (f == null) {// �����S���إ߹L => �إ߭Ӱ����I
						f = new person();
						f.unconstructed = true;
						f.CODE = p.getFatherCode();
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
		System.out.println("�H����(���t�d�l):"+Genealogy.keySet().size());
		build(Genealogy);
		System.out.println("lonely node : "+lonelyGenealogy.size());
	}
}
