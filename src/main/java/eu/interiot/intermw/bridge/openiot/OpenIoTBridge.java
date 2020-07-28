package eu.interiot.intermw.bridge.openiot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.interiot.intermw.bridge.BridgeConfiguration;
import eu.interiot.intermw.bridge.abstracts.AbstractBridge;
import eu.interiot.intermw.bridge.annotations.Bridge;
import eu.interiot.intermw.bridge.exceptions.BridgeException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.intermw.commons.model.enums.IoTDeviceType;
import eu.interiot.intermw.commons.requests.SubscribeReq;
import eu.interiot.intermw.commons.requests.UnsubscribeReq;
import eu.interiot.intermw.commons.requests.UpdatePlatformReq;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.ID.PropertyID;
import eu.interiot.message.Message;
import eu.interiot.message.MessageMetadata;
import eu.interiot.message.MessagePayload;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata.MessageTypesEnum;
import eu.interiot.message.metadata.PlatformMessageMetadata;
import eu.interiot.message.payload.types.IoTDevicePayload;
import eu.interiot.translators.syntax.openiot.TranslateObsrToRDF;
import eu.interiot.translators.syntax.openiot.domain.Sensor;
import eu.interiot.intermw.commons.model.IoTDevice;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;


@Bridge(platformType = "http://inter-iot.eu/openiot/nuig")
public class OpenIoTBridge extends AbstractBridge {
    
    private final static String PLATFORM_URL = "http://srvgal106.deri.ie:8018/api/";

    private final Logger logger = LoggerFactory.getLogger(OpenIoTBridge.class);
    private HttpClient httpClient;
    private Client client;
    private WebTarget platformTarget;


    public OpenIoTBridge(BridgeConfiguration configuration, Platform platform) throws MiddlewareException, URISyntaxException {
        super(configuration, platform);
        logger.debug("OpenIoT bridge is initializing...");
        httpClient = HttpClientBuilder.create().build();
        // an example how to retrieve a property from bridge configuration file
        Properties myProperty = configuration.getProperties();
     
        client = ClientBuilder.newClient();

        logger.info("OpenIoT bridge has been initialized successfully.");
    }

    @Override
    public Message registerPlatform(Message message) throws Exception {
        logger.debug("Registering platform {}...", platform.getPlatformId());
        // note: platform object is set by the AbstractBridge constructor when instantiating a new bridge, it
        // contains data extracted from the REGISTER_PLATFORM message. On the other hand the raw
        // REGISTER_PLATFORM message is given as a message parameter of registerPlatform method.

        platformTarget = client.target(platform.getBaseEndpoint().toURI());

        if (message.getMetadata().getMessageTypes().contains(MessageTypesEnum.SYS_INIT)) {
            // restore platform registration after INTER-MW restart. Registration request shouldn't be sent to the platform again.
            logger.debug("Registration of the platform {} has been restored.", platform.getPlatformId());
            return createResponseMessage(message);

        } else {
           
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
            return createResponseMessage(message);
        }
    }

    @Override
    public Message updatePlatform(Message message) throws Exception {
        UpdatePlatformReq req = new UpdatePlatformReq(message);
        platform = req.getPlatform();
        platformTarget = client.target(platform.getBaseEndpoint().toURI());
        logger.debug("Platform {} registration has been updated successfully.", platform.getPlatformId());
        
        return createResponseMessage(message);
    }

    @Override
    public Message unregisterPlatform(Message message) throws Exception {
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
        return createResponseMessage(message);
    }

