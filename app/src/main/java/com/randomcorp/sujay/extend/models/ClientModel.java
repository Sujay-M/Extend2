package com.randomcorp.sujay.extend.models;

import java.net.InetAddress;

/**
 * Created by sujay on 8/8/15.
 */
public class ClientModel
{
    public final InetAddress ipAddress;
    public final int port;
    private int msgNo;
    public final int devNo;
    private int tryNo;

    public ClientModel(InetAddress clientAddress, int port, int devNo)
    {
        this.ipAddress = clientAddress;
        this.port = port;
        this.devNo = devNo;
        this.setMsgNo(1);
        this.setTryNo(0);
    }
    public synchronized void setMsgNo(int msgNo)
    {
        this.msgNo = msgNo;
    }
    public synchronized int getMsgNumber()
    {
        return this.msgNo;
    }
    public synchronized void setTryNo(int tryNo)
    {
        this.tryNo = tryNo;
    }
    public synchronized int getTryNo()
    {
        return this.tryNo;
    }


}