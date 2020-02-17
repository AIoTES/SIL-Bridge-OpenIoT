package eu.interiot.translators.syntax.openiot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class TranslateObsrToRDF {

 public static String openiotbaseURI = "http://inter-iot.eu/syntax/OPENIOT#";
 public static String openiot = "http://openiot.eu/ontology/";
 public static String openiot_res = "http://openiot.eu/resource/";
 public static String geo = "http://www.opengis.net/ont/geosparql#";
 public static String wgs84_pos ="http://www.w3.org/2003/01/geo/wgs84_pos#";
 public static String qudt = "http://qudt.org/1.1/vocab/";
 public static String sosa = "http://www.w3.org/ns/sosa/";

 public static Model toJenaModel(String message) throws IOException {

  //String contents = new String(Files.readAllBytes(Paths.get("sampleObs.json")));

  ObjectMapper mapper = new ObjectMapper();
  JsonFactory factory = mapper.getFactory();
  JsonParser parser = factory.createParser(message);
  JsonNode topLevelNode = mapper.readTree(parser);

  Model mdl = ModelFactory.createDefaultModel();

  if (topLevelNode.isObject()) {

   Resource observationRsr = null;

   Iterator < Entry < String, JsonNode >> fields = topLevelNode.fields();

   while (fields.hasNext()) {
    Map.Entry < String, JsonNode > field = fields.next();

    // create the model for the toplevelNode i.e., observation
    if (field.getKey().equals("id")) {

     observationRsr = mdl.createResource(openiot_res + field.getValue());
     observationRsr.addProperty(RDF.type, mdl.createResource(sosa + "Observation"));

    }
    if (field.getKey().equals("observedProperty")) {
     observationRsr.addProperty(mdl.createProperty(sosa + "observedProperty"), mdl.getResource(openiot + field.getValue()));
    }
    if (field.getKey().equals("value")) {
     observationRsr.addProperty(mdl.createProperty(sosa + "hasSimpleResult"), mdl.createTypedLiteral(new Float(field.getValue().floatValue())));
    }
    if (field.getKey().equals("unit")) {
     observationRsr.addProperty(mdl.createProperty(qudt + "unit"), mdl.createTypedLiteral(new String(field.getValue().textValue())));
    }
    if (field.getKey().equals("created")) {
     observationRsr.addProperty(mdl.createProperty(sosa + "resultTime"), mdl.createTypedLiteral(new Float(field.getValue().floatValue())));
    }

    // start from sensor key that includes all the rest of the information
    if (field.getKey().equals("sensor")) {
     // sensor as root object:
     JsonNode sensor = field.getValue();

     Resource sensorRsr = null;

     if (sensor.isObject()) {

      Iterator < Entry < String,
       JsonNode >> sensorFields = sensor.fields();
      while (sensorFields.hasNext()) {

       Map.Entry < String,
        JsonNode > sensorField = sensorFields.next();

       //System.err.println(sensorField.getKey());
       // get the ID of that sensor and create a sensor type and its properties in model
       if (sensorField.getKey().equals("id")) {
        sensorRsr = mdl.createResource(openiot_res + sensorField.getValue());
        sensorRsr.addProperty(RDF.type, mdl.createResource(sosa + "Sensor"));
    
        

        observationRsr.addProperty(mdl.createProperty(sosa + "madeBySensor"), sensorRsr);

       }
       
       if(sensorField.getKey().equals("sensorType")){
    	    sensorRsr.addProperty(mdl.createProperty(sosa + "observes"), mdl.createResource(openiot + sensorField.getValue()));
       }
       
       if(sensorField.getKey().equals("category")){
    	   
    	   JsonNode categoryObj= sensorField.getValue();
    	   Iterator<Entry<String, JsonNode>> categoryFields = categoryObj.fields();
    	   
    	   while(categoryFields.hasNext()){
    		   Entry<String, JsonNode> categoryFeild = categoryFields.next();
    		   if(categoryFeild.getKey().equals("name")){
    			   sensorRsr.addProperty(mdl.createProperty(openiot + "category"), mdl.createTypedLiteral(new String(categoryFeild.getValue().textValue())));
    		   }
    		   
    	   }
       }
       

       // here rdf is create for the platform
       if (sensorField.getKey().equals("platform")) {
        // create the new JSON object for platform and parse it
        JsonNode platformObj = sensorField.getValue();

        Resource platformRsr = null;
        Resource blankNodePoint = mdl.createResource();
        JsonNode latitude = null;

        JsonNode longitude = null;

        if (platformObj.isObject()) {
         Iterator < Entry < String,
          JsonNode >> platformFields = platformObj.fields();
         while (platformFields.hasNext()) {

          Map.Entry < String,
           JsonNode > platformField = platformFields.next();
          System.err.println(platformField.getKey());
          if (platformField.getKey().equals("id")) {
           platformRsr = mdl.createResource(openiot_res + platformField.getValue());
           platformRsr.addProperty(RDF.type, mdl.createResource(sosa + "Platform"));

           platformRsr.addProperty(RDF.type, mdl.createResource(wgs84_pos + "Point"));
           platformRsr.addProperty(mdl.createProperty(geo + "hasGeometry"), blankNodePoint);

          }

          if (platformField.getKey().equals("latitude")) {

           latitude = platformField.getValue();
          }

          if (platformField.getKey().equals("longitude")) {

           longitude = platformField.getValue();
          }

          if ((latitude != null) && (longitude != null)) {
           blankNodePoint.addProperty(mdl.createProperty(geo + "asWKT"), mdl.createTypedLiteral(String.format("POINT(%s %s)", longitude, latitude),new String("http://www.opengis.net/def/sf/wktLiteral")));
           //mdl.write(System.out, "N-Triples");
          }

         }

        }

       }

      }

     }

    }


   }
  }
  mdl.write(System.out, "N-Triples");
  return mdl;

 }
/* public static void main (String[] args){
	 
	 try {
		 String contents = new String(Files.readAllBytes(Paths.get("sampleObs.json")));
		TranslateObsrToRDF.toJenaModel(contents);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }*/
}