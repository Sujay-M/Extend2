package com.randomcorp.sujay.extend.networking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by sujay on 12/9/15.
 */
public class ClientExtendProtocol implements ExtendProtocol, Networking.MessageReceivedCallback {

    public interface CommandFromServer
    {
        void commandReceived(String type, String data);
    }
    private static final String TAG = "CLIENT";
    final static int port = 9999;
    private CommandFromServer callback;
    private Networking client;
    private boolean isConnected;
    private Handler handler;
    private String username;
    private String macAddress = "XYZ";
    private String devicename;
    private boolean isImageServerRunning;
    private InetAddress serverAddress;
    private int msgNo,devNo;

    public ClientExtendProtocol(CommandFromServer commandFromServer,String username,String deviceName)
    {
        this.callback = commandFromServer;
        this.username = username;
        this.devicename = deviceName;
        isConnected = false;
        client = new Networking(this);
        setDevNo(-1);
        msgNo = 0;
        handler = new Handler(new Handler.Callback()
        {
            @Override
            public boolean handleMessage(Message msg)
            {
                Bundle b = msg.getData();
                String type = b.getString("TYPE");
                String data = b.getString("DATA");
                callback.commandReceived(type,data);
                return false;
            }
        });
    }

    public void startClientProtocol()
    {
        client.startReceiving();
    }

    public void stopClientProtocol()
    {
        client.stopReceiving();
    }

    public synchronized int getDevNo() {
        return devNo;
    }

    public synchronized void setDevNo(int devNo) {
        this.devNo = devNo;
    }

    private void connectToServer()
    {
        byte[] buf = (clientStartHeader+delimiter+deviceDetailsHeader+delimiter+macAddress+delimiter+ username).getBytes();
        final DatagramPacket ack = new DatagramPacket(buf,buf.length,serverAddress,port);
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while(getDevNo()==-1)
                {
                    client.send(ack);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "devNo = " + getDevNo());
            }
        });
        t.start();
    }

    private DatagramPacket buildAck()
    {
        String s = clientStartHeader+delimiter+ackHeader+delimiter+msgNo;
        byte buf[];
        buf = s.getBytes();
        return new DatagramPacket(buf, buf.length, serverAddress, port);
    }

    private void sendAck()
    {
        final DatagramPacket ackPkt = buildAck();
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.send(ackPkt);
            }
        }).start();
    }

    @Override
    public void msgReceived(DatagramPacket pkt)
    {
        final InetAddress serverAddr = pkt.getAddress();
        String message = new String(pkt.getData(), 0, pkt.getLength());
        Log.d(TAG,"message = "+message);
        String msgParts[] = message.split(delimiter);
        try
        {
            if(msgParts[0].equals(serverStartHeader))
            {
                if (msgParts[1].equals(discoveryHeader) && !isConnected)
                {
                    new Thread(new Runnable() {
                        @Override
                        public void run()
                        {
                            Log.d(TAG,"sending addr = "+serverAddr.getHostAddress()+" port = "+port);
                            byte[] buf = (clientStartHeader + delimiter + deviceInfoHeader + delimiter + username + delimiter + devicename).getBytes();
                            DatagramPacket indentifyPkt = new DatagramPacket(buf, buf.length, serverAddr, port);
                            client.send(indentifyPkt);
                        }
                    }).start();
                    if(!isImageServerRunning)
                        startImageServer();
                }
                else if(msgParts[1].equals(connectionRequestHeader) && !isConnected)
                {
                        serverAddress = serverAddr;
                        connectToServer();
                }
                else if(getDevNo()==-1 && msgParts[2].equals(deviceNumberHeader))
                {
                    int messageNo = Integer.parseInt(msgParts[1]);
                    int dno = Integer.parseInt(msgParts[3]);
                    isConnected = true;
                    setDevNo(dno);
                    msgNo = messageNo;
                    sendAck();
                }
                else if(serverAddr.equals(serverAddress) && msgParts.length>2)
                {
                    int messageNo = Integer.parseInt(msgParts[1]);
                    if(msgNo==messageNo || (msgNo+1)==messageNo)
                    {
                        msgNo = messageNo;
                        sendAck();
                        Message msg = handler.obtainMessage();
                        Bundle b = msg.getData();
                        b.putString("TYPE",msgParts[2]);
                        b.putString("DATA", message.substring(msgParts[0].length() + msgParts[1].length() + msgParts[2].length()+3));
                        handler.sendMessage(msg);
                    }

                }
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            return;
        }
    }

    private void startImageServer()
    {

    }

    public void sendSyncPacket()
    {
        byte buf[];
        buf = (clientStartHeader+delimiter+syncHeader+delimiter+SystemClock.elapsedRealtime()).getBytes();
        final DatagramPacket syncPkt = new DatagramPacket(buf, buf.length, serverAddress, port);
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.send(syncPkt);
            }
        }).start();
    }

}

