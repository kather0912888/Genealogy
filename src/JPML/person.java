package JPML;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

class person implements Comparable<person>{
	static int personID = 1;
	// 索引標籤
	static HashMap<String,Integer> dataIndex = new HashMap<>();
	// 索引資料
	public static Row indexRow;
	// 仍未建立的點
	boolean unconstructed = false;
	String CODE="";
	// 是否衝突(兩人共享同code)
	boolean conflict = false;
	// 祖先是否衝突
	boolean ancientConflict = false;
	// 若衝突是否已經輸出至檔案?
	boolean output = false;

	int personid;
	Row data;
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
		if(dataIndex.containsKey(input)){
			try{
				data.getCell(dataIndex.get(input)).setCellType(Cell.CELL_TYPE_STRING);
				return data.getCell(dataIndex.get(input)).getStringCellValue();
			}
			catch(Exception e){
				return "";
			}
		
		}
		
		return "";
	}
	// constructor
	public person(Row r){
		this.personid = personID++;
		this.data = r;
	}
	public person(){
		this.personid = personID++;
		this.unconstructed = true;
	}
	public boolean isAcestor(){
		return (this.get("an")=="y" || this.get("an").compareTo("y")==0);
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
	
	private static boolean isDigit(char c) {
		if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8'
				|| c == '9')
			return true;
		return false;
	}	

}
