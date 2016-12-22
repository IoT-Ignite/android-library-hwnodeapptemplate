
package com.ardic.android.iot.hwnodeapptemplate.listener;

public interface SocketConnectionListener {

    public void onDataReceived(String message);

    public void onConnectionLost();
}
