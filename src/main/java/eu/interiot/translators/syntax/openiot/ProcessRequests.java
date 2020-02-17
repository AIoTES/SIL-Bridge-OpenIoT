package eu.interiot.translators.syntax.openiot;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.interiot.intermw.bridge.BridgeConfiguration;
import eu.interiot.intermw.bridge.exceptions.BridgeException;
import eu.interiot.intermw.bridge.openiot.OpenIoTUtils;
import eu.interiot.intermw.comm.broker.Publisher;
import eu.interiot.intermw.comm.broker.exceptions.BrokerException;
import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.intermw.commons.model.enums.IoTDeviceType;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.MessagePayload;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.ID.PropertyID;
import eu.interiot.message.exceptions.MessageException;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata.MessageTypesEnum;
import eu.interiot.message.metadata.PlatformMessageMetadata;
import eu.interiot.message.payload.types.IoTDevicePayload;
import eu.interiot.message.utils.MessageUtils;
import eu.interiot.translators.syntax.openiot.domain.Sensor;
import spark.Spark;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;


public class ProcessRequests {
    private final static Logger logger = LoggerFactory.getLogger(ProcessRequests.class);
    private final static String PLATFORM_ID = "openiot";
    private final static String PLATFORM_NAME = "openiot";
    private final static String PLATFORM_URL = "http://srvgal106.deri.ie:8018/api/";

    private HttpClient httpClient;
    Properties properties;
    Client client;
    
    private WebTarget platformTarget;
    
    
    public ProcessRequests(BridgeConfiguration configuration, HttpClient httpClient) {

    	this.httpClient= httpClient;
    	this.properties=configuration.getProperties();
    	
    	client = ClientBuilder.newClient();	
    
	}

	public Sensor convertIoTDeviceToSensor(IoTDevice device) {
        Sensor sensor = new Sensor();
        sensor.setName(device.getName());
        sensor.setSensorId(device.getDeviceId());
        sensor.setSensorType("SENSOR");
        return sensor;
    }


    public Message thingRegisterMessage(Message message2, URL baseEndpointURL) throws IOException, MessageException {
    	
    	   
      	logger.debug("Registering platform {}...", baseEndpointURL);
    	
        List<Sensor> sensors = getPlatformSensor();
        String sensorDeviceGraph = sensorsToGraphDevices(sensors);
        URL registerMessageUrl = Resources.getResource("messages/thing-register.json");
        String platformRegisterJson = Resources.toString(registerMessageUrl, Charsets.UTF_8);
        platformRegisterJson = String.format(platformRegisterJson, PLATFORM_ID, PLATFORM_ID,sensorDeviceGraph);
        logger.info("thingRegisterMessage: {}", platformRegisterJson);
        Message message = new Message(platformRegisterJson);
        return  message;
    }
    
    
    public  Message thingUnRegisterMessage() throws IOException, MessageException {
        List<Sensor> sensors = getPlatformSensor();
        String sensorDeviceGraph = sensorsToGraphDevices(sensors);
        URL registerMessageUrl = Resources.getResource("messages/thing-unregister.json");
        String platformRegisterJson = Resources.toString(registerMessageUrl, Charsets.UTF_8);
        platformRegisterJson = String.format(platformRegisterJson, PLATFORM_ID, PLATFORM_ID,sensorDeviceGraph);
        logger.info("thingUnRegisterMessage: {}", platformRegisterJson);
        Message message = new Message(platformRegisterJson);
        return  message;
    }
    
    
	
	public Message thingListDevices(Message message, Platform platform, Publisher<Message> publisher) throws BridgeException, BrokerException {
		
	    List<Sensor> devices = null;
	    System.err.println("base URI: "+platform.getBaseEndpoint().toString());
        try {
        	platformTarget=client.target(platform.getBaseEndpoint().toString());
        	//platformTarget=client.target("http://srvgal106.deri.ie:8018/api");
        	System.err.println("URI of the sensor list :"+platformTarget.getUri());
        	
        	devices = platformTarget
                    .path("/api/sensors")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<List<Sensor>>() {
                    });
        	        	
       	String gs1= new Gson().toJson(devices);
        	
        	for(Sensor dev: devices){	
        		String gs= new Gson().toJson(dev);
        		System.err.println(gs);
        	}

            
        } catch (Exception e) {
            throw new BridgeException(String.format(
                    "Failed to retrieve list of devices from the platform %s.", platform.getPlatformId()), e);
        }

