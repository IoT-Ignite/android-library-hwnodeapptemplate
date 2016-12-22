package com.ardic.android.iot.hwnodeapptemplate.manager;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TcpServerSocket {

    private static final String TAG = TcpServerSocket.class.getSimpleName();
    private ServerSocket serverSocket;
    private int portNo;

    public TcpServerSocket(final int serverPort) {

        this.portNo = serverPort;
        try {
            serverSocket = new ServerSocket(portNo);
            serverSocket.setReuseAddress(true);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isClosed() {
        if (serverSocket != null) {
            return serverSocket.isClosed();
        }

        return false;
    }

    public Socket acceptClient() {
        Socket socket = null;
        try {
            socket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return socket;
    }

    public boolean sendTo(Socket socket, final String sendMessage) {
        if (socket != null) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                if (outputStream != null) {

                    Log.i(TAG, "Sending msg : " + sendMessage);
                    PrintStream printStream = new PrintStream(outputStream);
                    if (printStream != null && !printStream.checkError()) {
                        printStream.println(sendMessage);
                        printStream.flush();

                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public String receiveFromAsText(Socket socket) {

        String message = null;

        if (socket != null) {
            //read the message received from client
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                if (in != null) {
                    message = in.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return message;
    }

    public int getSoTimeout() {
        try {
            if (serverSocket != null) {
                return serverSocket.getSoTimeout();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void setSoTimeout(final int timeout) {
        try {
            if (serverSocket != null) {
                serverSocket.setSoTimeout(timeout);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
