/**
 * ACTIVAGE. ACTivating InnoVative IoT smart living environments for AGEing well.
 * ACTIVAGE is a R&D project which has received funding from the European 
 * Union’s Horizon 2020 research and innovation programme under grant 
 * agreement No 732679.
 * 
 * Copyright (C) 2016-2018, by :
 * - Universitat Politècnica de València, http://www.upv.es/
 * 
 *
 * For more information, contact:
 * - @author <a href="mailto:majuse@upv.es">Matilde Julián</a>  
 * - Project coordinator:  <a href="mailto:coordinator@activage.eu"></a>
 *  
 *
 *    This code is licensed under the EPL license, available at the root
 *    application directory.
 */
package eu.interiot.intermw.bridge.openiot;

import eu.interiot.intermw.bridge.BridgeConfiguration;
import eu.interiot.intermw.bridge.abstracts.AbstractBridge;
import eu.interiot.intermw.bridge.exceptions.BridgeException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.message.Message;
import eu.interiot.message.MessagePayload;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.translators.syntax.openiot.OpenIoTTranslator;
import spark.Service;
import spark.Spark;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


@eu.interiot.intermw.bridge.annotations.Bridge(platformType = "http://inter-iot.eu/openiot/nuig")
public class OpenIoTBridgeOld extends AbstractBridge {
    private final Logger logger = LoggerFactory.getLogger(eu.interiot.intermw.bridge.openiot.OpenIoTBridgeOld.class);
    final static String PROPERTIES_PREFIX = "openiot-";
	Platform platForm;
    private HttpClient httpClient;
    ProcessRequests processReq ;

    public OpenIoTBridgeOld(BridgeConfiguration configuration, Platform platform) throws MiddlewareException, MalformedURLException {
    	super(configuration, platform);
        this.platForm=platform;
        httpClient = HttpClientBuilder.create().build();
        processReq =new ProcessRequests(configuration,httpClient);
        logger.debug("OPENIOT bridge is initializing...");
        if (bridgeCallbackUrl == null) { // From the AbstractBridge class
            throw new BridgeException("Invalid OPENIOT bridge configuration.");
        }
        logger.info("OPENIOT bridge has been initialized successfully.");
    }
    
	@Override
	public Message registerPlatform(Message message) throws Exception {
		logger.info("registering open-iot bridge {}...",platform.getPlatformId() );
        Message responseMessage = processReq.platformRegisterMessage(message, this.platForm, publisher);
        return createResponseMessage(responseMessage);
	}
	
	@Override
	public Message unregisterPlatform(Message message) throws Exception {
        Message responseMessage = processReq.platformUnRegisterMessage(message, this.platForm, publisher);
        return createResponseMessage(responseMessage);
	}

	@Override
	public Message subscribe(Message message) throws Exception {
		logger.debug("subscribe() started.");
		logger.info("bridge callbackURL is set to {}", bridgeCallbackUrl);
		Message responseMessage= processReq.thingSubribleMessage(message, this.platForm,bridgeCallbackUrl,publisher);
        return createResponseMessage(responseMessage);
    }
    
	@Override
	public Message unsubscribe(Message message) throws Exception {
		logger.info("unsubscribing the conversation {} from the platform {}....", 
				message.getMetadata().asPlatformMessageMetadata().getSubscriptionId().get()
				,platform.getPlatformId());
		Message responseMessage= processReq.thingUnsubribleMessage(message, this.platForm,publisher);
		return createResponseMessage(responseMessage);
	}
	
	@Override
	public Message query(Message message) throws Exception {
		Message responseMessage = createResponseMessage(message);
		try{
			List<IoTDevice> devices = OpenIoTUtils.extractDevices(message);
			for (IoTDevice iotDevice : devices) {
				String thingId[] = OpenIoTUtils.filterThingID(iotDevice.getDeviceId());
				String responseBody = "";//client.query(thingId[0], thingId[1], thingId[2]);
				OpenIoTTranslator translator = new OpenIoTTranslator();
				// Create the model from the response JSON
				Model translatedModel = translator.toJenaModel(responseBody);
				// Create a new message payload for the response message
				MessagePayload responsePayload = new MessagePayload(translatedModel);
				// Attach the payload to the message
				responseMessage.setPayload(responsePayload);
			}
			// Set the OK status
			responseMessage.getMetadata().setStatus("OK");
			// Publish the message to INTER-MW
			publisher.publish(responseMessage);
		}
		catch (Exception e) {
			logger.error("Error in query: " + e.getMessage());
			responseMessage.getMetadata().setStatus("KO");
			responseMessage.getMetadata().addMessageType(URIManagerMessageMetadata.MessageTypesEnum.ERROR);
			responseMessage.getMetadata().asErrorMessageMetadata().setExceptionStackTrace(e);
			responseMessage.getMetadata().asErrorMessageMetadata().setErrorDescription(e.toString());
			responseMessage.getMetadata().asErrorMessageMetadata().setOriginalMessage(message.toString());
		}
		return responseMessage;
	}
	