        MessageMetadata metadata = new MessageMetadata();
        metadata.initializeMetadata();
        metadata.addMessageType(MessageTypesEnum.DEVICE_REGISTRY_INITIALIZE);
        metadata.asPlatformMessageMetadata().setSenderPlatformId(new EntityID(platform.getPlatformId()));

        IoTDevicePayload payload = new IoTDevicePayload();
        for (Sensor device : devices) {
        	System.err.println(device.getId());
            EntityID entityID = new EntityID(PLATFORM_URL+device.getId());
            payload.createIoTDevice(entityID);
            payload.setHasName(entityID, PLATFORM_URL + device.getName());
            
            payload.setIsHostedBy(entityID, new EntityID(platform.getPlatformId()));

            PropertyID deviceTypePropertyId = new PropertyID("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            payload.addDataPropertyAssertionToEntity(entityID, deviceTypePropertyId, IoTDeviceType.SENSOR.getDeviceTypeUri());
        }

        Message registryInitializeMessage = new Message();
        registryInitializeMessage.setMetadata(metadata);
        registryInitializeMessage.setPayload(payload);

        // publish DEVICE_REGISTRY_INITIALIZE message
        publisher.publish(registryInitializeMessage);

        // return LIST_DEVICES response message
        return registryInitializeMessage;
        
	}
	
