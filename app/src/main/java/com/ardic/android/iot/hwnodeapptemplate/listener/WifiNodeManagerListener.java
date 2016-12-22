package com.ardic.android.iot.hwnodeapptemplate.listener;

import com.ardic.android.iot.hwnodeapptemplate.base.BaseWifiNodeDevice;
import com.ardic.android.iotignite.exceptions.UnsupportedVersionException;

public interface WifiNodeManagerListener {
    void onWifiNodeDeviceAdded(BaseWifiNodeDevice device);
    void onIgniteConnectionChanged(boolean isConnected);
}
