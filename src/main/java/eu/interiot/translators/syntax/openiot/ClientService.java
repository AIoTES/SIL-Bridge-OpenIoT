package eu.interiot.translators.syntax.openiot;


import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.translators.syntax.openiot.domain.Platform;
import eu.interiot.translators.syntax.openiot.domain.Sensor;

public interface ClientService {

    public Sensor registerDevice(Sensor sensor);
    public Sensor unRegisgterDevice(Sensor sensor);
    public Platform registerPlatform(Platform platform);
    public Platform unregisterPlatform(Platform platform);
  
    public void unregisterPlatform(String platformId);


    void registerPlatformById(String platformId);

    void registerDevice(IoTDevice iotDevice);

    void deleteDevice(IoTDevice iotDevice);
}
