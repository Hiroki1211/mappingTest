package paramaterExtracter;

import java.util.ArrayList;

import analyzer.AnalyzerMethod;
import tracer.ValueOption;

public class ExtractMethod {

	private String methodName;
	private int seqNum;
	private ArrayList<ValueOption> argumentLists = new ArrayList<ValueOption>();
	private ValueOption returnValueOption = null;
	private AnalyzerMethod analyzerMethod;
	
	public ExtractMethod(String s, AnalyzerMethod aM, int sN) {
		methodName = s;
		analyzerMethod = aM;
		seqNum = sN;
	}
	
	public void addArgumentLists(ValueOption input) {
		argumentLists.add(input);
	}
	
	public void setReturnValueOption(ValueOption input) {
		returnValueOption = input;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public int getSeqNum() {
		return seqNum;
	}
	
	public ArrayList<ValueOption> getArgmentLists(){
		return argumentLists;
	}
	
	public ValueOption getReturnValueOption() {
		return returnValueOption;
	}
	
	public AnalyzerMethod getAnalyzerMethod() {
		return analyzerMethod;
	}
}
