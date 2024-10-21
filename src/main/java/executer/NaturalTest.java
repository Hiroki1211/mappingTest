package executer;

import java.util.ArrayList;

import testAnalyzer.Test;
import testAnalyzer.TestAssertion;
import testAnalyzer.TestImport;
import testAnalyzer.TestMethod;

public class NaturalTest {

	private String className;
	private ArrayList<String> content = new ArrayList<String>();
	private ArrayList<TestImport> importLists;
	private String packageName;
	
	private ArrayList<Test> naturalTestLists = new ArrayList<Test>();
	private ArrayList<Test> evoSuiteTestLists = new ArrayList<Test>();
	
	public NaturalTest(String s, ArrayList<TestImport> iL) {
		className = s;
		importLists = iL;
	}
	
	public void createContent() {
		packageName = "naturalTest";
		
		content.add("package " + packageName + ";"); 
		content.add("");
		
		for(int importNum = 0; importNum < importLists.size(); importNum++) {
			TestImport testImport = importLists.get(importNum);
			content.add(testImport.getStatement());
		}
		
		content.add("public class " + className + " {");
		content.add("");
		
		content.add("  // Mapping Natural Test");
		for(int naturalTestNum = 0; naturalTestNum < naturalTestLists.size(); naturalTestNum++) {
			Test naturalTest = naturalTestLists.get(naturalTestNum);
			
			content.add("  @Test");
			content.add("  public void naturalTest" + naturalTestNum + "(){");
			
			ArrayList<TestMethod> methodLists = naturalTest.getMethodLists();
			for(int methodNum = 0; methodNum < methodLists.size(); methodNum++) {
				TestMethod method = methodLists.get(methodNum);
				
				content.add(method.getStatement());
			}
			
			ArrayList<TestAssertion> assertionLists = naturalTest.getAssertionLists();
			for(int assertNum = 0; assertNum < assertionLists.size(); assertNum++) {
				TestAssertion testAssertion = assertionLists.get(assertNum);
				String assertContent = "      assertEquals(";
				assertContent = assertContent + testAssertion.getValue() + ", ";
				
				if(testAssertion.getVariable().equals("")) {
					assertContent = assertContent + testAssertion.getGetterMethodInstance() + "." + testAssertion.getGetterMethodName() + "(";
					
					ArrayList<String> argumentLists = testAssertion.getGetterMethodArgument();
					if(argumentLists.size() == 0) {
						assertContent = assertContent + "));";
					}else {
						for(int argNum = 0; argNum < argumentLists.size(); argNum++) {
							if(argNum == argumentLists.size() - 1) {
								assertContent = assertContent + argumentLists.get(argNum) + "));";
							}else {
								assertContent = assertContent + argumentLists.get(argNum) + ", ";
							}
						}
					}
				}else {
					assertContent = assertContent + testAssertion.getVariable() + ");";
				}
				
				content.add(assertContent);
			}
			
			content.add("  }");
			content.add("");
		}
		
		content.add("  // not matched EvoSuiteTest");
		for(int evoTestNum = 0; evoTestNum < evoSuiteTestLists.size(); evoTestNum++) {
			Test evoSuiteTest = evoSuiteTestLists.get(evoTestNum);
			ArrayList<String> evoSuiteTestContent = evoSuiteTest.getContents();
			for(int contentNum = 0; contentNum < evoSuiteTestContent.size(); contentNum++) {
				content.add(evoSuiteTestContent.get(contentNum));
			}
			content.add("");
		}
		
		content.add("}");
		
	}
	
	public String getClassName() {
		return className;
	}
	
	public ArrayList<String> getContent(){
		return content;
	}
	
	public ArrayList<Test> getNaturalTestLists(){
		return naturalTestLists;
	}
	
	public ArrayList<Test> getEvoSuiteTestLists(){
		return evoSuiteTestLists;
	}
	
	public void addNaturalTestLists(Test input) {
		naturalTestLists.add(input);
	}
	
	public void addEvoSuiteTestLists(Test input) {
		evoSuiteTestLists.add(input);
	}
}
