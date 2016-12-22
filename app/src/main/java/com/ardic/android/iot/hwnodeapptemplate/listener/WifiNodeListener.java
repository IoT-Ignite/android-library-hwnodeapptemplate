package com.ardic.android.iot.hwnodeapptemplate.listener;


import com.ardic.android.iot.hwnodeapptemplate.object.WifiNode;

public interface WifiNodeListener {
    void onWifiNodeAdded(WifiNode device);
    void onWifiNodeAddressChange(WifiNode device);
}
