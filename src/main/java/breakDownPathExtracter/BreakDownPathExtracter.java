package breakDownPathExtracter;

import java.util.ArrayList;
import java.util.regex.Pattern;

import analyzer.Analyzer;
import analyzer.AnalyzerVariable;
import pathExtracter.PathExtracter;
import pathExtracter.TraceMethodBlock;
import testAnalyzer.Test;
import testAnalyzer.TestAnalyzer;
import testAnalyzer.TestClass;
import tracer.Trace;
import tracer.ValueOption;

public class BreakDownPathExtracter {

	public static void main(String[] args) {
		BreakDownPathExtracter breakDownPathExtracter = new BreakDownPathExtracter();
		
		ArrayList<String> inputFileNameLists = breakDownPathExtracter.getInputFileNameLists();
		ArrayList<String> inputTestFileNameLists = breakDownPathExtracter.getInputTestTestFileNameLists();
		ArrayList<String> inputTestTraceFileNameLists = breakDownPathExtracter.getInputTestTraceFileNameLists();
		
		// product code analyze
		Analyzer analyzer = new Analyzer();
		analyzer.run(inputFileNameLists);
		ArrayList<AnalyzerVariable> analyzerVariableLists = analyzer.getVariableLists();
		
		// test code analyze
		TestAnalyzer testAnalyzer = new TestAnalyzer();
		ArrayList<TestClass> testClassLists = testAnalyzer.run(inputTestFileNameLists, analyzer.getMethodLists());
		
		// Extract test Path
		PathExtracter pathExtracter = new PathExtracter();
		pathExtracter.testTestPathExtract(inputTestTraceFileNameLists, testClassLists);
		
		// method return value
		breakDownPathExtracter.extractMethodReturnValue(testClassLists);
		
		// put field variable
		breakDownPathExtracter.putFieldVariable(testClassLists, analyzerVariableLists);
		
	}
	
	public ArrayList<TestClass> run(Analyzer analyzer, ArrayList<String> inputTestFileNameLists, ArrayList<String> inputTestTraceFileNameLists){
		ArrayList<AnalyzerVariable> analyzerVariableLists = analyzer.getVariableLists();
		
		TestAnalyzer testAnalyzer = new TestAnalyzer();
		ArrayList<TestClass> testClassLists = testAnalyzer.run(inputTestFileNameLists, analyzer.getMethodLists());
		
		// Extract test Path
		PathExtracter pathExtracter = new PathExtracter();
		pathExtracter.testTestPathExtract(inputTestTraceFileNameLists, testClassLists);
		
		// method return value
		this.extractMethodReturnValue(testClassLists);
		
		// put field variable
		this.putFieldVariable(testClassLists, analyzerVariableLists);
		
		return testClassLists;
	}
	
	public void putFieldVariable(ArrayList<TestClass> classLists, ArrayList<AnalyzerVariable> analyzerVariableLists) {
		
		for(int classNum = 0; classNum < classLists.size(); classNum++) {
			TestClass testClass = classLists.get(classNum);
			
			ArrayList<Test> testLists = testClass.getTestLists();
			for(int testNum = 0; testNum < testLists.size(); testNum++) {
				Test test = testLists.get(testNum);
				
				// put Instance Variable
				ArrayList<TraceMethodBlock> traceMethodBlockLists = test.getTraceMethodBlockLists();
				for(int traceMethodBlockNum = 0; traceMethodBlockNum < traceMethodBlockLists.size(); traceMethodBlockNum++) {
					ArrayList<Trace> traceLists = traceMethodBlockLists.get(traceMethodBlockNum).getTraceLists();
					
					ValueOption targetInstanceValueOption = null;
					for(int traceNum = 0; traceNum < traceLists.size(); traceNum++) {
						Trace trace = traceLists.get(traceNum);
						
						if(trace.getEvent().equals("PUT_INSTANCE_FIELD")) {
							targetInstanceValueOption = trace.getValueOption();
							
						}else if(trace.getEvent().equals("PUT_INSTANCE_FIELD_VALUE")) {
							String[] splitOwner = trace.getAttr().getOwner().split(Pattern.quote("."));
							String owner = splitOwner[splitOwner.length-1];
							String variableName = trace.getAttr().getName();
							
							AnalyzerVariable analyzerVariable = null;
							for(int analyzerVariableNum = 0; analyzerVariableNum < analyzerVariableLists.size(); analyzerVariableNum++) {
								if(analyzerVariableLists.get(analyzerVariableNum).getName().equals(variableName) && analyzerVariableLists.get(analyzerVariableNum).getOwnerClass().getName().equals(owner)) {
									analyzerVariable = analyzerVariableLists.get(analyzerVariableNum);
								}
							}
							
							ValueOption putValueOption = trace.getValueOption();
							PutInstanceVariable putInstanceVariable = new PutInstanceVariable(analyzerVariable, targetInstanceValueOption, putValueOption);
							test.addPutInstanceVaiableLists(putInstanceVariable);
							
//							trace.displayContent();
//							analyzerVariable.display();

						}
						
					}
					
				}
				
			}
			
		}
		
	}
	
	public void extractMethodReturnValue(ArrayList<TestClass> classLists) {
		for(int classNum = 0; classNum < classLists.size(); classNum++) {
			TestClass testClass = classLists.get(classNum);
			for(int testNum = 0; testNum < testClass.getTestLists().size(); testNum++) {
				Test test = testClass.getTestLists().get(testNum);
				
//				for(int i = 0; i < test.getMethodLists().size(); i++) {
//					test.getMethodLists().get(i).getAnalyzerMethod().display();
//				}
				
				int methodNum = 0;
				for(int traceNum = 0; traceNum < test.getTraceMethodBlock().getTraceLists().size(); traceNum++) {
					Trace trace = test.getTraceMethodBlock().getTraceLists().get(traceNum);
					//trace.displayContent();
					
					if(trace.getEvent().equals("CALL_RETURN")) {
						test.getMethodLists().get(methodNum).setReturnValueOption(trace.getValueOption());
						
						methodNum += 1;
					}
				}
			}
			
		}
		
	}
	
	private ArrayList<String> getInputFileNameLists(){
		ArrayList<String> result = new ArrayList<String>();
		result.add("src/main/resources/ex06/code/Human.java");
		result.add("src/main/resources/ex06/code/Executer.java");
		
		return result;
	}
	
	private ArrayList<String> getInputTestTestFileNameLists(){
		ArrayList<String> result = new ArrayList<String>();
		result.add("src/main/resources/ex06/breakDownTest/Human_Test.java");
		result.add("src/main/resources/ex06/breakDownTest/Executer_Test.java");
		
		return result;
	}
	
	private ArrayList<String> getInputTestTraceFileNameLists(){
		ArrayList<String> result = new ArrayList<String>();
		result.add("src/main/resources/ex06/breakDownTestTrace/Human_Test.json");
		result.add("src/main/resources/ex06/breakDownTestTrace/Executer_Test.json");
		
		return result;
	}
	
}
