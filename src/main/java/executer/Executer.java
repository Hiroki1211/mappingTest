package executer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import analyzer.Analyzer;
import analyzer.AnalyzerVariable;
import breakDownPathExtracter.BreakDownPathExtracter;
import breakDownPathExtracter.PutInstanceVariable;
import pathExtracter.PathExtracter;
import pathExtracter.TraceMethodBlock;
import testAnalyzer.Test;
import testAnalyzer.TestAssertion;
import testAnalyzer.TestClass;
import testAnalyzer.TestMethod;
import tracer.Trace;
import tracer.ValueOption;

public class Executer {

	public static void main(String args[]) {
		Executer executer = new Executer();
		
		ArrayList<String> inputFileNameLists = executer.getInputFileNameLists();
		ArrayList<String> inputEvoSuiteTestFileNameLists = executer.getInputEvoSuiteTestFileNameLists();
		ArrayList<String> inputEvoSuiteTestTraceFileNameLists = executer.getInputEvoSuiteTestTraceFileNameLists();
		ArrayList<String> inputBreakDownTestFileNameLists = executer.getInputBreakDownTestFileNameLists();
		ArrayList<String> inputBreakDownTestTraceFileNameLists = executer.getInputBreakDownTestTraceFileNameLists();
		
		// 1. product code analyze
		Analyzer analyzer = new Analyzer();
		analyzer.run(inputFileNameLists);
		
		// 2. evosuite test
		PathExtracter pathExtracter = new PathExtracter();
		ArrayList<TestClass> evoSuiteTestClassLists = pathExtracter.run(analyzer, inputEvoSuiteTestFileNameLists, inputEvoSuiteTestTraceFileNameLists);
		
		// 3. breakDown test
		BreakDownPathExtracter breakDownPathExtracter = new BreakDownPathExtracter();
		ArrayList<TestClass> breakDownTestClassLists = breakDownPathExtracter.run(analyzer, inputBreakDownTestFileNameLists, inputBreakDownTestTraceFileNameLists);
		
		// 4. analyze extract path 
		executer.analyzeExtractPath(evoSuiteTestClassLists);
		executer.analyzeExtractPath(breakDownTestClassLists);
		
		// 5. summarize same extract method
		executer.summarizeSameExecutePath(evoSuiteTestClassLists);
		executer.summarizeSameExecutePath(breakDownTestClassLists);
		
		// 6. matching same extecute path test ( brekDownTest vs evoSuite )
		ArrayList<Result> resultLists = executer.matchingSameExecutePathTest(evoSuiteTestClassLists, breakDownTestClassLists);
		ArrayList<NaturalTest> naturalTestLists = executer.createNaturalTest(resultLists, analyzer);
		
		// 7. export result
		executer.createExternalFile(naturalTestLists);
		executer.createEx01ResultExternalFIle(breakDownTestClassLists);
		executer.createEx02ResultExternalFile(resultLists);
		executer.createEx03ResultExternalFile(resultLists);
	}
	
