package paramaterExtracter;

import java.util.ArrayList;

public class ExtractClass {

	private String ownerClass;
	private ArrayList<Instance> instanceLists = new ArrayList<Instance>();
	
	public ExtractClass(String oC) {
		ownerClass = oC;
	}
	
	public void addInstanceLists(Instance input) {
		instanceLists.add(input);
	}
	
	public String getOwnerClass() {
		return ownerClass;
	}
	
	public ArrayList<Instance> getInstanceLists(){
		return instanceLists;
	}
}
