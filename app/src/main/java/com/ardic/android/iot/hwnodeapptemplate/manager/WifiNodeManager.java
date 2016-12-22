package com.ardic.android.iot.hwnodeapptemplate.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.connectivitylib.listener.NsdServiceListener;
import com.ardic.android.iot.hwnodeapptemplate.base.BaseWifiNodeManager;
import com.ardic.android.iot.hwnodeapptemplate.listener.WifiNodeListener;
import com.ardic.android.iot.hwnodeapptemplate.listener.WifiNodeManagerListener;
import com.ardic.android.iot.hwnodeapptemplate.object.WifiNode;
import com.ardic.android.iot.hwnodeapptemplate.service.NSDHelperService;
import com.ardic.android.iotignite.exceptions.AuthenticationException;
import com.ardic.android.iotignite.nodes.IotIgniteManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yavuz.erzurumlu on 04.11.2016.
 */

public class WifiNodeManager implements NsdServiceListener {

    private static final String TAG = WifiNodeManager.class.getSimpleName();
    private static WifiNodeManager instance;
    private static Context mContext;
    private static IotIgniteManager mIotIgniteManager;
    private static NSDHelperService mNsdHelper;
    private static String deviceID;
    private TcpServerManager tcpServerManager = new TcpServerManager();
    private List<BaseWifiNodeManager> managers = new CopyOnWriteArrayList<>();
    private IpHostListenerClass deviceListener = new IpHostListenerClass();

    private WifiNodeManager(Context context, IotIgniteManager igniteContext) {
        mContext = context;
        mIotIgniteManager = igniteContext;
        getDeviceID();
        mNsdHelper = new NSDHelperService(deviceID, this);

    }

    public static IotIgniteManager getIotIgniteManager() {
        return mIotIgniteManager;
    }

    public static void setIotIgniteManager(IotIgniteManager mIotIgniteManager) {
        WifiNodeManager.mIotIgniteManager = mIotIgniteManager;
    }

    public static synchronized WifiNodeManager getInstance(Context context, IotIgniteManager igniteContext) {
        if (instance == null && context != null && igniteContext != null) {
            instance = new WifiNodeManager(context, igniteContext);
        }
        return instance;
    }

    public synchronized void startManagement() {
        mNsdHelper.startNSDService();
        TcpServerManager.addListener(deviceListener);
        for (BaseWifiNodeManager mngr : managers) {
            mngr.startManagement();

        }
    }

    public synchronized void stopManagement() {
        mNsdHelper.stopNSDService();
        TcpServerManager.removeListener(deviceListener);
        for (BaseWifiNodeManager mngr : managers) {
            mngr.stopManagement();
        }
    }

    // Use only when device connection changed.

    public synchronized void restartManagement() {
        mNsdHelper.restartNSDService();
        stopManagement();
        startManagement();
    }

    private void getDeviceID() {
        try {
            deviceID = mIotIgniteManager.getDeviceID();
        } catch (AuthenticationException e) {
            Log.i(TAG, "AuthenticationException on getDeviceID() function");
        }
    }

    @Override
    public void onServiceRegistered() {
        Log.i(TAG, "Service registered !!!!!!!!!!!!!!!!");
        tcpServerManager.start();
    }

    @Override
    public void onServiceUnregistered() {
        tcpServerManager.stop();
    }

    @Override
    public void onRegistrationFailure() {
        //TODO:stop tcp server if started
        Log.i(TAG, "NSD Registration Failed! Stopping Service...");
        tcpServerManager.stop();
    }

    @Override
    public void onUnregistrationFailure() {
        //TODO: dont stop tcp server.
    }

    public synchronized void addManager(BaseWifiNodeManager manager) {
        if (manager != null && !managers.contains(manager)) {
            managers.add(manager);
        }
    }

    public synchronized void removeManager(BaseWifiNodeManager manager) {
        if (manager != null && managers.contains(manager)) {
            managers.remove(manager);
        }
    }


    public void sendIgniteConnectionChanged(boolean isConnected) {
        for (BaseWifiNodeManager manager : managers) {
            for (WifiNodeManagerListener listener : manager.getWifiNodeManagerListeners()) {
                listener.onIgniteConnectionChanged(isConnected);
            }
        }
    }

    private class IpHostListenerClass implements WifiNodeListener {
        private BaseWifiNodeManager getManager(WifiNode device) {
            BaseWifiNodeManager genericManager = null;
            if (device != null) {
                for (BaseWifiNodeManager manager : managers) {
                    if (manager instanceof GenericWifiNodeManager) {
                        genericManager = manager;
                    }
                    if (TextUtils.equals(manager.getNodeType(), device.getNodeType())) {
                        return manager;
                    }
                }
            }

            return genericManager;
        }


        @Override
        public void onWifiNodeAddressChange(WifiNode device) {
            BaseWifiNodeManager manager = getManager(device);
            if (manager != null) {
                manager.onIpAddressChange(device);
            }
        }

        @Override
        public void onWifiNodeAdded(WifiNode device) {
            BaseWifiNodeManager manager = getManager(device);
            if (manager == null) {
                GenericWifiNodeManager generic = GenericWifiNodeManager.getInstance(mContext);
                generic.setIgniteManager(mIotIgniteManager);
                generic.onAdded(device);
                generic.startManagement();
                managers.add(generic);
            } else {
                manager.onAdded(device);
            }
        }
    }
}
