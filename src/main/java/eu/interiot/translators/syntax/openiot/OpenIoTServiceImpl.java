package eu.interiot.translators.syntax.openiot;

import eu.interiot.intermw.commons.model.IoTDevice;
import eu.interiot.translators.syntax.openiot.domain.Platform;
import eu.interiot.translators.syntax.openiot.domain.Sensor;

public class OpenIoTServiceImpl implements  ClientService {
    @Override
    public Sensor registerDevice(Sensor sensor) {
        return null;
    }

    @Override
    public Sensor unRegisgterDevice(Sensor sensor) {
        return null;
    }

    @Override
    public Platform registerPlatform(Platform platform) {
        return null;
    }

    @Override
    public Platform unregisterPlatform(Platform platform) {
        return null;
    }

 

    @Override
    public void unregisterPlatform(String platformId) {

    }

    @Override
    public void registerPlatformById(String platformId) {
    }

    @Override
    public void registerDevice(IoTDevice iotDevice) {

    }

    @Override
    public void deleteDevice(IoTDevice iotDevice) {

    }
}
