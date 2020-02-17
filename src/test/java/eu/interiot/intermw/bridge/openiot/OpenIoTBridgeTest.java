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

import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import eu.interiot.intermw.bridge.BridgeConfiguration;
import eu.interiot.intermw.commons.DefaultConfiguration;
import eu.interiot.intermw.commons.interfaces.Configuration;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata;
import eu.interiot.message.managers.URI.URIManagerMessageMetadata.MessageTypesEnum;
import eu.interiot.translators.syntax.openiot.OpenIoTTranslator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Service;
import spark.Spark;

import java.net.URL;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class OpenIoTBridgeTest {
    private OpenIoTPlatformEmulator platformEmulator;
    //final static Logger logger = Logger.getLogger(OpenIoTBridgeTest.class);
    private final Logger logger = LoggerFactory.getLogger(OpenIoTBridgeTest.class);
    @Before
    public void setUp() throws Exception {
       // platformEmulator = new OpenIoTPlatformEmulator(4569, 5);
       // platformEmulator.start();
    }

    @After
    public void tearDown() {
       // platformEmulator.stop();
    }


    @Test
    public void testBridge() throws Exception {
        logger.info("--------------------start test bridge=================");

        Configuration configuration = new DefaultConfiguration("bridge.properties");
        
        BridgeConfiguration config= new BridgeConfiguration("bridge.properties", "http://openiot", configuration);

        URL callbackUrl = new URL(configuration.getProperty("bridge.callback.url"));
        int callbackPort = callbackUrl.getPort();
       
         Spark.port(8981);
         //logger.debug("SPARK is listening on port {}: ",Spark.port());
         //logger.debug("SPARK is listening on port {}: ",Service.ignite().port());
         
        // create Message objects from serialized messages
        URL url1 = Resources.getResource("messages/platform-register.json");
        String platformRegisterJson = Resources.toString(url1, Charsets.UTF_8);
        Message platformRegisterMsg = new Message(platformRegisterJson);

        System.err.println(platformRegisterMsg.getMetadata().prettyPrint());
       // logger.info("platformRegisterMsg: {}", JsonUtils.toPrettyString(platformRegisterMsg.getMetadata()));
       /* logger.info("platformRegisterMsg: {}", JsonUtils.toPrettyString(platformRegisterMsg.getPayloadAsGOIoTPPayload()));
        logger.info("platformRegisterMsg: {}", JsonUtils.toPrettyString(platformRegisterMsg.getPayload()));
        logger.info("platformRegisterMsg: {}", JsonUtils.toPrettyString(platformRegisterMsg.getMessageConfig()));
        logger.info("platformRegisterMsg: {}", JsonUtils.toPrettyString(platformRegisterMsg.getPrefixes()));
*/
        URL url2 = Resources.getResource("messages/thing-register.json");
        String thingRegisterJson = Resources.toString(url2, Charsets.UTF_8);
        Message thingRegisterMsg = new Message(thingRegisterJson);

        logger.info("thingRegisterMsg:" + thingRegisterMsg);

        URL url3 = Resources.getResource("messages/thing-subscribe.json");
        String thingSubscribeJson = Resources.toString(url3, Charsets.UTF_8);
        Message thingSubscribeMsg = new Message(thingSubscribeJson);

        logger.info("thingSubscribeMsg: {}" + thingSubscribeMsg);


        
      /*  URL url4 = Resources.getResource("messages/thing-unsubscribe.json");
        String thingUnsubscribeJson = Resources.toString(url4, Charsets.UTF_8);
        Message thingUnsubscribeMsg = new Message(thingUnsubscribeJson);

        logger.info("thingUnsubscribeMsg: {}" + thingUnsubscribeMsg);

//        URL url5 = Resources.getResource("messages/observe.json");
        URL url5 = Resources.getResource("messages/observe-UniversAAL.json");
        String observeJson = Resources.toString(url5, Charsets.UTF_8);
        Message observeMsg = new Message(observeJson);


        logger.info("observeMsg:" + observeMsg);*/
        
        URL url6 = Resources.getResource("messages/platform-unregister.json");
        String platformUnregisterJson = Resources.toString(url6, Charsets.UTF_8);
        Message platformUnregisterMsg = new Message(platformUnregisterJson);
        
        URL url7 = Resources.getResource("messages/discovery.json");
        String discoveryListJson = Resources.toString(url7, Charsets.UTF_8);
        Message discoveryListMsg = new Message(discoveryListJson);
        
        // create Platform object using platform-register message
        EntityID platformId = platformRegisterMsg.getMetadata().asPlatformMessageMetadata().getReceivingPlatformIDs().iterator().next();
        Platform platform = new Platform();
        platform.setPlatformId(platformId.toString());
        // SHOULD GET THESE VALUES FROM THE MESSAGE (AND SOME OF THEM FROM PROPERTIES)
        platform.setClientId("test");
        platform.setName("Example Platform #1");
        platform.setType("openiot");
        platform.setBaseEndpoint(new URL("http://srvgal106.deri.ie:8018"));
        platform.setLocation("http://test.inter-iot.eu/TestLocation");

        OpenIoTBridge openiotBridge = new OpenIoTBridge(config, platform);
        PublisherMock<Message> publisher = new PublisherMock<>();
        openiotBridge.setPublisher(publisher);

       
        // register platform
        openiotBridge.process(platformRegisterMsg);
        Message responseMsg = publisher.retrieveMessage();
        Set<MessageTypesEnum> messageTypesEnumSet = responseMsg.getMetadata().getMessageTypes();
       // assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.RESPONSE));
        assertTrue(messageTypesEnumSet.contains(URIManagerMessageMetadata.MessageTypesEnum.PLATFORM_REGISTER));
        
             // register thing
       /* openiotBridge.process(thingRegisterMsg);
        Message responseMsg2 = publisher.retrieveMessage();
        Set<MessageTypesEnum> messageTypesEnumSet2 = responseMsg2.getMetadata().getMessageTypes();
        assertTrue(messageTypesEnumSet2.contains(MessageTypesEnum.RESPONSE));
        assertTrue(messageTypesEnumSet2.contains(URIManagerMessageMetadata.MessageTypesEnum.PLATFORM_CREATE_DEVICE));
        */

        // Discovery
        openiotBridge.process(discoveryListMsg);
        // Get device_add messages
        Message deviceAddMsg = publisher.retrieveMessage();
        messageTypesEnumSet = deviceAddMsg.getMetadata().getMessageTypes();
        //assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.DEVICE_ADD_OR_UPDATE));
        // Get response message
        responseMsg = publisher.retrieveMessage();

       logger.info("list of devices json serialization: {}", deviceAddMsg.serializeToJSONLD());
        OpenIoTTranslator translator = new OpenIoTTranslator();
        
       logger.info("MessageTypesEnum.LIST_DEVICES:"  + MessageTypesEnum.LIST_DEVICES);
        messageTypesEnumSet = responseMsg.getMetadata().getMessageTypes();
        assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.RESPONSE));
      //  assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.LIST_DEVICES));
        
        
 

        // subscribe to thing
        openiotBridge.process(thingSubscribeMsg);
        responseMsg = publisher.retrieveMessage();
        
        messageTypesEnumSet = responseMsg.getMetadata().getMessageTypes();
        assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.RESPONSE));
      //  assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.SUBSCRIBE));

        // wait for observation message
        Long startTime = new Date().getTime();
        Message observationMsg = null;
        do {
            Thread.sleep(1000);
            observationMsg = publisher.retrieveMessage();
            if (observationMsg != null) {
            	System.out.println("JSON-LD: "+observationMsg.serializeToJSONLD());
            	messageTypesEnumSet = observationMsg.getMetadata().getMessageTypes();
            	System.out.println(messageTypesEnumSet);
            	break;
            }
        } while (true);

       /* if (observationMsg != null) {
            messageTypesEnumSet = observationMsg.getMetadata().getMessageTypes();
            System.out.println("observation form IOT: "+observationMsg.serializeToJSONLD());
            //OpenIoTTranslator tran= new OpenIoTTranslator();
            
//            assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.RESPONSE));
            assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.OBSERVATION));
        } else {
            fail("Timeout waiting for observation messages.");
        }*/
        
        /*// observe
        openiotBridge.process(observeMsg);
        responseMsg = publisher.retrieveMessage();
        messageTypesEnumSet = responseMsg.getMetadata().getMessageTypes();
        assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.RESPONSE));
        assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.OBSERVATION));
        
        // unsubscribe
        openiotBridge.process(thingUnsubscribeMsg);
        responseMsg = publisher.retrieveMessage();
        messageTypesEnumSet = responseMsg.getMetadata().getMessageTypes();
        assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.RESPONSE));
        assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.UNSUBSCRIBE));*/
        
        // Unregister Platform
      //  openiotBridge.process(platformUnregisterMsg);
      //  responseMsg = publisher.retrieveMessage();
      //  messageTypesEnumSet = responseMsg.getMetadata().getMessageTypes();
      //  assertTrue(messageTypesEnumSet.contains(MessageTypesEnum.RESPONSE));
      //  assertTrue(messageTypesEnumSet.contains(URIManagerMessageMetadata.MessageTypesEnum.PLATFORM_UNREGISTER));
    }
}
