package com.ardic.android.iot.hwnodeapptemplate.handler;

import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.iot.hwnodeapptemplate.listener.SocketConnectionListener;
import com.ardic.android.utilitylib.interfaces.TimeoutListener;
import com.ardic.android.utilitylib.timer.TimeoutTimer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by yavuz.erzurumlu on 05.11.2016.
 */

public class ClientSocketHandler extends Thread {

    private static final long PACKET_TIMEOUT = 14000L;
    private static final String TAG = ClientSocketHandler.class.getSimpleName();
    private Socket socket;
    private SocketConnectionListener mSocketConnectionListener;
    private TimeoutTimer msgTimer = new TimeoutTimer(new TimeoutListener() {
        @Override
        public void onTimerTimeout() {
            sendConnectionLost();
        }
    });
    private boolean isTerminated = false;

    public ClientSocketHandler(Socket socket, SocketConnectionListener listener) {
        this.mSocketConnectionListener = listener;
        this.socket = socket;
    }

    public SocketConnectionListener getSocketConnectionListener() {
        return mSocketConnectionListener;
    }

    public void setSocketConnectionListener(SocketConnectionListener mSocketConnectionListener) {
        this.mSocketConnectionListener = mSocketConnectionListener;
    }

    @Override
    public void run() {
        super.run();

        msgTimer.startTimer(PACKET_TIMEOUT);
        while (!isTerminated) {
            parseMessage(receiveData());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted Exception on ClientThread:" + e);
            }
        }
        Log.e(TAG, "Client socket handler closing...");
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException on run() :" + e);
        }
    }

    public void parseMessage(String msg) {
        if (!TextUtils.isEmpty(msg) && mSocketConnectionListener != null) {
            mSocketConnectionListener.onDataReceived(msg);
            msgTimer.startTimer(PACKET_TIMEOUT);
        }

    }


    public boolean isTerminated() {
        return isTerminated;
    }

    public void setTerminated(boolean terminated) {
        isTerminated = terminated;
    }

    public String receiveData() {

        String message = null;

        if (socket != null) {
            //read the message received from client
            BufferedReader br = null;
            InputStreamReader is = null;
            try {
                is = new InputStreamReader(socket.getInputStream());
                br = new BufferedReader(is);

                if (br != null) {
                    message = br.readLine();
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException on receiveData() :" + e);
                sendConnectionLost();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "IOException on receiveData() :" + e2);
                    }
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "IOException on receiveData() :" + e2);
                    }
                }
            }
        }

        return message;
    }

    public boolean sendTo(final String sendMessage) {
        if (socket != null) {
            OutputStream outputStream = null;
            PrintStream printStream = null;
            try {
                outputStream = socket.getOutputStream();
                if (outputStream != null) {
                    printStream = new PrintStream(outputStream);
                    if (printStream != null && !printStream.checkError()) {
                        printStream.println(sendMessage);
                        printStream.flush();
                        Log.e(TAG, "print stream flushed ");
                        return true;
                    } else {
                        Log.e(TAG, "print stream is null");
                    }
                } else {
                    Log.e(TAG, "output stream is NULL");
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException on sendTo() :" + e);
                if (printStream != null) {
                    printStream.close();
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "IOException on sendTo() :" + e2);
                    }
                }
            }
        } else {
            Log.e(TAG, "socket NULL");
        }

        return false;
    }

    private void sendConnectionLost() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException on sendConnectionLost() :" + e);
            }
            socket = null;
        }
        if (mSocketConnectionListener != null) {
            mSocketConnectionListener.onConnectionLost();
        }
    }
}
