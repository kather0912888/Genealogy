package JPML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class visualization {
	public static Document parse(String string) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(string);
		return document;
	}
		static enum GENDER{
			MALE,
			FEMALE
		}
	
	private static class node{
		String name;
		String familyName;
		GENDER gender = GENDER.MALE;
		ArrayList<String> wifes = new ArrayList<>();
		ArrayList<String> sons = new ArrayList<>();
		node father;
		node husband;
		boolean unconstructed=false;
	}
	
	static HashMap<String,node> map = new HashMap<>();

	private static node getNode(String id){
		if(map.get(id)==null){
			node n = new node();
			map.put(id, n);
			return n;
		}
		else
			return map.get(id);
	}
	
	public static void main(String[] args) throws DocumentException {
		Document document = parse("jpml.xml");
		Element root = document.getRootElement();

		// get people list
		Element e = (Element) root.elementIterator().next();
		// find the 4th
		Iterator temp = e.elementIterator();
		Element element = (Element) temp.next();
		element = (Element) temp.next();
		element = (Element) temp.next();
		element = (Element) temp.next();
		// got the people list.
		int times = 0;
		for (Iterator i = element.elementIterator(); i.hasNext();) {
			Element p =  (Element) i.next();
			String id = p.attributeValue("personID");
			// 創造node
			node n = getNode(id);
			
			// p is one people
			for(Iterator pi = p.elementIterator(); pi.hasNext();){
				Element pe = (Element) pi.next();
				// get name
				if(pe.getName() == "personName" || pe.getName().compareTo("personName")==0){
					n.name=pe.getStringValue();
//					System.out.println("!!!!"+pe.getStringValue()+"!!!!");
				}
				// get family name
				if(pe.getName() == "familyName" || pe.getName().compareTo("familyName")==0){
					n.familyName=pe.getStringValue();
					System.out.println("!!!!"+pe.getStringValue()+"!!!!");
//					System.out.println();
				}
				// get gender
				if(pe.getName() == "gender" || pe.getName().compareTo("gender")==0){
					if(pe.getStringValue()=="男" || pe.getStringValue().compareTo("男")==0)
						n.gender = GENDER.MALE;
					else
						n.gender = GENDER.FEMALE;
				}
				// get relations
				if(pe.getName()=="relations" || pe.getName().compareTo("relations")==0){
					for(Iterator r = pe.elementIterator(); r.hasNext();){
						Element re = (Element) r.next();
						if(re.getName()=="relative" || re.getName().compareTo("relative")==0){
							String relative_id = re.attributeValue("ID");
							// 關係
							String relative = re.attributeValue("kinrel");
							// 關係有 W F
							if(relative == "W" || relative.compareTo("W")==0){
								node w = getNode(relative_id);
								n.wifes.add(relative_id);
							}
							else{
								node F = getNode(relative_id);
								n.father = F;
							}
						}
						
						if(re.getName()=="is_a_relative_of" || re.getName().compareTo("is_a_relative_of")==0){
							String relative_id = re.attributeValue("ID");
							// 關係
							String relative = re.attributeValue("kinrel");
							// 關係有 W F
							if(relative == "W" || relative.compareTo("W")==0){
								//// 本人是老婆
//								System.out.println("gg");
								node h = getNode(relative_id);
								n.husband = h;
//								System.out.println("---"+h.name);
							}
							else{
								node s = getNode(relative_id);
								n.sons.add(relative_id);
							}
						}
					}
				}
			}
			times++;
			if(times==11)
				break;
		}
		check();
	}

	private static void check(){
		for(String k : map.keySet()){
			node n = getNode(k);
			if(n.name==null && n.familyName==null)
				continue;
			if(n.name!=null)
			System.out.println(n.name);
			else if(n.familyName!=null)
				System.out.println(n.familyName);
			System.out.println(k);
//			System.out.println(k.hashCode());
			if(n.gender == GENDER.MALE){
				if(n.father!=null){
					System.out.println("\t父親:"+n.father.name);
				}
				// 配偶
				for(String ws : n.wifes){
					System.out.println("\t"+ws);
//					System.out.println(ws.hashCode());
					node w = getNode(ws);
					System.out.println("\t老婆:"+w.familyName);
//					System.out.println(w.husband.name);
				}
				// 孩子
				for(String ss : n.sons){
					System.out.println("\t"+ss);
					node s = getNode(ss);
					System.out.println("\t兒子:"+s.name);
				}
			}
			else{
				// 配偶
				System.out.println("\t老公"+n.husband.name);
			}
		}
	}
}
