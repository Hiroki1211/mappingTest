package paramaterExtracter;

import java.util.ArrayList;

import breakDownPathExtracter.PutInstanceVariable;
import tracer.ValueOption;

public class Instance {

	private String id;
	private String ownerClass;
	private ArrayList<ExtractMethod> extractMethodLists = new ArrayList<ExtractMethod>();
	private ArrayList<PutInstanceVariable> putInstanceVariableLists = new ArrayList<PutInstanceVariable>();
	
	public Instance(String s, String oC) {
		id = s;
		ownerClass = oC;
	}
	
	public void display() {
		System.out.println(id);
		for(int i = 0; i < extractMethodLists.size(); i++) {
			System.out.print(extractMethodLists.get(i).getMethodName() + ":");
			ArrayList<ValueOption> argLists = extractMethodLists.get(i).getArgmentLists();
			for(int j = 0; j < argLists.size(); j++) {
				System.out.print(argLists.get(j).getValue() + ", ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public String getId() {
		return id;
	}
	
	public String getOwnerClass() {
		return ownerClass;
	}
	
	public ArrayList<ExtractMethod> getExtractMethodLists(){
		return extractMethodLists;
	}
	
	public ArrayList<PutInstanceVariable> getPutInstanceVariableLists(){
		return putInstanceVariableLists;
	}
	
	public void addExtractMethodLists(ExtractMethod input) {
		extractMethodLists.add(input);
	}
	
	public void addPutInstanceVariableLists(PutInstanceVariable input) {
		putInstanceVariableLists.add(input);
	}
}
