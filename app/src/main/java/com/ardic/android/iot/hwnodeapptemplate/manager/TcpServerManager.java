package com.ardic.android.iot.hwnodeapptemplate.manager;

import android.util.Log;


import com.ardic.android.iot.hwnodeapptemplate.constant.IgniteNodeMCUConstants;
import com.ardic.android.iot.hwnodeapptemplate.listener.WifiNodeListener;
import com.ardic.android.iot.hwnodeapptemplate.object.InventoryHolder;
import com.ardic.android.iot.hwnodeapptemplate.object.WifiNode;
import com.ardic.android.iot.hwnodeapptemplate.utils.CommunicationUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yavuz.erzurumlu on 04.11.2016.
 */

public class TcpServerManager {


    private static final String TAG = TcpServerManager.class.getSimpleName();

    private static List<WifiNode> wifiNodeList = new CopyOnWriteArrayList<WifiNode>();
    private static List<WifiNodeListener> wifiNodeListenerList = new CopyOnWriteArrayList<>();
    private InetAddress mInetAddress;
    private TcpServerSocket mTcpServerSocket;
    private boolean isRunning = false;
    private boolean isTerminated;

    public TcpServerManager() {
    }

    public static void addListener(WifiNodeListener listener) {
        if (!wifiNodeListenerList.contains(listener)) {
            wifiNodeListenerList.add(listener);
        }
    }

    public static void removeListener(WifiNodeListener listener) {
        wifiNodeListenerList.remove(listener);
    }

    private static void sendNewDeviceToListeners(WifiNode device) {
        for (WifiNodeListener listener : wifiNodeListenerList) {
            listener.onWifiNodeAdded(device);
        }
    }

    private static void sendChangedDeviceToListeners(WifiNode device) {
        for (WifiNodeListener listener : wifiNodeListenerList) {
            listener.onWifiNodeAddressChange(device);
        }
    }

    public static List<WifiNode> getWifiNodeList() {
        return wifiNodeList;
    }

    public synchronized void start() {
        if (!isRunning) {
            isRunning = true;
            mTcpServerSocket = new TcpServerSocket(IgniteNodeMCUConstants.PORT);

            isTerminated = false;
            if (mTcpServerSocket != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!isTerminated()) {
                            Log.i(TAG, "before accepted.");
                            Socket socket = mTcpServerSocket.acceptClient();
                            Log.i(TAG, "Socket accepted.");
                            readAndParseSocket(socket);
                            Log.i(TAG, "after accepted.");
                        }
                    }
                }).start();
            } else {
                Log.i(TAG, "mTcpServerSocket is NULL");
            }
        }
    }

    public synchronized void stop() {
        if (isRunning) {
            isRunning = false;
            isTerminated = true;
            if (mTcpServerSocket != null) {
                mTcpServerSocket.close();
            }
        }
    }

    private void readAndParseSocket(final Socket socket) {
        if (socket != null) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Socket Received !!!!! \n" + socket);
                    while (true) {
                        Log.d(TAG, "Waiting for connections...");
                        String message = mTcpServerSocket.receiveFromAsText(socket);
                        Log.i(TAG,"<<<<<<< MSG >>>>>>>>>>>>>>  "  + message );
                        if (message != null) {

                            Log.i(TAG, "Inventory Message Received !!!!! \n" + message);

                            try {
                                JSONObject temporary = new JSONObject(message);
                            } catch (JSONException e) {
                                Log.i(TAG, "Json is not valid");
                                try {
                                    socket.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                break;
                            }

                            InventoryHolder holder = CommunicationUtils.parseNodeInventoryMessage(message);
                            if (holder != null) {
                                mTcpServerSocket.sendTo(socket, IgniteNodeMCUConstants.INVENTORY_OK_MSG);
                                WifiNode device = new WifiNode();

                                device.setNodeType(holder.getNodeType());
                                device.setUniqueID(holder.getUniqueId());
                                device.setIpAddress(socket.getInetAddress().getHostAddress());
                                device.setNodeSocket(socket);
                                device.setHolder(holder);

                                boolean wasFound = false;
                                for (int i = 0; i < wifiNodeList.size(); ++i) {
                                    if (wifiNodeList.get(i).equals(device)) {
                                        wifiNodeList.set(i, device);
                                        wasFound = true;
                                        break;
                                    }
                                }
                                if (!wasFound) {
                                    wifiNodeList.add(device);
                                    sendNewDeviceToListeners(device);
                                } else {
                                    sendChangedDeviceToListeners(device);
                                }
                                break;
                            } else {
                                Log.d(TAG, "holder is null closing soscket");
                                mTcpServerSocket.sendTo(socket, IgniteNodeMCUConstants.INVENTORY_NOK_MSG);
                            }
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }).start();
        } else {
            Log.i(TAG, "Socket is NULL !!!!! \n" + socket);
        }
    }

    public boolean isTerminated() {
        return isTerminated;
    }

    public void setTerminated(boolean terminated) {
        isTerminated = terminated;
    }
}
