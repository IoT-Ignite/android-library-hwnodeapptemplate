package com.ardic.android.iot.hwnodeapptemplate.manager;

import android.content.Context;
import android.util.Log;

import com.ardic.android.iot.hwnodeapptemplate.base.BaseWifiNodeManager;
import com.ardic.android.iot.hwnodeapptemplate.node.GenericWifiNodeDevice;
import com.ardic.android.iot.hwnodeapptemplate.object.WifiNode;
import com.ardic.android.iotignite.nodes.IotIgniteManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GenericWifiNodeManager extends BaseWifiNodeManager {

    private static final String TAG = GenericWifiNodeManager.class.getSimpleName();
    private static GenericWifiNodeManager instance;
    private CopyOnWriteArrayList<GenericWifiNodeDevice> genericWifiNodeDeviceList = new CopyOnWriteArrayList<>();


    private GenericWifiNodeManager(Context context) {
        super(context, BaseWifiNodeManager.GENERIC);
        registerDevices();
    }

    public static synchronized GenericWifiNodeManager getInstance(Context context) {
        if (instance == null) {
            instance = new GenericWifiNodeManager(context);
        }

        return instance;
    }

    public void setIgniteManager(IotIgniteManager igniteManager) {
        this.igniteContext = igniteManager;
    }

    @Override
    public void startManagement() {
        for (GenericWifiNodeDevice genericWifiNodeDevice : genericWifiNodeDeviceList) {
            genericWifiNodeDevice.start();
        }
    }

    @Override
    public void stopManagement() {
        for (GenericWifiNodeDevice genericWifiNodeDevice : genericWifiNodeDeviceList) {
            genericWifiNodeDevice.stop();
        }
    }

    @Override
    public void onAdded(WifiNode device) {
        Log.d(TAG, "onAdded: " + device.getIpAddress());
        GenericWifiNodeDevice genericWifiNodeDevice = new GenericWifiNodeDevice(igniteContext, mContext, device);
        if (!genericWifiNodeDeviceList.contains(genericWifiNodeDevice)) {
            genericWifiNodeDeviceList.add(genericWifiNodeDevice);
            genericWifiNodeDevice.start();
            sendWifiNodeDeviceAdded(genericWifiNodeDevice);
        }
    }

    @Override
    public void onIpAddressChange(WifiNode device) {
        Log.d(TAG, "onIpAddressChange: " + device.getIpAddress());
        GenericWifiNodeDevice genericWifiNodeDevice = new GenericWifiNodeDevice(igniteContext, mContext, device);
        boolean isFound = false;
        for (int i = 0; i < genericWifiNodeDeviceList.size(); i++) {
            if (genericWifiNodeDeviceList.get(i).getWifiNodeDevice().equals(device)) {
                genericWifiNodeDeviceList.get(i).stop();
                genericWifiNodeDeviceList.add(genericWifiNodeDevice);
                genericWifiNodeDevice.start();
                sendWifiNodeDeviceAdded(genericWifiNodeDevice);
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            Log.d(TAG, device.getUniqueID() + " not found in list");
        }

    }

    @Override
    protected void registerDevices() {
        List<WifiNode> wifiNodeList = TcpServerManager.getWifiNodeList();
        for (WifiNode wifiNodeDevice : wifiNodeList) {
            GenericWifiNodeDevice genericWifiNodeDevice = new GenericWifiNodeDevice(igniteContext, mContext, wifiNodeDevice);
            genericWifiNodeDeviceList.add(genericWifiNodeDevice);
        }
    }

    public List<GenericWifiNodeDevice> getWifiNodeDeviceList() {
        return this.genericWifiNodeDeviceList;
    }
}
