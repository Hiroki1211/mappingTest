package exportFileGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ExportFileGenerator {

	public void run(ArrayList<NaturalTestClass> naturalTestClassLists) {
		String dirName = "src/test/java/mappingTest";
		File dir = new File(dirName);
		dir.mkdir();
		
		for(int classNum = 0; classNum < naturalTestClassLists.size(); classNum++) {
			NaturalTestClass naturalTestClass = naturalTestClassLists.get(classNum);
			ArrayList<String> contents = naturalTestClass.getContentLists();
			
			File file = new File(dirName + "/" + naturalTestClass.getClassName() + ".java");
			file.setExecutable(true);
			file.setReadable(true);
			file.setWritable(true);
			
			try {
				FileWriter fw = new FileWriter(file);
				for(int i = 0; i < contents.size(); i++) {
					fw.write(contents.get(i) + "\n");
				}
				
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
