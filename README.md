#  OponIoT Bridge
OpenIoT bridge is for the OpenIoT platform
=================================
## Getting stated
After deploying SIL component in AIoTES 2.0, the OpenIoT bridge must be installed inside the docker component (i.e., inermw-container). Following files need to be copied in their respective docker directories
*  openiot.bridge-2.3.0-SNAPSHOT.jar  & syntactic-translators-1.0.jar in  "<intermw-container>:/usr/local/tomcat/webapps/ROOT/WEB-INF/lib" directory
* OpenIoTBridge.properties  in "<intermw-container>:/etc/inter-iot/intermw" directory
The OpenIoT bridge does not required any other libraries.

=============================================================================================================================================================
## OpenIoT bridge configuration
Create an OpenIoTBridge.properties file copy following text and replace the IP address with the machine address.
scan-packages=eu.interiot
bridge.callback.url=http://10.196.2.109:8981
======================================================================================
## Further information



