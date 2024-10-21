package paramaterExtracter;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import analyzer.*;
import breakDownPathExtracter.PutInstanceVariable;
import pathExtracter.PathExtracter;
import pathExtracter.TraceMethodBlock;
import tracer.Lexer;
import tracer.Trace;
import tracer.ValueOption;

public class ParamaterExtracter {
	private static String inputTraceFileName = "trace.json";
	
	public static void main(String[] argv) {
		File inputTraceFile = new File(inputTraceFileName);
		Lexer lexer = new Lexer(inputTraceFile);
		ArrayList<Trace> traceLists = lexer.getTraceLists();
		Analyzer analyzer = new Analyzer();
		ParamaterExtracter paramaterExtracter = new ParamaterExtracter();
		ArrayList<String> analyzeTestLists = paramaterExtracter.getAnalyzeFile();
		if(analyzeTestLists.size() > 0) {
			analyzer.run(analyzeTestLists);
			ArrayList<AnalyzerMethod> analyzerMethodLists = analyzer.getMethodLists();
			ArrayList<AnalyzerVariable> analyzerVariableLists = analyzer.getVariableLists();
			paramaterExtracter.run(analyzerMethodLists, analyzerVariableLists, traceLists);
		}else {
			System.out.println("code does not exist");
		}
	}
	
	public ArrayList<String> getAnalyzeFile(){
//		String path = "src/main/java/";
		ArrayList<String> result = new ArrayList<String>();
		result.add("src/test/resources/ex07/Calculator.java");
		result.add("src/test/resources/ex07/Executer.java");
		result.add("src/test/resources/ex07/Formula.java");
//		
//		File dir = new File(path);
//		File[] files = dir.listFiles();
//		
//		ArrayList<String> filePathLists = new ArrayList<String>();
//		
//		for(int i = 0; i < files.length; i++) {
//			String filePath = files[i].getPath();
//			
//			if(!filePath.contains("trace.json")) {
//				if(!filePath.contains(".java") && !filePath.contains(".class")) {
//					filePathLists.add(filePath);
//				}
//			}
//		}
//		
//		while(filePathLists.size() > 0) {
//			File pathDir = new File(filePathLists.get(0));
//			filePathLists.remove(0);
//			
//			File[] pathDirFiles = pathDir.listFiles();
//			
//			for(int i = 0; i < pathDirFiles.length; i++) {
//				String pathFilePath = pathDirFiles[i].getPath();
//				
//				if(pathFilePath.contains(".java")) {
//					result.add(pathFilePath);
//				}else if(!pathFilePath.contains(".class")){
//					filePathLists.add(pathFilePath);
//				}
//			}
//			
//		}

		return result;
	}
	
	public ArrayList<ExtractClass> run(ArrayList<AnalyzerMethod> analyzerMethodLists, ArrayList<AnalyzerVariable> analyzerVariableLists, ArrayList<Trace> traceLists) {
		ArrayList<ExtractClass> extractClassLists = this.extractParamater(traceLists, analyzerMethodLists, analyzerVariableLists);

		return extractClassLists;
	}
	
