package paramaterExtracter;

import java.util.ArrayList;

import analyzer.AnalyzerMethod;
import tracer.ValueOption;

public class ExtractMethod {

	private String methodName;
	private ArrayList<ValueOption> argumentLists = new ArrayList<ValueOption>();
	private ValueOption returnValueOption = null;
	private AnalyzerMethod analyzerMethod;
	
	public ExtractMethod(String s, AnalyzerMethod aM) {
		methodName = s;
		analyzerMethod = aM;
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
