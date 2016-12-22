package com.ardic.android.iot.hwnodeapptemplate.base;

import android.content.Context;

import com.ardic.android.iot.hwnodeapptemplate.listener.WifiNodeManagerListener;
import com.ardic.android.iot.hwnodeapptemplate.node.GenericWifiNodeDevice;
import com.ardic.android.iot.hwnodeapptemplate.object.WifiNode;
import com.ardic.android.iotignite.nodes.IotIgniteManager;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BaseWifiNodeManager {

    protected static final String GENERIC = "GENERIC IOT IGNITE NODE MANAGER";
    protected Context mContext;
    protected IotIgniteManager igniteContext;
    private String nodeType;
    private CopyOnWriteArrayList<WifiNodeManagerListener> wifiNodeManagerListeners = new CopyOnWriteArrayList<>();

    public BaseWifiNodeManager(Context context, IotIgniteManager igniteContext, String nodeType) {
        this.mContext = context;
        this.igniteContext = igniteContext;
        this.nodeType = nodeType;
    }

    public BaseWifiNodeManager(Context context, String nodeType) {
        this(context, null, nodeType);
    }

    public void addWifiNodeManagerListener(WifiNodeManagerListener listener) {
        if (listener != null && !wifiNodeManagerListeners.contains(listener)) {
            wifiNodeManagerListeners.add(listener);
        }
    }

    public void removeWifiNodeManagerListener(WifiNodeManagerListener listener) {
        if (listener != null && wifiNodeManagerListeners.contains(listener)) {
            wifiNodeManagerListeners.remove(listener);
        }
    }

    protected void sendWifiNodeDeviceAdded(GenericWifiNodeDevice device){
        for(WifiNodeManagerListener listener : wifiNodeManagerListeners){
            listener.onWifiNodeDeviceAdded(device);
        }
    }

    public CopyOnWriteArrayList<WifiNodeManagerListener> getWifiNodeManagerListeners() {
        return wifiNodeManagerListeners;
    }

    protected abstract void registerDevices();

    public abstract void onIpAddressChange(WifiNode device);

    public abstract void onAdded(WifiNode device);

    public abstract void startManagement();

    public abstract void stopManagement();

    public String getNodeType() {
        return nodeType;
    }
}
