package breakDownPathExtracter;

import analyzer.AnalyzerVariable;
import tracer.ValueOption;

public class PutInstanceVariable {

	ValueOption targetInstanceValueOption = null;
	ValueOption putValueOption = null;
	AnalyzerVariable analyzerVariable = null;
	
	public PutInstanceVariable(AnalyzerVariable input, ValueOption target, ValueOption value) {
		analyzerVariable = input;
		targetInstanceValueOption = target;
		putValueOption = value;
	}
	public AnalyzerVariable getAnalyzerVariable() {
		return analyzerVariable;
	}
	
	public ValueOption getTargetInstanceValueOption() {
		return targetInstanceValueOption;
	}
	
	public ValueOption getPutValueOption() {
		return putValueOption;
	}
}
