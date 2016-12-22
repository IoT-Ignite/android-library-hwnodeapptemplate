package com.ardic.android.iot.hwnodeapptemplate.utils;

import android.util.Log;

import com.ardic.android.iot.hwnodeapptemplate.constant.JsonKeys;
import com.ardic.android.iot.hwnodeapptemplate.object.InventoryHolder;
import com.ardic.android.iot.hwnodeapptemplate.object.ThingHolder;
import com.ardic.android.iotignite.things.Thing;
import com.ardic.android.iotignite.things.ThingData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yavuz.erzurumlu on 04.11.2016.
 */

public class CommunicationUtils {


    private static final String TAG = CommunicationUtils.class.getSimpleName();

    public static InventoryHolder parseNodeInventoryMessage(String nodeInventoryMessage) {


        nodeInventoryMessage = nodeInventoryMessage.replace("\n", "");
        InventoryHolder holder = null;

        try {
            JSONObject root = new JSONObject(nodeInventoryMessage);

            if (root.has(JsonKeys.INVENTORY_MESSAGE)) {

                holder = new InventoryHolder();
                JSONObject nodeInventory = root.getJSONObject(JsonKeys.INVENTORY_MESSAGE);


                JSONArray thingsArray = nodeInventory.getJSONArray(JsonKeys.THINGS);

                holder.setNodeId(nodeInventory.getString(JsonKeys.NODE_ID));
                holder.setNodeType(nodeInventory.getString(JsonKeys.NODE_TYPE));
                holder.setUniqueId(nodeInventory.getString(JsonKeys.UNIQUE_ID));


                ThingHolder holderThing;
                for (int i = 0; i < thingsArray.length(); i++) {
                    JSONObject thing = thingsArray.getJSONObject(i);
                    holderThing = new ThingHolder();
                    holderThing.setThingId(thing.getString(JsonKeys.THING_ID));
                    holderThing.setThingType(thing.getString(JsonKeys.THING_TYPE));
                    holderThing.setActuator(thing.getBoolean(JsonKeys.ACTUATOR));
                    holderThing.setConnectedPin(thing.getString(JsonKeys.CONNECTED_PIN));
                    holderThing.setDataType(thing.getString(JsonKeys.DATA_TYPE));
                    holderThing.setVendor(thing.getString(JsonKeys.VENDOR));
                    holder.getThings().add(holderThing);
                }


            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException on parseNodeInventoryMessage :" + e);
            holder = null;
        }

        return holder;
    }

    public static boolean parseHeartbeatMessage(String heartbeat) {
        //{"messageType":"heartbeat","heartbeat":"_--^--_--^--_--^--_--^--_"}
        try {
            JSONObject heartbeatJson = new JSONObject(heartbeat);
            if (heartbeatJson.has(JsonKeys.MSG_TYPE) && JsonKeys.HEARTBEAT_MESSAGE.equals(heartbeatJson.getString(JsonKeys.MSG_TYPE))) {
                return true;
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException on parseHeartbeatMessage :" + e);
        }

        return false;

    }

    public static boolean parseDataMessage(String data) {
        //{"messageType":"heartbeat","heartbeat":"_--^--_--^--_--^--_--^--_"}
        try {
            JSONObject dataJson = new JSONObject(data);

            if (dataJson.has(JsonKeys.MSG_TYPE) && JsonKeys.DATA_MESSAGE.equals(dataJson.getString(JsonKeys.MSG_TYPE))) {
                return true;
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException on parseDataMessage :" + e);
        }

        return false;

    }

    public static boolean parseDataMessageAndSendToAgent(List<Thing> thingList, String data) {

        try {
            JSONObject dataJson = new JSONObject(data);


            JSONArray dataArray = dataJson.getJSONArray(JsonKeys.DATA_MESSAGE);

            String thingId = dataJson.getString(JsonKeys.THING_ID);


            ArrayList<String> dataList = new ArrayList<>();

            for (int i = 0; i < dataArray.length(); i++) {
                dataList.add(dataArray.getString(i));
            }

            for (Thing t : thingList) {

                if (t.getThingID().equals(thingId)) {
                    ThingData thingData = new ThingData();
                    for (String str : dataList) {
                        thingData.addData(str);
                    }
                    return t.sendData(thingData);
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException on parseDataMessageAndSendToAgent :" + e);
        }

        return false;

    }


}