	@Override
	public Message listDevices(Message message) throws Exception {
		Message responseMessage = createResponseMessage(message);
		try{
			logger.debug("ListDevices started...");
			// should return the registered devices in the IOT platform
			responseMessage= processReq.thingListDevices(message, platForm,publisher);
		}
		catch (Exception e) {
			logger.error("Error in query: " + e.getMessage());
			e.printStackTrace();
			responseMessage.getMetadata().setStatus("ERROR");
			responseMessage.getMetadata().addMessageType(URIManagerMessageMetadata.MessageTypesEnum.ERROR);
			responseMessage.getMetadata().asErrorMessageMetadata().setExceptionStackTrace(e);
			responseMessage.getMetadata().asErrorMessageMetadata().setErrorDescription(e.toString());
			responseMessage.getMetadata().asErrorMessageMetadata().setOriginalMessage(message.toString());
		}
		return createResponseMessage(responseMessage);
	}
	
	@Override
	public Message platformCreateDevices(Message message) throws Exception {
		logger.info("platform create devices request received in open-iot bridge");
		Message responseMessage= processReq.platformCreateDevices(message, this.platForm,publisher);
		return createResponseMessage(responseMessage);
	}
	
	@Override
	public Message platformUpdateDevices(Message message) throws Exception {
		// TODO: UPDATE VIRTUAL DEVICES
		return null;
	}
	
	@Override
	public Message platformDeleteDevices(Message message) throws Exception {
		// DELETE VIRTUAL DEVICES
		Message responseMessage = createResponseMessage(message);
		try {
			logger.debug("Removing devices...");
			List<IoTDevice> devices = OpenIoTUtils.extractDevices(message);
			for (IoTDevice iotDevice : devices) {
				//client.deleteDevice(iotDevice);
				logger.debug("Device {} has been removed.", iotDevice.getDeviceId());
			}
			responseMessage.getMetadata().setStatus("OK");
		} 
		catch (Exception e) {
			logger.error("Error removing devices: " + e.getMessage());
			responseMessage.getMetadata().setStatus("KO");
			responseMessage.getMetadata().addMessageType(URIManagerMessageMetadata.MessageTypesEnum.ERROR);
			responseMessage.getMetadata().asErrorMessageMetadata().setExceptionStackTrace(e);
			responseMessage.getMetadata().asErrorMessageMetadata().setErrorDescription(e.toString());
			responseMessage.getMetadata().asErrorMessageMetadata().setOriginalMessage(message.toString());
		}
		return responseMessage;
	}
	
	@Override
	public Message observe(Message message) throws Exception {
		// TRANSLATE MESSAGE PAYLOAD TO FORMAT X AND SEND IT TO PLATFORM
		Message responseMessage = createResponseMessage(message);
		try{
			logger.debug("Sending observation to the platform {}...", platForm.getPlatformId());
			OpenIoTTranslator translator = new OpenIoTTranslator();
			String body = translator.toFormatX(message.getPayload().getJenaModel());
			// Get ontology and data for update
			String ontName = OpenIoTUtils.getOntName(body);
		    String data = OpenIoTUtils.getUpdateData(body);
		    logger.debug("OPENIOT ontology: " + ontName);
		    logger.debug("Observation data: " + data);
		}catch(Exception ex){
			logger.error("Error in observe: " + ex.getMessage());
			throw ex;
		}
		return responseMessage;
	}
	
	@Override
	public Message actuate(Message message) throws Exception {
		// TRANSLATE MESSAGE PAYLOAD TO FORMAT X AND SEND IT TO PLATFORM
		Message responseMessage = createResponseMessage(message);
		try{
			logger.debug("Sending actuation to the platform {}...", platForm.getPlatformId());
			OpenIoTTranslator translator = new OpenIoTTranslator();
			String body = translator.toFormatX(message.getPayload().getJenaModel());
			// Get ontology and data for update
			String ontName = OpenIoTUtils.getOntName(body);
		    String data = OpenIoTUtils.getUpdateData(body);
		    logger.debug("OPENIOT ontology: " + ontName);
		    logger.debug("Actuation data: " + data);
		}catch(Exception ex){
			logger.error("Error in actuate: " + ex.getMessage());
			throw ex;
		}
		return responseMessage;
	}

	@Override
	public Message error(Message message) throws Exception {
		logger.debug("Error occured in {}...", message);
		Message responseMessage = createResponseMessage(message);
		responseMessage.getMetadata().setStatus("KO");
		return responseMessage;
	}

	@Override
	public Message unrecognized(Message message) throws Exception {
		logger.debug("Unrecognized message type.");
		Message responseMessage = createResponseMessage(message);
		responseMessage.getMetadata().setStatus("OK");
		return responseMessage;
	}

	@Override
	public Message updatePlatform(Message arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
