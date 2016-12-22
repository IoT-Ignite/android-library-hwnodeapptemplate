package com.ardic.android.iot.hwnodeapptemplate.object;

/**
 * Created by yavuz.erzurumlu on 05.11.2016.
 */
public class ThingHolder {
    public String thingId;
    public String thingType;
    public String connectedPin;
    public String vendor;
    public String dataType;
    public boolean actuator;


    public String getThingId() {
        return thingId;
    }

    public void setThingId(String thingId) {
        this.thingId = thingId;
    }

    public String getThingType() {
        return thingType;
    }

    public void setThingType(String thingType) {
        this.thingType = thingType;
    }

    public String getConnectedPin() {
        return connectedPin;
    }

    public void setConnectedPin(String connectedPin) {
        this.connectedPin = connectedPin;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isActuator() {
        return actuator;
    }

    public void setActuator(boolean actuator) {
        this.actuator = actuator;
    }


}