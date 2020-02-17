package eu.interiot.intermw.bridge.openiot;

import eu.interiot.translators.syntax.openiot.OpenIoTTranslator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.nio.file.Files;

public class SyntacticTranslationTest {
	
	@Test
	public void testTranslation() throws Exception {

		// test 
		
        File resourcesDirectory = new File("src/test/resources/messages/DeviceList.json");
        
       
        OpenIoTTranslator translator1 = new OpenIoTTranslator();
       
        String deviceListContent = new String(Files.readAllBytes(resourcesDirectory.toPath()));
        Model jenaModel1 = translator1.toJenaModel(deviceListContent);
    
        
        BufferedWriter writer = new BufferedWriter(new FileWriter("samplefile1.n3"));
        
        String text=translator1.printJenaModel(jenaModel1, Lang.N3);
        writer.write(text);
        //String text=translator1.toFormatXTransformed(jenaModel1);
        //writer.write(translator1.prettifyJsonString(text));
        writer.close();
        
       

        System.out.println();
        System.out.println("+++ Reverse translation: +++");

        //Reverse the translation
        String jsonString1 = translator1.toFormatXTransformed(jenaModel1);

        System.out.println();
        System.out.println(translator1.prettifyJsonString(jsonString1));
        System.out.println();

        
        
         
        
        
       // String jsonString1 = translator1.toFormatXTransformed(jenaModel1);
       // System.out.println(jsonString1);
/*        

        FilenameFilter jsonFilenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".json")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        
        File[] jsonFiles = resourcesDirectory.listFiles(jsonFilenameFilter);

        OpenIoTTranslator translator = new OpenIoTTranslator();

        for(File f : jsonFiles){
            System.out.println("************************");
            System.out.println("+++ Input file: " + f.getAbsolutePath() + " +++");
            System.out.println();

            String fileContents = new String(Files.readAllBytes(f.toPath()));

            System.out.println(fileContents);

            System.out.println();
            System.out.println("+++ RDF output: +++");
            System.out.println();

            //Translate towards INTER-MW

            Model jenaModel = translator.toJenaModelTransformed(fileContents);

            System.out.println(translator.printJenaModel(jenaModel, Lang.N3));

            System.out.println();
            System.out.println("+++ Reverse translation: +++");

            //Reverse the translation
            String jsonString = translator.toFormatXTransformed(jenaModel);

            System.out.println();
            System.out.println(translator.prettifyJsonString(jsonString));
            System.out.println();

        }*/

    }
	
}
