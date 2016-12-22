package com.ardic.android.iot.hwnodeapptemplate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ardic.android.iot.hwnodeapptemplate.service.WifiNodeService;

/**
 * Created by yavuz.erzurumlu on 04.11.2016.
 */

public class ConnectionReceiver extends BroadcastReceiver {

    private static final String TAG = ConnectionReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Connection state is changed!!! Restarting Nsd Service...");

        if (WifiNodeService.getInstance() != null) {
            WifiNodeService.getInstance().restartManagement();
        }
    }
}
