#  OponIoT Bridge
OpenIoT bridge is a plug-inbased component in SIL. The purpose of OpenIoT is two folded: (i) upstreaming and downstreaming messaging (ii) translating messages. 

## Getting stated
After deploying SIL component in AIoTES 2.0, the OpenIoT bridge must be installed inside the docker component (i.e., inermw-container). Following files need to be copied in their respective docker directories
*  openiot.bridge-2.3.0-SNAPSHOT.jar  & syntactic-translators-1.0.jar in  "<intermw-container>:/usr/local/tomcat/webapps/ROOT/WEB-INF/lib" directory
* OpenIoTBridge.properties  in "<intermw-container>:/etc/inter-iot/intermw" directory

Note: syntactic-translators-1.0.jar can be found here (library, available at https://git.activageproject.eu/Bridges_Binaries/Libraries)

The OpenIoT bridge does not required any other libraries.

Following are the limitations of current version:
* Create, update, and delete devices is not supported yet


### Bridge configuration
Create an OpenIoTBridge.properties file copy following text and replace the IP address with the machine address.
```
scan-packages=eu.interiot
bridge.callback.url=http://IP
```
### Build bridge

To build the OpenIoT bridge:
1. Clone repository
2. Skip all unitary tests in the compilation and use the jar obtained. You can find SNAPSHOT in /target folder
Run this command to generate jar file:  `$ mvn clean package -DskipTests`


### Dependencies 
You have to copy into the INTER-MW library (i.e. lib directory) all runtime dependencies that are required by the bridge project and do not already exist there. Make sure that dependencies added to INTER-MW library do not cause dependency conflict. The mw.bridges.api dependency together with its sub-dependencies are already included in the INTER-MW library.

You can get a list of all dependencies available in INTER-MW library by listing lib directory content:
`$ docker exec <intermw-container> ls -l /usr/local/tomcat/webapps/ROOT/WEB-INF/lib`
To display dependency tree for the bridge project (runtime dependencies), run the following command:
`$ mvn dependency:tree -Dscope=runtime`

## Testing
JUnit tests are provided with the bridge code. These tests can be adapted to test new functionalities.
## Further information

### Tutorial

**Create Client**
After deploying the OpenIoT bridge in SIL, the very first step is to register a client in SIL component. Following example shows how a client is registered. Paramter ClientId must have an 'Id', the second one is the callback, and the third is receivingCapacity. The third parameter shows how many messages in a a group user wants to receive (i.e., upstreaming) where integerValue represents the numbers.
```
{
        "clientId": "id",
        "callbackUrl": "https://activage.datascienceinstitute.ie:8081/sil/callback",
        "receivingCapacity": integerValue,
        "responseFormat": "JSON_LD",
        "responseDelivery": "CLIENT_PULL"
    }
```
**OpenIoT platform registration**
When client is registered, the next step is to register a platform in the SIL using the POST /mw2mw/platforms operation. Following parameters are provided (some are mendatory and some of them are optional):
* **platformId**: the id that will be assigned to the registered platform. It has to conform to the format “http://{DS_CODE}.inter-iot.eu/platforms/{id}”, where DS_CODE is the acronym for the deployment site in ActivAge and ‘id’ is an internal identifier for the platform (eg, ‘openiot’)
* **type**: this is the bridge type to use (http://inter-iot.eu/openiot) . This label can be obtained from /get platform-types or from the Table of platform types on the main/general guide. Check that the expected platform type is shown using GET platform-types in the API to confirm that the 'openiot' bridge has been integrated correctly.
* **baseEndpoint**: it refers to Orion’s address. It should be an URL (e.g., ‘http://{openiot-platform_ipaddress}:{port}).
* **location**: internal attribute used by Inter-Iot to give the geographic location of the platform. This field is optional, but in case it is provided, it has to be an URL.
* **Name**: a label to identify the platform
* **downstreamInputXXX/upstreamInputXXX**: these fields are used to assignt the corresponding upstream and downstream alignments to your platform instance.
 
Example JSON object:
```
 {
  "platformId": "http://inter-iot.eu/platforms/openiot",
  "type": "http://inter-iot.eu/openiot",
  "baseEndpoint": "http://srvgal106.deri.ie:8018",
  "location": "http://inter-iot.eu/activage-server",
  "name": “openIoT production platform",
  "downstreamInputAlignmentName": "",
  "downstreamInputAlignmentVersion": "",
  "downstreamOutputAlignmentName": "",
  "downstreamOutputAlignmentVersion": "",
  "upstreamInputAlignmentName": "",
  "upstreamInputAlignmentVersion": "",
  "upstreamOutputAlignmentName": "",
  "upstreamOutputAlignmentVersion": ""
}
```


(If you have alignments, you should fill the name and version in this json following the indications from the general guide)

The REST API operation returns 202 (Accepted) response code. To make sure the process has executed successfully, check the response message and the logs.

**subscribe**
When platform is registered, the next step is to subscribe the number and types of devices client is expected to recieve the observations from. Following example shows a susbcription message:

```
{
  "deviceIds": [
    "http://srvgal106.deri.ie:8018/api/53",
    "if more devices has to add"
  ]
}
```

## License
The OpenIoT bridge is licensed under [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).