	private ArrayList<ExtractClass> extractParamater(ArrayList<Trace> traceLists, ArrayList<AnalyzerMethod> analyzerMethodLists, ArrayList<AnalyzerVariable> analyzerVariableLists) {
		ArrayList<ExtractClass> extractClassLists = new ArrayList<ExtractClass>();
		
		// data sets
		ArrayList<Instance> instanceLists = new ArrayList<Instance>();
		
		// for method 
		Trace callTrace = null;
		ArrayList<Trace> callParamTraceLists = new ArrayList<Trace>();
		boolean isSameClass = false;
		
		// for constructor
		boolean isConstructor = false;
		
		// extract params
		for(int traceNum = 0; traceNum < traceLists.size(); traceNum++) {
			Trace trace = traceLists.get(traceNum);
			
			switch(trace.getEvent()) {
				case "CALL":
					callTrace = trace;
					if(trace.getAttr().getOwner().equals(trace.getCname())) {
						isSameClass = true;
					}
					
					break;
				
				case "CALL_PARAM":
					if(trace.getValuetype().equals("float")) {
						ValueOption paramValueOption = trace.getValueOption();
						String tmpValue = paramValueOption.getValue() + "f";
						paramValueOption.setValue(tmpValue);
					}
					callParamTraceLists.add(trace);
					break;
			
				case "CALL_RETURN":
					// is not called from same class & is not constructor
					if(!isSameClass && !isConstructor) {
						
						AnalyzerMethod analyzerMethod = null;
						String[] splitOwner = callTrace.getAttr().getOwner().split(Pattern.quote("."));
						String methodOwnerName = splitOwner[splitOwner.length - 1];
						for(int analyzerNum = 0; analyzerNum < analyzerMethodLists.size(); analyzerNum++) {
							AnalyzerMethod targetAnalyzerMethod = analyzerMethodLists.get(analyzerNum);
							
							if(callTrace.getAttr().getName().equals(targetAnalyzerMethod.getName()) && targetAnalyzerMethod.getOwnerClass().getName().equals(methodOwnerName)) {
								analyzerMethod = targetAnalyzerMethod;
							}
						}
						
						if(analyzerMethod != null && analyzerMethod.getAccessModifier().equals("public")) {
							// create method
							ExtractMethod extractMethod = new ExtractMethod(callTrace.getAttr().getName(), analyzerMethod, callTrace.getSeqNum());
							for(int callParamNum = 0; callParamNum < callParamTraceLists.size(); callParamNum++ ) {
								extractMethod.addArgumentLists(callParamTraceLists.get(callParamNum).getValueOption());
							}
							extractMethod.setReturnValueOption(trace.getValueOption());
							
							// add method to instance
							String instanceId = callTrace.getValueOption().getId();
							Instance instance = this.getInstanceFromId(instanceId, instanceLists);
							if(instance != null) {
								instance.addExtractMethodLists(extractMethod);
							}
						}
						
						callParamTraceLists = new ArrayList<Trace>();
					}
					
					isSameClass = false;
					break;
				case "NEW_OBJECT":
					isConstructor = true;
					break;
				
				case "NEW_OBJECT_CREATED":
					if(!isSameClass) {
						String[] splitType = trace.getValueOption().getType().split(Pattern.quote("."));
						String ownerClass = splitType[splitType.length - 1];
						
						Instance createInstance = new Instance(trace.getValueOption().getId(), ownerClass);
						instanceLists.add(createInstance);
						
						// create method
						AnalyzerMethod analyzerMethod = null;
						for(int analyzerNum = 0; analyzerNum < analyzerMethodLists.size(); analyzerNum++) {
							AnalyzerMethod targetAnalyzerMethod = analyzerMethodLists.get(analyzerNum);
							String[] splitOwner = callTrace.getAttr().getOwner().split(Pattern.quote("."));
							String methodOwnerName = splitOwner[splitOwner.length - 1];
							
							if(callTrace.getMname().equals(targetAnalyzerMethod.getName()) && targetAnalyzerMethod.getOwnerClass().getName().equals(methodOwnerName)) {
								analyzerMethod = targetAnalyzerMethod;
							}
						}
						
						if(analyzerMethod == null || (analyzerMethod != null && analyzerMethod.getAccessModifier().equals("public"))) {
							ExtractMethod extractMethod = new ExtractMethod(callTrace.getAttr().getName(), analyzerMethod, callTrace.getSeqNum());
							for(int callParamNum = 0; callParamNum < callParamTraceLists.size(); callParamNum++ ) {
								extractMethod.addArgumentLists(callParamTraceLists.get(callParamNum).getValueOption());
							}
							extractMethod.setReturnValueOption(trace.getValueOption());
							
							createInstance.addExtractMethodLists(extractMethod);
						}
						
						isConstructor = false;
					}
					
					isSameClass = false;
					callParamTraceLists = new ArrayList<Trace>();
					
					break;
			}
		}
		
		// put instance field lists
		Trace putInstanceFieldTrace = null;
		for(int traceNum = 0; traceNum < traceLists.size(); traceNum++) {
			Trace trace = traceLists.get(traceNum);
			
			switch(trace.getEvent()) {
				case "PUT_INSTANCE_FIELD":
					putInstanceFieldTrace = trace;
					break;
				
				case "PUT_INSTANCE_FIELD_VALUE":
					String instanceId = putInstanceFieldTrace.getValueOption().getId();
					Instance putInstance = this.getInstanceFromId(instanceId, instanceLists);
					
					if(putInstance != null) {
						AnalyzerVariable analyzerVariable = null;
						for(int varNum = 0; varNum < analyzerVariableLists.size(); varNum++) {
							AnalyzerVariable targetAnalyzerVariable = analyzerVariableLists.get(varNum);
							if(targetAnalyzerVariable.getName().equals(trace.getAttr().getName()) && targetAnalyzerVariable.getOwnerClass().getName().equals(putInstance.getOwnerClass())) {
								analyzerVariable = targetAnalyzerVariable;
								break;
							}
						}
						
						PutInstanceVariable putInstanceVariable = new PutInstanceVariable(analyzerVariable, putInstanceFieldTrace.getValueOption(), trace.getValueOption());
						putInstance.addPutInstanceVariableLists(putInstanceVariable);
					}
					
					break;
			}	
		}
		
		PathExtracter pathExtracter = new PathExtracter();
		ArrayList<TraceMethodBlock> traceMethodBlockLists = pathExtracter.getTraceMethodBlockLists(traceLists);
		
		for(int instanceNum = 0; instanceNum < instanceLists.size(); instanceNum++) {
			for(int index = 0; index < instanceLists.size() - 1; index++) {
				Instance frontInstance = instanceLists.get(index);
				Instance backInstance = instanceLists.get(index + 1);
				int frontSeqNum = frontInstance.getExtractMethodLists().get(0).getSeqNum();
				int backSeqNum = backInstance.getExtractMethodLists().get(0).getSeqNum();
				
				if(frontSeqNum > backSeqNum) {
					instanceLists.set(index, backInstance);
					instanceLists.set(index + 1, frontInstance);
				}
			}
		}
		
		ArrayList<Integer> borderLists = new ArrayList<Integer>();
		for(int instanceNum = 0; instanceNum < instanceLists.size(); instanceNum++) {
			borderLists.add(instanceLists.get(instanceNum).getExtractMethodLists().get(0).getSeqNum());
		}
		
		for(int blockNum = 0; blockNum < traceMethodBlockLists.size(); blockNum++) {
			TraceMethodBlock traceMethodBlock = traceMethodBlockLists.get(blockNum);
			Trace entryTrace = traceMethodBlock.getTraceLists().get(0);
			
			if(entryTrace.getValueOption() != null) {
				String instanceId = entryTrace.getValueOption().getId();
				Instance entryInstance = this.getInstanceFromId(instanceId, instanceLists);
				
				if(entryInstance != null) {
					entryInstance.addTraceMethodBlockLists(traceMethodBlock);
				}
			}else {
				String split[] = entryTrace.getFilename().split("/");
				String className = split[split.length - 1];

				for(int borderNum = 0; borderNum < borderLists.size(); borderNum++) {
					if(borderNum == borderLists.size() - 1) {
						if(instanceLists.get(borderNum).getOwnerClass().equals(className)) {
							instanceLists.get(borderNum).addTraceMethodBlockLists(traceMethodBlock);
						}
					}else {
						if(entryTrace.getSeqNum() > borderLists.get(borderNum) && entryTrace.getSeqNum() < borderLists.get(borderNum + 1)) {
							if(instanceLists.get(borderNum).getOwnerClass().equals(className)) {
								instanceLists.get(borderNum).addTraceMethodBlockLists(traceMethodBlock);
							}
							break;
						}
					}
				}
			}
		}
		
		ArrayList<String> codeLists = this.getAnalyzeFile();
		for(int codeNum = 0; codeNum < codeLists.size(); codeNum++) {
			String pathName = codeLists.get(codeNum);
			String[] split = pathName.split("/");
			String className = split[split.length - 1].replace(".java", "");
			
			ExtractClass extractClass = new ExtractClass(className);
			extractClassLists.add(extractClass);
			
			for(int instanceNum = 0; instanceNum < instanceLists.size(); instanceNum++) {
				Instance instance = instanceLists.get(instanceNum);
				if(instance.getOwnerClass().equals(className)) {
					extractClass.addInstanceLists(instance);
				}
			}
			
			System.out.println(className);
			ArrayList<Instance> testInstanceLists = extractClass.getInstanceLists();
			for(int i = 0; i < testInstanceLists.size(); i++) {
				testInstanceLists.get(i).display();
				Instance x = testInstanceLists.get(i);
				for(int j = 0; j < x.getTraceMethodBlockLists().size(); j++) {
					System.out.println(x.getTraceMethodBlockLists().get(j).getTraceLists().get(0).getMname() + x.getTraceMethodBlockLists().get(j).getTraceLists().get(0).getSeqNum());
				}
				System.out.println();
			}
		}
		
		return extractClassLists;
	}
	
	private Instance getInstanceFromId(String id, ArrayList<Instance> instanceLists) {
		Instance instance = null;
		for(int instanceNum = 0; instanceNum < instanceLists.size(); instanceNum++) {
			Instance targetInstance = instanceLists.get(instanceNum);
			if(targetInstance.getId().equals(id)) {
				instance = targetInstance;
				break;
			}
		}
		
		return instance;
	}
}
