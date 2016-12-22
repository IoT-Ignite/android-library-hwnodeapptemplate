package com.ardic.android.iot.hwnodeapptemplate.node;

import android.content.Context;
import android.text.TextUtils;

import com.ardic.android.iot.hwnodeapptemplate.base.BaseWifiNodeDevice;
import com.ardic.android.iot.hwnodeapptemplate.object.WifiNode;
import com.ardic.android.iotignite.nodes.IotIgniteManager;

/**
 * Created by yavuz.erzurumlu on 05.11.2016.
 */

public class GenericWifiNodeDevice extends BaseWifiNodeDevice {

    private boolean isRunning = false;

    public GenericWifiNodeDevice(IotIgniteManager igniteContext, Context mContext, WifiNode device) {
        super(igniteContext,
                mContext,
                !TextUtils.isEmpty(device.getNodeType()) ? device.getNodeType() + " - " + device.getHolder().getNodeId() : device.getHolder().getNodeId(),
                device);
    }

    @Override
    public synchronized void start() {
        if (!isRunning) {
            isRunning = true;
            super.start();
        }
    }

    @Override
    public synchronized void stop() {
        if (isRunning) {
            isRunning = false;
            super.stop();
        }
    }
}
