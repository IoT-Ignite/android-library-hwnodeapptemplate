package com.ardic.android.iot.hwnodeapptemplate.base;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.iot.hwnodeapptemplate.constant.IgniteNodeMCUConstants;
import com.ardic.android.iot.hwnodeapptemplate.constant.JsonKeys;
import com.ardic.android.iot.hwnodeapptemplate.handler.ClientSocketHandler;
import com.ardic.android.iot.hwnodeapptemplate.listener.SocketConnectionListener;
import com.ardic.android.iot.hwnodeapptemplate.listener.ThingEventListener;
import com.ardic.android.iot.hwnodeapptemplate.object.InventoryHolder;
import com.ardic.android.iot.hwnodeapptemplate.object.ThingHolder;
import com.ardic.android.iot.hwnodeapptemplate.object.WifiNode;
import com.ardic.android.iot.hwnodeapptemplate.utils.CommunicationUtils;
import com.ardic.android.iotignite.enumerations.NodeType;
import com.ardic.android.iotignite.enumerations.ThingCategory;
import com.ardic.android.iotignite.enumerations.ThingDataType;
import com.ardic.android.iotignite.listeners.NodeListener;
import com.ardic.android.iotignite.listeners.ThingListener;
import com.ardic.android.iotignite.nodes.IotIgniteManager;
import com.ardic.android.iotignite.nodes.Node;
import com.ardic.android.iotignite.things.Thing;
import com.ardic.android.iotignite.things.ThingActionData;
import com.ardic.android.iotignite.things.ThingConfiguration;
import com.ardic.android.iotignite.things.ThingData;
import com.ardic.android.iotignite.things.ThingType;
import com.ardic.android.utilitylib.interfaces.TimeoutListener;
import com.ardic.android.utilitylib.timer.TimeoutTimer;
import com.google.gson.Gson;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yavuz.erzurumlu on 04.11.2016.
 */

public abstract class BaseWifiNodeDevice implements TimeoutListener {

    private static final long IGNITE_RESTART_INTERVAL = 10000L;
    private Node mNode;
    private List<ThingEventListener> thingEventListeners = new CopyOnWriteArrayList<>();
    private List<Thing> thingList = new ArrayList<>();
    private WifiNode wifiNodeDevice;
    private IotIgniteManager mIotIgniteManager;
    private Context appContext;
    private Object sync = new Object();
    private String logTag;
    private ClientSocketHandler mClientSocketHandler;
    private TimeoutTimer registrationTimer = new TimeoutTimer(this);

    private SocketConnectionListener mListener = new SocketConnectionListener() {
        @Override
        public void onDataReceived(String message) {

            InventoryHolder holder = CommunicationUtils.parseNodeInventoryMessage(message);
            if (mNode != null) {
                if (!mNode.isConnected()) {
                    setNodeAsConnected();
                    setThingsAsConnected();
                    sendConnectionStateChanged(mNode.getNodeID(), true);
                }
                if (holder != null) {
                    mClientSocketHandler.sendTo(IgniteNodeMCUConstants.INVENTORY_OK_MSG);
                } else if (CommunicationUtils.parseHeartbeatMessage(message)) {
                    Log.i(logTag, "Heartbeat Received from " + wifiNodeDevice.getNodeType() + "  Message :  " + message);

                } else if (CommunicationUtils.parseDataMessage(message)) {
                    Log.i(logTag, "Data msg received  " + wifiNodeDevice.getNodeType() + "  Message :  " + message);
                    CommunicationUtils.parseDataMessageAndSendToAgent(thingList, message);
                    sendDataReceived(message);

                } else {
                    Log.i(logTag, "Unknown  msg received  " + wifiNodeDevice.getNodeType() + "  Message :  " + message);
                    sendUnknownMessageReceived(wifiNodeDevice.getHolder().getNodeId(), message);
                }
            }

        }

        @Override
        public void onConnectionLost() {
            Log.i(logTag, "SOCKET CONNECTION LOST ");
            setNodeAndThingsDisconnected();
            sendConnectionStateChanged(wifiNodeDevice.getHolder().getNodeId(), false);
            stop();
        }
    };

    private NodeListener nodeListener = new NodeListener() {
        @Override
        public void onNodeUnregistered(String s) {
            sendResetMessage();
        }
    };

