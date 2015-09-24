package JPML;

import org.apache.poi.ss.usermodel.Row;

class wife extends person{

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