    public  Message platformRegisterMessage(Message msgMetaData, Platform platform, Publisher<Message> publisher) throws IOException, MessageException, URISyntaxException, BridgeException {
    	
       
        if (msgMetaData.getMetadata().getMessageTypes().contains(MessageTypesEnum.SYS_INIT)) {
            // restore platform registration after INTER-MW restart. Registration request shouldn't be sent to the platform again.
            logger.debug("Registration of the platform {} has been restored.", platform.getPlatformId());
            return msgMetaData;
        }

        //URL url = new URL("http://srvgal106.deri.ie:8018/api/platforms");
        URL url = new URL(platform.getBaseEndpoint(), "/api/register-platform"); 
        
        HttpPost httpPost = new HttpPost(url.toURI());
        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("platformId", platform.getPlatformId()));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters, "UTF-8");
        httpPost.setEntity(entity);
        try {
            HttpResponse resp = httpClient.execute(httpPost);
            logger.debug("Received response from the platform: {}", resp.getStatusLine());
           // temporaryly commented out, hoan has to implement
            /*if (resp.getStatusLine().getStatusCode() != 204) {
                throw new BridgeException("Invalid response code received from the platform: " + resp.getStatusLine().toString());
            }*/
        } finally {
            httpPost.releaseConnection();
        }

        logger.debug("Platform {} has been registered successfully.", platform.getPlatformId());
        return msgMetaData;
    }
    public Message platformUnRegisterMessage(Message msgMetaData, Platform platform, Publisher<Message> publisher) throws IOException, MessageException, URISyntaxException, BridgeException {
    	
    	
    	
    	URL url = new URL(platform.getBaseEndpoint(), "/api/unregister-platform");
        HttpPost httpPost = new HttpPost(url.toURI());
        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("platformId", platform.getPlatformId()));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters, "UTF-8");
        httpPost.setEntity(entity);
        try {
            HttpResponse resp = httpClient.execute(httpPost);
            logger.debug("Received response from the platform: {}", resp.getStatusLine());
          /*  if (resp.getStatusLine().getStatusCode() != 204) {
                throw new BridgeException("Invalid response code received from the platform: " + resp.getStatusLine().toString());
            }*/
        } finally {
            httpPost.releaseConnection();
        }

        logger.debug("Platform {} has been unregistered.", platform.getPlatformId());
        return msgMetaData;

    }
    
    
    public   Message thingSubribleMessage(Message message, Platform platform, URL bridgeCallbackUrl, Publisher<Message> publisher) throws IOException, MessageException, BridgeException, URISyntaxException {
    	
    	String conversationId = message.getMetadata().getConversationId().orElse(null); 
    	
    	List<String> deviceIds = OpenIoTUtils.extractDeviceIds(message);
    	
    	
    	if (message.getMetadata().getMessageTypes().contains(MessageTypesEnum.SYS_INIT)) {
            // restore subscription after INTER-MW restart. Subscription is still active on the platform so the
            // subscribe request shouldn't be sent again.
            logger.debug("Restoring subscription {} to devices {}...", conversationId, deviceIds);
            URL convCallbackUrl = new URL(bridgeCallbackUrl, conversationId);
            createObservationsListener(conversationId, convCallbackUrl, platform, publisher);
            logger.debug("Subscription {} has been restored.", conversationId);
            return message;

        } else {
            logger.debug("Subscribing to devices {} with conversationId {}...", deviceIds, conversationId);
            
            
            URL url = new URL(platform.getBaseEndpoint(),"/api/subscriptions"); 
           // URL url = new URL("http://srvgal106.deri.ie:8018/api/subscriptions"); 	
            HttpPost httpPost = new HttpPost(url.toURI());
            
            JsonObject jsonObj= new JsonObject();
            
            URL convCallbackUrl = new URL(bridgeCallbackUrl, conversationId);
            
            jsonObj.addProperty("callBackURL", convCallbackUrl.toString());
            
            jsonObj.addProperty("conversationId", conversationId);
            JsonArray array= new JsonArray();
            
            
            for(String devices: deviceIds){
            	
            	String deviceId = devices.substring(devices.lastIndexOf('/') + 1);
            	JsonObject sensorIds= new JsonObject();
            	sensorIds.addProperty("id", deviceId);
            	array.add(sensorIds);
            }
            
            jsonObj.add("sensors", array);
            
            System.out.println(jsonObj);
            StringEntity entity = new StringEntity(jsonObj.toString(), HTTP.UTF_8);
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            try {
                HttpResponse resp = httpClient.execute(httpPost);
                logger.debug("Received response from the platform: {}", resp.getStatusLine());
                if (resp.getStatusLine().getStatusCode() != 201) {
                    throw new BridgeException("Invalid response code received from the platform: " + resp.getStatusLine().toString());
                }
            } finally {
                httpPost.releaseConnection();
            }

            createObservationsListener(conversationId, convCallbackUrl, platform, publisher);

            logger.debug("Subscribed successfully to devices {} with conversationId {}...", deviceIds, conversationId);
            return message;
        }
 
    }

    
    private void createObservationsListener(String conversationId, URL convCallbackUrl, Platform platform, Publisher<Message> publisher ) {
        logger.debug("Creating callback listener for conversation {} listening at {}...", conversationId, convCallbackUrl);
        
        try{        	
        	Spark.post(conversationId, (request, response) -> {
            logger.debug("New observation received from the platform.");
            try {
                // create message metadata
                PlatformMessageMetadata metadata = new MessageMetadata().asPlatformMessageMetadata();
                metadata.initializeMetadata();
                metadata.addMessageType(URIManagerMessageMetadata.MessageTypesEnum.OBSERVATION);
                metadata.addMessageType(URIManagerMessageMetadata.MessageTypesEnum.RESPONSE);
                metadata.setSenderPlatformId(new EntityID(platform.getPlatformId()));
                metadata.setConversationId(conversationId);

                //response.status(200);
                

                // using OpenIotTranslator convert observations into a MessagePayload object
                String observation = request.body();
                System.err.println("observation: "+ observation);
                
                //TranslateObsrToRDF.toJenaModel(observation);
                
                //OpenIoTTranslator translate= new OpenIoTTranslator();
                Model output = TranslateObsrToRDF.toJenaModel(observation);//translate.toJenaModel(observation);
                
                output.write(System.out, "N-Triples");
                
                MessagePayload messagePayload = new MessagePayload(output);
                Message observationMessage = new Message();
                observationMessage.setMetadata(metadata);
                observationMessage.setPayload(messagePayload);
                
                publisher.publish(observationMessage);
                
                logger.debug("Observation message {} has been published upstream through Inter MW.", observationMessage.getMetadata().getMessageID().get());
                return "";

            } catch (Exception e) {
                logger.debug("Failed to handle observation with conversationId " + conversationId + ": " + e.getMessage(), e);
                response.status(400);
                return "Failed to handle observation: " + e.getMessage();
            }
        });
        
    }catch(Exception e){
    	logger.debug("failed to send request using SPARK API {}", e.getMessage());
    }
    }
    


    public  Message thingUnsubribleMessage(Message message, Platform platform, Publisher<Message> publisher) throws IOException, MessageException, URISyntaxException, BridgeException {

    	String conversationId = message.getMetadata().asPlatformMessageMetadata().getSubscriptionId().get();
        logger.debug("Unsubscribing from the conversation {}...", conversationId);

        URL url = new URL(platform.getBaseEndpoint(), "/unsubscribe");
        HttpPost httpPost = new HttpPost(url.toURI());
        List<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("conversationId", conversationId));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters, "UTF-8");
        httpPost.setEntity(entity);
        try {
            HttpResponse resp = httpClient.execute(httpPost);
            logger.debug("Received response from the platform: {}", resp.getStatusLine());
            if (resp.getStatusLine().getStatusCode() != 204) {
                throw new BridgeException("Invalid response code received from the platform: " + resp.getStatusLine().toString());
            }
        } finally {
            httpPost.releaseConnection();
        }

        logger.debug("Unsubscribed from the conversation {} successfully.", conversationId);
        return message;
        
    }

    
    protected  Message createResponseMessage(Message message, Platform platform) {
        Message responseMessage = MessageUtils.createResponseMessage(message);
        responseMessage.getMetadata().asPlatformMessageMetadata().setSenderPlatformId(new EntityID(platform.getPlatformId()));
        return responseMessage;
    }
    
    private static String sensorSubribleDevice(Sensor sensor) {
        String sensorTemplate = String.format("{\n" +
                "      \"@id\" : \"http://inter-iot.eu/dev/SensorTemperatura/identificador#%s\",\n" +
                "      \"@type\" : \"InterIoT:GOIoTP#IoTDevice\",\n" +
                "      \"InterIoT:GOIoTP#hasName\" : \"%s\",\n" +
                "      \"http://www.w3.org/ns/sosa/isHostedBy\" : {\n" +
                "        \"@id\" : \"InterIoT:%s\"\n" +
                "      }\n" +
                "    }", sensor.getId(), sensor.getName(), "openiot");
        return sensorTemplate;

    }

    private static String sensorUnSubribleDevice(Sensor sensor) {
        String sensorTemplate = String.format("{\n" +
                "       \"@id\" : \"http://inter-iot.eu/dev/SensorTemperatura/identificador#%s\",\n" +
                "       \"@type\" : \"InterIoT:GOIoTP#IoTDevice\",\n" +
                "       \"InterIoT:conversationId\" : \"%s\"\n" +
                "     }", sensor.getId(), "conversationId");
        return sensorTemplate;

    }

    private static String sensorsUnSubribleToGraphDevices(List<Sensor> sensors) {
        String str = "";
        for(int i =0; i< sensors.size(); i++) {
            str += sensorUnSubribleDevice(sensors.get(i));
            if(i < sensors.size() -1) {
                str += ",";
            }
        }
        return str;
    }

    private static String sensorsSubribleToGraphDevices(List<Sensor> sensors) {
        String str = "";
        for(int i =0; i< sensors.size(); i++) {
            str += sensorSubribleDevice(sensors.get(i));
            if(i < sensors.size() -1) {
                str += ",";
            }
        }
        return str;
    }

    private static String sensorToGraphDevice(Sensor sensor) {
        String sensorTemplate = String.format("{\n" +
                "      \"@id\" : \"http://inter-iot.eu/dev/SensorTemperatura/identificador#%s\",\n" +
                "      \"@type\" : \"InterIoT:GOIoTP#IoTDevice\",\n" +
                "      \"InterIoT:GOIoTP#hasLocation\" : {\n" +
                "        \"@id\" : \"http://test.inter-iot.eu/TestLocation\"\n" +
                "      },\n" +
                "      \"InterIoT:GOIoTP#hasName\" : \"%s\",\n" +
                "      \"http://www.w3.org/ns/sosa/IsHostedBy\" : {\n" +
                "        \"@id\" : \"InterIoT:%s\"\n" +
                "      }\n" +
                "}", sensor.getId(), sensor.getName(), "openiot");


        return sensorTemplate;
    }

    private static String sensorsToGraphDevices(List<Sensor> sensors) {
        String str = "";
        for(int i =0; i< sensors.size(); i++) {
            str += sensorToGraphDevice(sensors.get(i));
            if(i < sensors.size() -1) {
                str += ",";
            }
        }
        return str;
    }

    private static void makeGETRequest(String restURL){
    	
    	try {

    		URL url = new URL(restURL);
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		conn.setRequestMethod("GET");
    		conn.setRequestProperty("Accept", "application/json");

    		if (conn.getResponseCode() != 200) {
    			throw new RuntimeException("Failed : HTTP error code : "
    					+ conn.getResponseCode());
    		}

    		BufferedReader br = new BufferedReader(new InputStreamReader(
    			(conn.getInputStream())));

    		String output;
    		System.out.println("Output from Server .... \n");
    		while ((output = br.readLine()) != null) {
    			System.out.println(output);
    		}

    		conn.disconnect();

    	  } catch (MalformedURLException e) {

    		e.printStackTrace();

    	  } catch (IOException e) {

    		e.printStackTrace();

    	  }

    	}
    
   

    private static List<Sensor> getPlatformSensor() {
        Sensor sensor1 = new Sensor();
        sensor1.setId(1L);
        sensor1.name("sensor1");
        sensor1.setSensorId("1");
        sensor1.setUnit("unit");

        Sensor sensor2 = new Sensor();
        sensor2.setId(2L);
        sensor2.name("sensor2");
        sensor2.setSensorId("2");
        sensor2.setUnit("unit");

        List<Sensor> list = new ArrayList<>();
        list.add(sensor1);
        list.add(sensor2);
        return list;
    }
   /* public static void main(String args[]) throws IOException, MessageException {

        System.out.println(thingSubribleMessage());
        System.out.println(thingUnsubribleMessage());

        System.out.println(platformRegisterMessage());
        System.out.println(platformUnRegisterMessage());


        System.out.println(thingRegisterMessage());
        System.out.println(thingUnRegisterMessage());



       // ThingRegisterTranslator.platformRegisterMessage();
        //ThingRegisterTranslator.platformUnRegisterMessage();
    }*/


	public Message platformCreateDevices(Message message, Platform platform, Publisher<Message> publisher) throws URISyntaxException, BridgeException, ClientProtocolException, IOException {
		
		List<IoTDevice> lstDevices= OpenIoTUtils.extractDevices(message);
		for (IoTDevice ioTDevice : lstDevices) {
            logger.debug("Sending create-device (start-to-manage) request to the platform for device {}...", ioTDevice.getDeviceId());
            URL url = new URL(platform.getBaseEndpoint(), "/create-device");
            HttpPost httpPost = new HttpPost(url.toURI());
            List<NameValuePair> postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("deviceId", ioTDevice.getDeviceId()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParameters, "UTF-8");
            httpPost.setEntity(entity);
            try {
                HttpResponse resp = httpClient.execute(httpPost);
                logger.debug("Received response from the platform: {}", resp.getStatusLine());
                if (resp.getStatusLine().getStatusCode() == 204) {
                    logger.debug("Device has been created.");
                } else if (resp.getStatusLine().getStatusCode() == 409) {
                    logger.debug("Device already exists.");
                } else {
                    throw new BridgeException("Invalid response code received from the platform: " + resp.getStatusLine().toString());
                }
            } finally {
                httpPost.releaseConnection();
            }
        }
        logger.debug("platformCreateDevice finished successfully.");
        return message;
        
	}

	private static class Device {
		
		public String deviceId;
		
		public Device() {
		}

		public String getDeviceId() {
			return deviceId;
		}

		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}
		
		
	}

	
}
