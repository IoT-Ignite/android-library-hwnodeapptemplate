package com.ardic.android.iot.hwnodeapptemplate.object;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.net.Socket;

public class WifiNode {

    private String ipAddress;
    private String uniqueID;
    private String nodeType;
    private Socket mNodeSocket;
    private InventoryHolder holder;

    public InventoryHolder getHolder() {
        return holder;
    }

    public void setHolder(InventoryHolder holder) {
        this.holder = holder;
    }

    public Socket getNodeSocket() {
        return mNodeSocket;
    }

    public void setNodeSocket(Socket mNodeSocket) {
        this.mNodeSocket = mNodeSocket;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WifiNode)) {
            return false;
        }
        WifiNode other = (WifiNode) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(uniqueID, other.uniqueID);
        builder.append(nodeType, other.nodeType);
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(uniqueID);
        builder.append(nodeType);
        return builder.hashCode();
    }
}