    @Override
    public Message subscribe(Message message) throws Exception {
        SubscribeReq req = new SubscribeReq(message);
        String conversationId = req.getConversationId();
        List<String> deviceIds = OpenIoTUtils.extractDeviceIds(message);
        
        if (message.getMetadata().getMessageTypes().contains(MessageTypesEnum.SYS_INIT)) {
            // restore subscription after INTER-MW restart. Subscription is still active on the platform so the
            // subscribe request shouldn't be sent again.
            logger.debug("Restoring subscription {}...", conversationId);
            createObservationsListener(conversationId);
            logger.debug("Subscription {} has been restored.", conversationId);
            return createResponseMessage(message);

        } else {
            
            logger.debug("Subscribing to devices {} with conversationId {}...", deviceIds, conversationId);
            URL url = new URL(platform.getBaseEndpoint(),"/api/subscriptions"); 
            HttpPost httpPost = new HttpPost(url.toURI());
            JsonObject jsonObj= new JsonObject();
            String file =bridgeCallbackUrl.getFile().endsWith("/") ? bridgeCallbackUrl.getFile()+conversationId : bridgeCallbackUrl.getFile()+"/"+conversationId;
            URL convCallbackUrl = new URL(bridgeCallbackUrl.getProtocol(), bridgeCallbackUrl.getHost(), bridgeCallbackUrl.getPort(), file, null);
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

            //call the observation listener
            createObservationsListener(conversationId);
            logger.debug("Subscribed successfully to devices {} with conversationId {}...", deviceIds, conversationId);
            return createResponseMessage(message);
        }
    }

    private void createObservationsListener(String conversationId) throws MalformedURLException {
        logger.debug("Creating callback listener for conversation {} listening at {}...", conversationId,
                new URL(bridgeCallbackUrl, conversationId));

        String path= "/*";
        
        Spark.post(conversationId, (request, response) -> {
            logger.debug("Observation received from the Example platform {}: {}", platform.getPlatformId(), request.body());
            try {
               
                // create message metadata
                PlatformMessageMetadata metadata = new MessageMetadata().asPlatformMessageMetadata();
                metadata.initializeMetadata();
                metadata.addMessageType(URIManagerMessageMetadata.MessageTypesEnum.OBSERVATION);
                metadata.setSenderPlatformId(new EntityID(platform.getPlatformId()));
                metadata.setConversationId(conversationId);

                // create message payload
                
                String observation = request.body();
                System.err.println("observation: "+ observation);
                Model output = TranslateObsrToRDF.toJenaModel(observation);//translate.toJenaModel(observation);
                
                MessagePayload messagePayload = new MessagePayload(output);
                Message observationMessage = new Message();
                
                //get the prefixes from the model and add them into @context
                for(Entry<String, String> prefix: output.getNsPrefixMap().entrySet())
                {
                	observationMessage.setPrefix(prefix.getKey(), prefix.getValue());
                }
                observationMessage.setMetadata(metadata);
                observationMessage.setPayload(messagePayload);
                publisher.publish(observationMessage);
                logger.debug("Observation message {} has been published upstream through Inter MW.", observationMessage.getMetadata().getMessageID().get());

                response.status(204);
                return "";

            } catch (Exception e) {
                logger.debug("Failed to handle observation with conversationId " + conversationId + ": " + e.getMessage(), e);
                response.status(400);
                return "Failed to handle observation: " + e.getMessage();
            }
        });

        logger.debug("spark is listning at port {}",Spark.port());
    }

    @Override
    public Message unsubscribe(Message message) throws Exception {
        UnsubscribeReq req = new UnsubscribeReq(message);
        String conversationId = req.getSubscriptionId(); // conversation ID to unsubscribe from
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

        return createResponseMessage(message);
    }

    @Override
    public Message query(Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message listDevices(Message message) throws Exception {
        
        List<Sensor> devices = null;
        try {
          
           platformTarget=client.target(platform.getBaseEndpoint().toString());
            devices = platformTarget
                    .path("/api/sensors")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<List<Sensor>>() {
                    });
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
        return createResponseMessage(message);
    }

    @Override
    public Message platformCreateDevices(Message message) throws Exception
        {
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
        return createResponseMessage(message);
    }

    @Override
    public Message platformUpdateDevices(Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message platformDeleteDevices(Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message observe(Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message actuate(Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message error(Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message unrecognized(Message message) {
        throw new UnsupportedOperationException();
    }

}

