package com.ardic.android.iot.hwnodeapptemplate.listener;

import com.ardic.android.iotignite.things.ThingConfiguration;
import com.ardic.android.iotignite.things.ThingData;

/**
 * Created by yavuz.erzurumlu on 29.11.2016.
 */

public interface ThingEventListener {
    void onDataReceived(String nodeId, String thingId, ThingData data);

    void onConnectionStateChanged(String nodeId, boolean state);

    void onActionReceived(String nodeId, String thingId, String action);

    void onConfigReceived(String nodeId, String thingId, ThingConfiguration config);

    void onUnknownMessageReceived(String nodeId, String message);

    void onNodeUnregistered(String nodeId);

    void onThingUnregistered(String nodeId, String thingId);
}