    private ThingListener thingListener = new ThingListener() {
        @Override
        public void onConfigurationReceived(Thing thing) {
            if (thing != null) {
                JSONObject object = new JSONObject();
                try {
                    object.put(JsonKeys.MSG_TYPE, JsonKeys.CONFIG_MESSAGE);
                    object.put(JsonKeys.THING_ID, thing.getThingID());
                    object.put(JsonKeys.CONFIG_MESSAGE, new Gson().toJson(thing.getThingConfiguration()));
                    Log.d(logTag, "configuration sending to node " + object.toString());
                    mClientSocketHandler.sendTo(object.toString());
                    sendConfigReceived(thing.getNodeID(), thing.getThingID(), thing.getThingConfiguration());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(logTag, "Configuration received" + thing.getThingID());
            }
        }

        @Override
        public void onActionReceived(String s, String s1, ThingActionData thingActionData) {
            if (s != null && s1 != null && thingActionData != null) {
                if (sendActionMessage(s1, thingActionData.getMessage())) {
                    sendActionReceived(s, s1, thingActionData.getMessage());
                } else {
                    Log.d(logTag, "Action received" + thingActionData.getMessage());
                }
            }
        }

        @Override
        public void onThingUnregistered(String s, String s1) {

            sendThingUnregistered(s, s1);
        }
    };


    public BaseWifiNodeDevice(IotIgniteManager mIotIgniteManager, Context appContext, final String logTag, WifiNode device) {
        this.mIotIgniteManager = mIotIgniteManager;
        this.appContext = appContext;
        this.logTag = logTag;
        this.wifiNodeDevice = device;
    }

    public synchronized void start() {
        if (mClientSocketHandler == null || !mClientSocketHandler.isAlive()) {
            mClientSocketHandler = new ClientSocketHandler(wifiNodeDevice.getNodeSocket(), mListener);
            mClientSocketHandler.start();
        }
        registrationTimer.startTimer(IGNITE_RESTART_INTERVAL);
        initNodeAndThings();
    }

    public synchronized void stop() {
        if (mClientSocketHandler != null) {
            mClientSocketHandler.setTerminated(true);
            mClientSocketHandler = null;
        }
        registrationTimer.cancelTimer();
        setNodeAndThingsDisconnected();
    }

    protected boolean registerNodeIfNotRegistered(Node node) {
        if (node != null) {
            if (!node.isRegistered()) {
                boolean result = node.register();
                Log.d(logTag, node.getNodeID() + " Node registration result: " + result);
                return result;
            }

            return true;
        } else {
            Log.d(logTag, "Node is null");
        }

        return false;
    }


    protected void setNodeConnectedState(boolean connected, String description) {
        if (mNode != null) {
            mNode.setConnected(connected, description);
        }
    }

    protected void setThingConnectedState(Thing thing, boolean connected, String description) {
        if (thing != null) {
            thing.setConnected(connected, description);
        }
    }


    private boolean registerNodes() {

        if (mNode == null) {
            mNode = IotIgniteManager.NodeFactory.createNode(wifiNodeDevice.getHolder().getNodeId(),
                    wifiNodeDevice.getHolder().getNodeId(), NodeType.GENERIC, null, nodeListener);
        }
        if (mNode != null && !mNode.isRegistered()) {
            boolean result = mNode.register();
            if (!result) {
                Log.d(logTag, "node: " + mNode.getNodeID() + " is not registered");
                return false;
            }
        }

        return true;
    }


    private boolean registerThings() {
        if (mNode != null) {
            for (ThingHolder tHolder : wifiNodeDevice.getHolder().getThings()) {
                Thing temp = mNode.createThing(tHolder.thingId,
                        new ThingType(tHolder.thingType, tHolder.vendor, ThingDataType.getDataTypeByString(tHolder.dataType)),
                        ThingCategory.EXTERNAL,
                        tHolder.actuator,
                        thingListener, null);

                if (temp != null) {
                    if (!thingList.contains(temp)) {
                        thingList.add(temp);
                    }
                } else {
                    Log.d(logTag, "thing: " + tHolder.thingId + " is null");
                    return false;
                }
            }
        } else {
            Log.d(logTag, "node: " + wifiNodeDevice.getHolder().getNodeId() + " is null");
            return false;
        }


        for (Thing t : thingList) {
            if (t != null && !t.isRegistered()) {
                boolean result = t.register();
                if (!result) {
                    Log.d(logTag, "thing: " + t.getThingID() + " is not registered");
                    return false;
                }
            }

        }

        return true;
    }

    @Override
    public void onTimerTimeout() {
        initNodeAndThings();
    }

    private void initNodeAndThings() {
        Log.i(logTag, "Initing node and things...");
        if (registerNodes() && registerThings()) {
            registrationTimer.cancelTimer();
            setNodeAsConnected();
            setThingsAsConnected();

            applyConfigs();
            Log.i(logTag, "Initing node and things, true");
        } else {
            Log.i(logTag, "Initing node and things, false");
            registrationTimer.startTimer(IGNITE_RESTART_INTERVAL);
        }
    }

    private void setNodeAndThingsDisconnected() {
        setNodeConnectedState(false, "Disconnected from: " + wifiNodeDevice.getIpAddress());
        for (Thing t : thingList) {
            setThingConnectedState(t, false, "Disconnected from: " + wifiNodeDevice.getIpAddress());
        }
    }

    private void setNodeAsConnected() {
        setNodeConnectedState(true, "connection successfull");
    }


    private void setThingsAsConnected() {

        for (Thing t : thingList) {
            setThingConnectedState(t, true, "connection successfull");
        }
    }

    private void applyConfigs() {
        for (Thing t : thingList) {
            if (t != null) {
                JSONObject object = new JSONObject();
                try {
                    object.put(JsonKeys.MSG_TYPE, JsonKeys.CONFIG_MESSAGE);
                    object.put(JsonKeys.THING_ID, t.getThingID());
                    object.put(JsonKeys.CONFIG_MESSAGE, new Gson().toJson(t.getThingConfiguration()));
                    mClientSocketHandler.sendTo(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(logTag, "Configuration received" + t.getThingID());

            }
        }
    }


    public WifiNode getWifiNodeDevice() {
        return wifiNodeDevice;
    }

    public void setWifiNodeDevice(WifiNode wifiNodeDevice) {
        this.wifiNodeDevice = wifiNodeDevice;
    }

    public void addThingEventListener(ThingEventListener listener) {

        if (listener != null && !thingEventListeners.contains(listener)) {
            thingEventListeners.add(listener);
        }
    }

    public void removeThingEventListener(ThingEventListener listener) {

        if (listener != null && thingEventListeners.contains(listener)) {
            thingEventListeners.remove(listener);
        }
    }

    private void sendActionReceived(String nodeId, String thingId, String action) {

        for (ThingEventListener listener : thingEventListeners) {
            listener.onActionReceived(nodeId, thingId, action);
        }
    }

    private void sendDataReceived(String data) {
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
                    String nodeId = t.getNodeID();
                    for (ThingEventListener listener : thingEventListeners) {
                        listener.onDataReceived(nodeId, thingId, thingData);
                    }
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void sendConfigReceived(String nodeId, String thingId, ThingConfiguration config) {
        for (ThingEventListener listener : thingEventListeners) {
            listener.onConfigReceived(nodeId, thingId, config);
        }
    }

    private void sendConnectionStateChanged(String nodeId, boolean state) {
        for (ThingEventListener listener : thingEventListeners) {
            listener.onConnectionStateChanged(nodeId, state);
        }
    }

    private void sendUnknownMessageReceived(String nodeId, String message) {
        for (ThingEventListener listener : thingEventListeners) {
            listener.onUnknownMessageReceived(nodeId, message);
        }
    }

    private void sendNodeUnregistered(String nodeId) {
        for (ThingEventListener listener : thingEventListeners) {
            listener.onNodeUnregistered(nodeId);
        }
    }

    private void sendThingUnregistered(String nodeId, String thingId) {
        for (ThingEventListener listener : thingEventListeners) {
            listener.onThingUnregistered(nodeId, thingId);
        }
    }

    public Node getNode() {
        return mNode;
    }

    public List<Thing> getThingList() {
        return thingList;
    }

    public List<ThingEventListener> getThingEventListeners() {
        return thingEventListeners;
    }

    public void setThingEventListeners(List<ThingEventListener> thingEventListeners) {
        this.thingEventListeners = thingEventListeners;
    }

    public boolean sendCustomMessage(String message) {
        if (mClientSocketHandler != null && !mClientSocketHandler.isTerminated()) {
            return mClientSocketHandler.sendTo(message);
        }
        return false;
    }

    public boolean sendActionMessage(String thingId, String action) {
        if (!TextUtils.isEmpty(thingId) && !TextUtils.isEmpty(action)) {
            JSONObject object = new JSONObject();
            try {
                object.put(JsonKeys.MSG_TYPE, JsonKeys.ACTION_MESSAGE);
                object.put(JsonKeys.THING_ID, thingId);
                object.put(JsonKeys.ACTION_MESSAGE, action);
                Log.d(logTag, "Action sending to node " + object.toString() + "\n Socket  :" + wifiNodeDevice.getNodeSocket());
                if (mClientSocketHandler != null && !mClientSocketHandler.isTerminated()) {
                    return mClientSocketHandler.sendTo(object.toString());
                }
            } catch (JSONException e) {
                Log.d(logTag, "JSONException on sendActionMessage():  " + e);
            }
        }

        return false;
    }

    public boolean sendResetMessage() {
        Log.d(logTag, "Reset received");


        JSONObject object = new JSONObject();
        try {

            object.put(JsonKeys.MSG_TYPE, JsonKeys.RESET_MESSAGE);

            if (mClientSocketHandler != null && !mClientSocketHandler.isTerminated() && mClientSocketHandler.sendTo(object.toString())) {
                sendNodeUnregistered(mNode.getNodeID());
                mNode = null;
                stop();
                return true;
            }

        } catch (JSONException e) {
            Log.i(logTag, "JSONException sendResetMessage() : " + e);
        }

        return false;

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BaseWifiNodeDevice)) {
            return false;
        }
        BaseWifiNodeDevice other = (BaseWifiNodeDevice) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(wifiNodeDevice.getHolder().getNodeId(), other.wifiNodeDevice.getHolder().getNodeId());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(wifiNodeDevice.getHolder().getNodeId());
        return builder.hashCode();
    }
}
