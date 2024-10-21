package executer;

import java.util.ArrayList;

import testAnalyzer.Test;
import testAnalyzer.TestClass;

public class Result {

	private TestClass evoSuiteTestClass;
	private TestClass breakDownTestClass;
	
	private ArrayList<MatchingResult> matchingResultLists = new ArrayList<MatchingResult>();
	private ArrayList<SameExecutePath> notMatchingEvoSuiteLists = new ArrayList<SameExecutePath>();
	private ArrayList<SameExecutePath> notMatchingBreakDownLists = new ArrayList<SameExecutePath>();
	
	public Result(TestClass e, TestClass b) {
		evoSuiteTestClass = e;
		breakDownTestClass = b;
	}
	
	public void display() {
		System.out.println("--------------- RESULT ------------------");
		System.out.println("EvoSuiteTestClass:" +  evoSuiteTestClass.getClassName());
		System.out.print("BreakDownTestClass:");
		if(breakDownTestClass == null) {
			System.out.println("null");
		}else {
			System.out.println(breakDownTestClass.getClassName());
		}
		System.out.println();
		
		// is matched 
		for(int matchNum = 0; matchNum < matchingResultLists.size(); matchNum++) {
			MatchingResult matchingResult = matchingResultLists.get(matchNum);
			SameExecutePath evoSuiteSameExecutePath = matchingResult.getEvoSuiteSamePathExecutePath();
			SameExecutePath breakDownSameExecutePath = matchingResult.getBreakDownSamePathExecutePath();
			System.out.println("######## matched #########");
			
			ArrayList<Test> evoSuiteTestLists = evoSuiteSameExecutePath.getTestLists();
			ArrayList<Test> breakDownTestLists = breakDownSameExecutePath.getTestLists();
			
			System.out.print("EvoSuite:");
			for(int evoTestNum = 0; evoTestNum < evoSuiteTestLists.size(); evoTestNum++) {
				Test evoSuiteTest = evoSuiteTestLists.get(evoTestNum);
				System.out.print(evoSuiteTest.getMethodName() + ", ");
			}
			System.out.println();
			
			System.out.print("BreakDown:");
			for(int breakNum = 0; breakNum < breakDownTestLists.size(); breakNum++) {
				Test breakDownTest = breakDownTestLists.get(breakNum);
				System.out.print(breakDownTest.getMethodName() + ", ");
			}
			System.out.println();
		}
		
		// is not matched EvoSuite
		for(int notEvoNum = 0; notEvoNum < notMatchingEvoSuiteLists.size(); notEvoNum++) {
			SameExecutePath notMatchEvoSuite = notMatchingEvoSuiteLists.get(notEvoNum);
			System.out.println("********** not match EvoSuite **********");
			
			ArrayList<Test> evoSuiteTestLists = notMatchEvoSuite.getTestLists();
			for(int evoNum = 0; evoNum < evoSuiteTestLists.size(); evoNum++) {
				System.out.print(evoSuiteTestLists.get(evoNum).getMethodName() + ", ");
			}
			System.out.println();
			
		}
		
		// is not matched BreakDown
		for(int notBreakNum = 0; notBreakNum < notMatchingBreakDownLists.size(); notBreakNum++) {
			SameExecutePath notMatchBreak = notMatchingBreakDownLists.get(notBreakNum);
			System.out.println("======== not match breakDown =======");
			
			ArrayList<Test> breakDownTestLists = notMatchBreak.getTestLists();
			for(int breakNum = 0; breakNum < breakDownTestLists.size(); breakNum++) {
				System.out.print(breakDownTestLists.get(breakNum).getMethodName() + ", ");
			}
			System.out.println();
		}
		
		System.out.println();
	}
	
	public void addMatchingResultLists(MatchingResult input) {
		matchingResultLists.add(input);
	}
	
	public void addNotMatchingEvoSuiteLists(SameExecutePath input) {
		notMatchingEvoSuiteLists.add(input);
	}
	
	public void addNotMatchingBreakDownLists(SameExecutePath input) {
		notMatchingBreakDownLists.add(input);
	}
	
	public TestClass getEvoSuiteTestClass() {
		return evoSuiteTestClass;
	}
	
	public TestClass getBreakDownTestClass() {
		return breakDownTestClass;
	}
	
	public ArrayList<MatchingResult> getMatchingResultLists(){
		return matchingResultLists;
	}
	
	public ArrayList<SameExecutePath> getNotMatchingEvoSuiteLists(){
		return notMatchingEvoSuiteLists;
	}
	
	public ArrayList<SameExecutePath> getNotMatchingBreakDownLists(){
		return notMatchingBreakDownLists;
	}
}