	private void createEx01ResultExternalFIle(ArrayList<TestClass> breakDownTestClassLists) {
		String directoryPathName = "src/test/java/naturalTest/";
		File file = new File(directoryPathName + "ex01.txt");
		file.setReadable(true);
		file.setWritable(true);
		
		try {
			FileWriter fw = new FileWriter(file);
			
			for(int classNum = 0; classNum < breakDownTestClassLists.size(); classNum++) {
				TestClass breakDownTestClass = breakDownTestClassLists.get(classNum);
				ArrayList<Test> testLists = breakDownTestClass.getTestLists();
				ArrayList<TestMethod> methodLists = new ArrayList<TestMethod>();
				
				for(int testNum = 0; testNum < testLists.size(); testNum++) {
					Test test = testLists.get(testNum);
					ArrayList<TestMethod> testMethodLists = test.getMethodLists();
					
					for(int methodNum = 0; methodNum < testMethodLists.size(); methodNum++) {
						TestMethod testMethod = testMethodLists.get(methodNum);
						
						if(methodLists.size() == 0) {
							methodLists.add(testMethod);
						}else {
							boolean isContain = false;
							for(int i = 0; i < methodLists.size(); i++) {
								if(this.isSameTestMethod(testMethod, methodLists.get(i))) {
									isContain = true;
									break;
								}
							}
							
							if(!isContain) {
								methodLists.add(testMethod);
							}
						}
					}
				}
				
				for(int methodNum = 0; methodNum < methodLists.size(); methodNum++) {
					TestMethod testMethod = methodLists.get(methodNum);
					
					String addFw = breakDownTestClass.getClassName() + "\t" + testMethod.getMethodName() + "\t";
					for(int argNum = 0; argNum < testMethod.getArgumentLists().size(); argNum++) {
						addFw += testMethod.getArgumentLists().get(argNum) + "\t";
					}
					
					fw.write(addFw + "\n");
				}
				
			}
			
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createEx02ResultExternalFile(ArrayList<Result> resultLists) {
		String directoryPathName = "src/test/java/naturalTest/";
		File file = new File(directoryPathName + "ex02.txt");
		file.setReadable(true);
		file.setWritable(true);
		try {
			FileWriter fw = new FileWriter(file);
			for(int resultNum = 0; resultNum < resultLists.size(); resultNum++) {
				Result result = resultLists.get(resultNum);
				
				int evoSuiteTestNum = result.getEvoSuiteTestClass().getTestLists().size();
				
				ArrayList<MatchingResult> matchLists = result.getMatchingResultLists();
				int matchEvoSuiteTestNum = 0;
				for(int matchNum = 0; matchNum < matchLists.size(); matchNum++) {
					MatchingResult matchingResult = matchLists.get(matchNum);
					matchEvoSuiteTestNum += matchingResult.getEvoSuiteSamePathExecutePath().getTestLists().size();
				}
				
				ArrayList<SameExecutePath> notMatchEvoSuiteTestLists = result.getNotMatchingEvoSuiteLists();
				int notMatchEvoSuiteTestNum = 0;
				for(int notMatchNum = 0; notMatchNum < notMatchEvoSuiteTestLists.size(); notMatchNum++) {
					notMatchEvoSuiteTestNum += notMatchEvoSuiteTestLists.get(notMatchNum).getTestLists().size();
				}
				
				int breakDownTestNum = result.getBreakDownTestClass().getTestLists().size();
				
				int matchBreakDownTestNum = 0;
				for(int matchNum = 0; matchNum < matchLists.size(); matchNum++) {
					MatchingResult matchingResult = matchLists.get(matchNum);
					matchBreakDownTestNum += matchingResult.getBreakDownSamePathExecutePath().getTestLists().size();
				}
				
				ArrayList<SameExecutePath> notMatchBreakDownTestLists = result.getNotMatchingBreakDownLists();
				int notMatchBreakDownTestNum = 0;
				for(int notMatchNum = 0; notMatchNum < notMatchBreakDownTestLists.size(); notMatchNum++) {
					notMatchBreakDownTestNum += notMatchBreakDownTestLists.get(notMatchNum).getTestLists().size();
				}
				
				fw.write(result.getEvoSuiteTestClass().getClassName() + 
						"\t" + evoSuiteTestNum + 
						"\t" + matchEvoSuiteTestNum + 
						"\t" + notMatchEvoSuiteTestNum +
						"\t" + breakDownTestNum + 
						"\t" + matchBreakDownTestNum + 
						"\t" + notMatchBreakDownTestNum);
				fw.write("\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	private void createEx03ResultExternalFile(ArrayList<Result> resultLists) {
		String directoryPathName = "src/test/java/naturalTest/";
		File file = new File(directoryPathName + "ex03.txt");
		file.setReadable(true);
		file.setWritable(true);
		
		try {
			FileWriter fw = new FileWriter(file);
			for(int resultNum = 0; resultNum < resultLists.size(); resultNum++) {
				Result result = resultLists.get(resultNum);
				ArrayList<MatchingResult> matchLists = result.getMatchingResultLists();
				for(int matchNum = 0; matchNum < matchLists.size(); matchNum++) {
					MatchingResult matchingResult = matchLists.get(matchNum);
					
					Test breakDownTest = matchingResult.getBreakDownSamePathExecutePath().getTestLists().get(0);
					
					for(int methodNum = 0; methodNum < breakDownTest.getMethodLists().size(); methodNum++) {
						ArrayList<Test> evoSuiteTestLists = matchingResult.getEvoSuiteSamePathExecutePath().getTestLists();
						for(int testNum = 0; testNum < evoSuiteTestLists.size(); testNum++) {
							Test evoSuiteTest = evoSuiteTestLists.get(testNum);
								
							TestMethod evoSuiteTestMethod = evoSuiteTest.getMethodLists().get(methodNum);
							
							String evoSuiteLine = result.getEvoSuiteTestClass().getClassName() + "\t" + evoSuiteTest.getMethodName() + "\t" + evoSuiteTestMethod.getMethodName() + "\t" + "EvoSuite" + "\t";
							for(int argNum = 0; argNum < evoSuiteTestMethod.getArgumentLists().size(); argNum++) {
								evoSuiteLine += evoSuiteTestMethod.getArgumentLists().get(argNum) + "\t";
							}
							fw.write(evoSuiteLine + "\n");
						}					
						
						TestMethod breakDownTestMethod = breakDownTest.getMethodLists().get(methodNum);
						
						String breakDownLine = result.getEvoSuiteTestClass().getClassName() + "\t" + breakDownTest.getMethodName() + "\t" + breakDownTestMethod.getMethodName() + "\t" + "breakDown" + "\t";
						for(int argNum = 0; argNum < breakDownTestMethod.getArgumentLists().size(); argNum++) {
							breakDownLine += breakDownTestMethod.getArgumentLists().get(argNum) + "\t";
						}
						fw.write(breakDownLine + "\n");
					}
				}
			}
			
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private boolean isSameTestMethod(TestMethod testMethod1, TestMethod testMethod2) {
		if(!testMethod1.getMethodName().equals(testMethod2.getMethodName())) {
			return false;
		}
		
		ArrayList<String> argumentLists1 = testMethod1.getArgumentLists();
		ArrayList<String> argumentLists2 = testMethod2.getArgumentLists();
		
		if(argumentLists1.size() != argumentLists2.size()) {
			return false;
		}
		
		for(int argNum = 0; argNum < argumentLists1.size(); argNum++) {
			if(!argumentLists1.get(argNum).equals(argumentLists2.get(argNum))) {
				return false;
			}
		}
		
		return true;
	}
	
	private void createExternalFile(ArrayList<NaturalTest> naturalTestLists) {
		String directoryPathName = "src/test/java/naturalTest";
		File dir = new File(directoryPathName);
		dir.mkdir();
		
		for(int naturalTestNum = 0; naturalTestNum < naturalTestLists.size(); naturalTestNum++) {
			NaturalTest naturalTest = naturalTestLists.get(naturalTestNum);
			
			File file = new File(directoryPathName + "/" + naturalTest.getClassName() + ".java");
			file.setExecutable(true);
			file.setReadable(true);
			file.setWritable(true);
			
			try {
				FileWriter fw = new FileWriter(file);
				ArrayList<String> contents = naturalTest.getContent();
				for(int contentNum = 0; contentNum < contents.size(); contentNum++) {
					fw.write(contents.get(contentNum) + "\n");
				}
				
				fw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private ArrayList<NaturalTest> createNaturalTest(ArrayList<Result> resultLists, Analyzer analyzer) {
		ArrayList<NaturalTest> naturalTestLists = new ArrayList<NaturalTest>();
		
		for(int resultNum = 0; resultNum < resultLists.size(); resultNum++) {
			Result result = resultLists.get(resultNum);
			TestClass evoSuiteTestClass = result.getEvoSuiteTestClass();
			NaturalTest naturalTest = new NaturalTest(evoSuiteTestClass.getClassName().replace("_ESTest", "") + "_NaturalTest", evoSuiteTestClass.getImportLists());
			naturalTestLists.add(naturalTest);
			
			//  add matching test
			ArrayList<MatchingResult> matchingResultLists = result.getMatchingResultLists();
			for(int matchNum = 0; matchNum < matchingResultLists.size(); matchNum++) {
				MatchingResult matchingResult = matchingResultLists.get(matchNum);
				
				Test evoSuiteTest = matchingResult.getEvoSuiteSamePathExecutePath().getTestLists().get(0);
				Test breakDownTest = matchingResult.getBreakDownSamePathExecutePath().getTestLists().get(0);
				
//				System.out.println("-------");
//				evoSuiteTest.display();
//				breakDownTest.display();
				
				Test addNaturalTest = new Test();
				String methodName = "naturalTest" + matchNum;
				addNaturalTest.setMethodName(methodName);
				
				// create natural method
				ArrayList<TestMethod> evoSuiteTestMethodLists = evoSuiteTest.getMethodLists(); 
				ArrayList<TestMethod> breakDownTestMethodLists = breakDownTest.getMethodLists();
				for(int evoTestMethodNum = 0; evoTestMethodNum < evoSuiteTestMethodLists.size(); evoTestMethodNum++) {
					TestMethod evoSuiteTestMethod = evoSuiteTestMethodLists.get(evoTestMethodNum);
					TestMethod breakDownTestMethod = breakDownTestMethodLists.get(evoTestMethodNum);
					TestMethod addNaturalTestMethod = this.createNaturalMethod(evoSuiteTestMethod, breakDownTestMethod);
					addNaturalTest.addMethodLists(addNaturalTestMethod);	
				}
				
				ArrayList<TestAssertion> evoSuiteTestAssertionLists = evoSuiteTest.getAssertionLists();
				// assertion
				for(int evoTestAssertNum = 0; evoTestAssertNum < evoSuiteTestAssertionLists.size(); evoTestAssertNum++){
					TestAssertion evoSuiteTestAssertion = evoSuiteTestAssertionLists.get(evoTestAssertNum);
					TestAssertion naturalTestAssertion = this.createNaturalAssertion(evoSuiteTestAssertion, evoSuiteTestMethodLists, breakDownTestMethodLists, evoSuiteTest, breakDownTest, analyzer, evoSuiteTestClass);
					
					addNaturalTest.addAssertionLists(naturalTestAssertion);
				}
				
				naturalTest.addNaturalTestLists(addNaturalTest);
				
			}
			
			ArrayList<SameExecutePath> notMatchEvoSuiteSameExecutePathLists = result.getNotMatchingEvoSuiteLists();
			for(int notEvoSameNum = 0; notEvoSameNum < notMatchEvoSuiteSameExecutePathLists.size(); notEvoSameNum++) {
				SameExecutePath notMatchEvoSuiteSameExecutePath = notMatchEvoSuiteSameExecutePathLists.get(notEvoSameNum);
				ArrayList<Test> notMatchEvoSuiteTestLists = notMatchEvoSuiteSameExecutePath.getTestLists();
				
				for(int notMatchTestNum = 0; notMatchTestNum < notMatchEvoSuiteTestLists.size(); notMatchTestNum++) {
					Test notMatchEvoSuiteTest = notMatchEvoSuiteTestLists.get(notMatchTestNum);
					
					naturalTest.addEvoSuiteTestLists(notMatchEvoSuiteTest);
				}
				
			}

			naturalTest.createContent();
		}
		
		return naturalTestLists;
	}
	
	private TestAssertion createNaturalAssertion(TestAssertion evoSuiteTestAssertion, ArrayList<TestMethod> evoSuiteTestMethodLists, ArrayList<TestMethod> breakDownTestMethodLists, Test evoSuiteTest, Test breakDownTest, Analyzer analyzer, TestClass evoSuiteTestClass) {
		ValueOption valueOption = null;
		
		if(!evoSuiteTestAssertion.getVariable().equals("")) {
			TestMethod targetEvoSuiteMethod = evoSuiteTestAssertion.getAssertionTargetMethod();
			int index = evoSuiteTestMethodLists.indexOf(targetEvoSuiteMethod);
			valueOption = breakDownTestMethodLists.get(index).getReturnValueOption();
					
		}else {
			String getterMethodInstance = evoSuiteTestAssertion.getGetterMethodInstance();
			int testMethodNum = 0;
			
			for(int evoTestMethodNum = 0; evoTestMethodNum < evoSuiteTestMethodLists.size(); evoTestMethodNum++) {
				TestMethod evoSuiteTestMethod = evoSuiteTestMethodLists.get(evoTestMethodNum);
				
				if(evoSuiteTestMethod.getReturnVariable().equals(getterMethodInstance)) {
					testMethodNum = evoTestMethodNum;
					break;
				}
			}
			
			TestMethod targetInstanceCreateBreakDownTest = breakDownTestMethodLists.get(testMethodNum);
			
			String instanceClass = targetInstanceCreateBreakDownTest.getConstructorClass();
			TraceMethodBlock traceMethodBlock = breakDownTest.getTraceMethodBlock();
			ArrayList<Trace> traceLists = traceMethodBlock.getTraceLists();
			
			int initNum = 0;
			boolean targetFlag = false;
			ValueOption instanceValueOption = null;
			for(int traceNum = 0; traceNum < traceLists.size(); traceNum++) {
				Trace trace = traceLists.get(traceNum);
				
				if(trace.getEvent().equals("CALL") && trace.getAttr().getName().equals("<init>")) {
					String[] split = trace.getAttr().getOwner().split(Pattern.quote("."));
					String className = split[split.length - 1];
					if(className.equals(instanceClass)) {
						char firstChar = className.charAt(0);
						firstChar = Character.toLowerCase(firstChar);
						String variableName = String.valueOf(firstChar) + className.substring(1, className.length()) + String.valueOf(initNum);
						
						if(evoSuiteTestAssertion.getGetterMethodInstance().equals(variableName)) {
							targetFlag = true;
						}else {
							initNum += 1;
						}
					}
				}if(targetFlag && trace.getEvent().equals("NEW_OBJECT_CREATED")) {
					instanceValueOption = trace.getValueOption();
					break;
				}
			}
			
			String instanceId = instanceValueOption.getId();
			String getterMethodName = evoSuiteTestAssertion.getGetterMethodName();
			ArrayList<AnalyzerVariable> analyzerVariableLists = analyzer.getVariableLists();
			AnalyzerVariable analyzerVariable = null;
			for(int analyzerVarNum = 0; analyzerVarNum < analyzerVariableLists.size(); analyzerVarNum++) {
				analyzerVariable = analyzerVariableLists.get(analyzerVarNum);
				if(analyzerVariable.getGetterMethod().getName().equals(getterMethodName) && analyzerVariable.getOwnerClass().getName().equals(evoSuiteTestClass.getClassName().replace("_ESTest", ""))) {
					break;
				}
			}
			
//			System.out.println(getterMethodName);
//			System.out.println(analyzerVariable.getGetterMethod().getName());
			
			PutInstanceVariable putInstanceVariable = null;
			ArrayList<PutInstanceVariable> putInstanceVariableLists = breakDownTest.getPutInstanceVariableLists();
			for(int putNum = putInstanceVariableLists.size() - 1; putNum > 0; putNum--) {
				putInstanceVariable = putInstanceVariableLists.get(putNum);
				
				if(putInstanceVariable.getAnalyzerVariable().getName().equals(analyzerVariable.getName()) && putInstanceVariable.getTargetInstanceValueOption().getId().equals(instanceId)) {
					break;
				}
			}
			
			System.out.println(breakDownTest.getMethodName());
			System.out.println(putInstanceVariableLists.size());
			valueOption = putInstanceVariable.getPutValueOption();
			
		}
		
		String assertValue = valueOption.getValue();
		if(!assertValue.contains("\"") && !assertValue.contains("'")) {
			if(assertValue.contains(".")) {
				assertValue += "f";
			}
		}
		
		String assertVariable = evoSuiteTestAssertion.getVariable();
		String assertGetterMethodInstance = evoSuiteTestAssertion.getGetterMethodInstance();
		String assertGetterMethodName = evoSuiteTestAssertion.getGetterMethodName();
		ArrayList<String> assertGetterMethodArgumentLists = evoSuiteTestAssertion.getGetterMethodArgument();
		TestMethod assertAssertionTargetMethod = evoSuiteTestAssertion.getAssertionTargetMethod();
		String assertStatement = "      assertEquals(" + assertValue + ", ";
		if(!assertVariable.equals("")) {
			assertStatement = assertStatement + assertVariable + ");";
		}else {
			assertStatement = assertStatement + assertGetterMethodInstance + "." + assertGetterMethodName + "(";
			
			if(assertGetterMethodArgumentLists.size() == 0) {
				assertStatement = assertStatement + ");";
			}else {
				for(int argNum = 0; argNum < assertGetterMethodArgumentLists.size(); argNum++) {
					if(argNum == assertGetterMethodArgumentLists.size() - 1) {
						assertStatement = assertStatement + assertGetterMethodArgumentLists.get(argNum) + ");";
					}else {
						assertStatement = assertStatement + assertGetterMethodArgumentLists.get(argNum) + ", ";
					}
				}
			}
		}
		
		TestAssertion addTestAssertion = new TestAssertion(assertValue, assertVariable, assertGetterMethodInstance, assertGetterMethodName, assertGetterMethodArgumentLists, assertAssertionTargetMethod, assertStatement);
		return addTestAssertion;
	}
	
	private TestMethod createNaturalMethod(TestMethod evoSuiteTestMethod, TestMethod breakDownTestMethod) {
		String statement = "      ";
		if(!evoSuiteTestMethod.getReturnType().equals("")) {
			statement = statement + evoSuiteTestMethod.getReturnType() + " " + evoSuiteTestMethod.getReturnVariable() + " = ";
		}
		
		if(!evoSuiteTestMethod.getInstance().equals("")) {
			statement = statement + evoSuiteTestMethod.getInstance() + "." + evoSuiteTestMethod.getMethodName() + "(";
		}else {
			statement = statement + "new " + evoSuiteTestMethod.getConstructorClass() + "(";
		}
		
		if(breakDownTestMethod.getArgumentLists().size() == 0) {
			statement = statement + ");";
		}else {
			for(int argNum = 0; argNum < breakDownTestMethod.getArgumentLists().size(); argNum++) {
				if(argNum == breakDownTestMethod.getArgumentLists().size() - 1) {
					statement = statement + breakDownTestMethod.getArgumentLists().get(argNum) + ");";
				}else {
					statement = statement + breakDownTestMethod.getArgumentLists().get(argNum) + ", ";
				}
			}
		}
		
		TestMethod naturalTestMethod = new TestMethod(statement, evoSuiteTestMethod.getMethodName());
		return naturalTestMethod;
	}
	
	private ArrayList<Result> matchingSameExecutePathTest(ArrayList<TestClass> evoSuiteTestClassLists, ArrayList<TestClass> breakDownTestClassLists) {
		ArrayList<Result> resultLists = new ArrayList<Result>();
		
		for(int evoSuiteTestClassNum = 0; evoSuiteTestClassNum < evoSuiteTestClassLists.size(); evoSuiteTestClassNum++) {
			TestClass evoSuiteTestClass = evoSuiteTestClassLists.get(evoSuiteTestClassNum);
			TestClass breakDownTestClass = null;
			
			for(int breakDownTestClassNum = 0; breakDownTestClassNum < breakDownTestClassLists.size(); breakDownTestClassNum++) {
				if(evoSuiteTestClass.getClassName().replace("_ESTest", "").equals(breakDownTestClassLists.get(breakDownTestClassNum).getClassName().replace("_Test", ""))) {
					breakDownTestClass = breakDownTestClassLists.get(breakDownTestClassNum);
				}
			}
			
			Result result = new Result(evoSuiteTestClass, breakDownTestClass);
			resultLists.add(result);
			
			ArrayList<SameExecutePath> registeredBreakDownSameExecutePathLists = new ArrayList<SameExecutePath>();
			
			if(breakDownTestClass == null) {
				for(int evoSuiteExecutePathNum = 0; evoSuiteExecutePathNum < evoSuiteTestClass.getSameExecutePathLists().size(); evoSuiteExecutePathNum++) {
					SameExecutePath sameExecutePath = evoSuiteTestClass.getSameExecutePathLists().get(evoSuiteExecutePathNum);
					result.addNotMatchingEvoSuiteLists(sameExecutePath);
				}
				
			}else {
				ArrayList<SameExecutePath> evoSuiteSameExecutePathLists = evoSuiteTestClass.getSameExecutePathLists();
				ArrayList<SameExecutePath> breakDownSameExecutePathLists = breakDownTestClass.getSameExecutePathLists();
				
				for(int evoSuiteSameExecutePathNum = 0; evoSuiteSameExecutePathNum < evoSuiteSameExecutePathLists.size(); evoSuiteSameExecutePathNum++) {
					SameExecutePath evoSuiteSameExecutePath = evoSuiteSameExecutePathLists.get(evoSuiteSameExecutePathNum);
					boolean isMatch = false;
					
					for(int breakDownSameExecutePathNum = 0; breakDownSameExecutePathNum < breakDownSameExecutePathLists.size(); breakDownSameExecutePathNum++) {
						SameExecutePath breakDownSameExecutePath = breakDownSameExecutePathLists.get(breakDownSameExecutePathNum);
						
						if(this.isSameExecutePath(evoSuiteSameExecutePath.getTestLists().get(0), breakDownSameExecutePath.getTestLists().get(0))) {
							MatchingResult matchingResult = new MatchingResult(evoSuiteSameExecutePath, breakDownSameExecutePath);
							result.addMatchingResultLists(matchingResult);
							isMatch = true;
							registeredBreakDownSameExecutePathLists.add(breakDownSameExecutePath);
							break;
						}
					}
					
					if(!isMatch) {
						result.addNotMatchingEvoSuiteLists(evoSuiteSameExecutePath);
					}
				}
				
				for(int breakDownSameExecutePathNum = 0; breakDownSameExecutePathNum < breakDownSameExecutePathLists.size(); breakDownSameExecutePathNum++) {
					SameExecutePath breakDownSameExecutePath = breakDownSameExecutePathLists.get(breakDownSameExecutePathNum);
					if(!registeredBreakDownSameExecutePathLists.contains(breakDownSameExecutePath)) {
						result.addNotMatchingBreakDownLists(breakDownSameExecutePath);
					}
					
				}
			}
		}
		
		return resultLists;
	}
	
	// TraceMethodBlockから実行経路を解析する
	private void analyzeExtractPath(ArrayList<TestClass> testClassLists) {
		for(int testClassNum = 0; testClassNum < testClassLists.size(); testClassNum++) {
			TestClass testClass = testClassLists.get(testClassNum);
//			System.out.println(testClass.getClassName());
			
			ArrayList<Test> testLists = testClass.getTestLists();
			for(int testNum = 0; testNum < testLists.size(); testNum++) {
				Test test = testLists.get(testNum);
				
				ArrayList<TraceMethodBlock> traceMethodBlockLists = test.getTraceMethodBlockLists();
				for(int traceMethodBlockNum = 0; traceMethodBlockNum < traceMethodBlockLists.size(); traceMethodBlockNum++) {
					ArrayList<Integer> lineLists = new ArrayList<Integer>();
					TraceMethodBlock traceMethodBlock = traceMethodBlockLists.get(traceMethodBlockNum);
					ArrayList<Trace> traceLists = traceMethodBlock.getTraceLists();
					
					for(int traceNum = 0; traceNum < traceLists.size(); traceNum++) {
						Trace trace = traceLists.get(traceNum);
						
						if(lineLists.size() == 0) {
							lineLists.add(trace.getLine());
						}else {
							if(!lineLists.contains(trace.getLine())) {
								lineLists.add(trace.getLine());
							}
						}
					}
					
					ExecutePath extractPath = new ExecutePath(lineLists, traceLists.get(0).getMname(), traceLists.get(0).getFilename());
					test.addExtractPathLists(extractPath);
					
//					System.out.println(test.getMethodName());
//					System.out.println(traceLists.get(0).getMname());
//					System.out.println(lineLists);
//					System.out.println();
				}
			}
		}
	}
	
	// 同じ実行経路のテストをまとめる
	private void summarizeSameExecutePath(ArrayList<TestClass> testClassLists) {
		
		for(int testClassNum = 0; testClassNum < testClassLists.size(); testClassNum++) {
			ArrayList<SameExecutePath> sameExecutePathLists = new ArrayList<SameExecutePath>();
			TestClass testClass = testClassLists.get(testClassNum);
			
			ArrayList<Test> testLists = testClass.getTestLists();
			for(int testNum = 0; testNum < testLists.size(); testNum++) {
				Test test = testLists.get(testNum);
				
				if(sameExecutePathLists.size() == 0) {
					SameExecutePath sameExecutePath = new SameExecutePath();
					sameExecutePath.addTestLists(test);
					sameExecutePathLists.add(sameExecutePath);
					
				}else {
					boolean registeredFlag = false;
					for(int sameExecutePathNum = 0; sameExecutePathNum < sameExecutePathLists.size(); sameExecutePathNum++) {
						if(this.isSameExecutePath(test, sameExecutePathLists.get(sameExecutePathNum).getTestLists().get(0))) {
							sameExecutePathLists.get(sameExecutePathNum).addTestLists(test);
							registeredFlag = true;
							break;
						}
					}
					
					if(!registeredFlag) {
						SameExecutePath sameExtractPath = new SameExecutePath();
						sameExtractPath.addTestLists(test);
						sameExecutePathLists.add(sameExtractPath);
					}
				}
			}
			
			testClass.setSameExecutePathLists(sameExecutePathLists);
		
//			System.out.println("##########");
//			System.out.println(testClass.getClassName());
//			for(int i = 0; i < sameExecutePathLists.size(); i++) {
//				SameExecutePath x = sameExecutePathLists.get(i);
//				for(int j = 0; j < x.getTestLists().size(); j++) {
//					System.out.println(x.getTestLists().get(j).getMethodName());
//				}
//				System.out.println();
//			}
//			System.out.println();
		}
	}
	
	private boolean isSameExecutePath(Test test1, Test test2) {
		ArrayList<ExecutePath> pathLists1 = test1.getExtractPathLists();
		ArrayList<ExecutePath> pathLists2 = test2.getExtractPathLists();
		
		if(pathLists1.size() != pathLists2.size()) {
			return false;
		}
		
		for(int pathNum = 0; pathNum < pathLists1.size(); pathNum++) {
			ExecutePath executePath1 = pathLists1.get(pathNum);
			ExecutePath executePath2 = pathLists2.get(pathNum);
			
			if(!(executePath1.getMname().equals(executePath2.getMname()) && executePath1.getFileName().equals(executePath2.getFileName()))) {
				return false;
			}
			
			ArrayList<Integer> lineLists1 = executePath1.getLineLists();
			ArrayList<Integer> lineLists2 = executePath2.getLineLists();
			
			if(lineLists1.size() != lineLists2.size()) {
				return false;
			}
			
			for(int lineNum = 0; lineNum < lineLists1.size(); lineNum++) {
				if(lineLists1.get(lineNum) != lineLists2.get(lineNum)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private ArrayList<String> getInputFileNameLists(){
		String path = "src/main/java/";
		ArrayList<String> result = new ArrayList<String>();
		
		File dir = new File(path);
		File[] files = dir.listFiles();
		
		ArrayList<String> filePathLists = new ArrayList<String>();
		
		for(int i = 0; i < files.length; i++) {
			String filePath = files[i].getPath();
			
			if(!filePath.contains(".java") && !filePath.contains(".class")) {
				filePathLists.add(filePath);
			}
		}
		
		while(filePathLists.size() > 0) {
			File pathDir = new File(filePathLists.get(0));
			filePathLists.remove(0);
			
			File[] pathDirFiles = pathDir.listFiles();
			
			for(int i = 0; i < pathDirFiles.length; i++) {
				String pathFilePath = pathDirFiles[i].getPath();
				
				if(pathFilePath.contains(".java")) {
					result.add(pathFilePath);
				}else if(!pathFilePath.contains(".class")){
					filePathLists.add(pathFilePath);
				}
			}
			
		}

		return result;
	}
	
	private ArrayList<String> getInputEvoSuiteTestFileNameLists(){
		String path = "src/test/java/";
		ArrayList<String> result = new ArrayList<String>();
		
		File dir = new File(path);
		File[] files = dir.listFiles();
		
		ArrayList<String> filePathLists = new ArrayList<String>();
		
		for(int i = 0; i < files.length; i++) {
			String filePath = files[i].getPath();
			
			if(!filePath.contains(".java") && !filePath.contains(".class")) {
				filePathLists.add(filePath);
			}
		}
		
		while(filePathLists.size() > 0) {
			File pathDir = new File(filePathLists.get(0));
			filePathLists.remove(0);
			
			File[] pathDirFiles = pathDir.listFiles();
			
			if(pathDirFiles != null) {
				for(int i = 0; i < pathDirFiles.length; i++) {
					String pathFilePath = pathDirFiles[i].getPath();
					
					if(pathFilePath.contains("_ESTest.java")) {
						result.add(pathFilePath);
					}else if(!pathFilePath.contains(".class")){
						filePathLists.add(pathFilePath);
					}
				}
			}
		}

		return result;
	}
	
	private ArrayList<String> getInputEvoSuiteTestTraceFileNameLists(){
		String path = "src/test/resources/EvoSuite/";
		ArrayList<String> result = new ArrayList<String>();
		
		File dir = new File(path);
		File[] files = dir.listFiles();
		for(int i = 0; i < files.length; i++) {
			String pathFilePath = files[i].getPath();
			
			if(pathFilePath.contains(".json")) {
				result.add(pathFilePath);
			}
		}

		return result;
	}
	
	private ArrayList<String> getInputBreakDownTestFileNameLists(){
		String path = "src/test/java/breakDownTest/";
		ArrayList<String> result = new ArrayList<String>();
		
		File dir = new File(path);
		File[] files = dir.listFiles();
		for(int i = 0; i < files.length; i++) {
			String pathFilePath = files[i].getPath();
			
			if(pathFilePath.contains(".java")) {
				result.add(pathFilePath);
			}
		}

		return result;
	}
	
	private ArrayList<String> getInputBreakDownTestTraceFileNameLists(){
		String path = "src/test/resources/breakDown/";
		ArrayList<String> result = new ArrayList<String>();
		
		File dir = new File(path);
		File[] files = dir.listFiles();
		for(int i = 0; i < files.length; i++) {
			String pathFilePath = files[i].getPath();
			
			if(pathFilePath.contains(".json")) {
				result.add(pathFilePath);
			}
		}

		return result;
	}
}
