package com.randomcorp.sujay.extend.networking;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;


import com.randomcorp.sujay.extend.models.ClientModel;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sujay on 12/9/15.
 */
public class ServerExtendProtocol implements ExtendProtocol, Networking.MessageReceivedCallback {


    public interface ServerProtocolMessage {
        void gotMessage(String msg, InetAddress clientAddress);
    }

    private final int MAXTRY = 5;
    private final String TAG = "ServerProtocol";
    Handler handler;
    private HashMap<InetAddress, Integer> addressIntegerHashMap;
    private static ServerExtendProtocol serverProtocol;
    private Networking server;
    private boolean acceptClients;
    private ExecutorService exe;
    private int devNo;
    private ServerProtocolMessage callback;
    private ArrayList<ClientModel> clients;

    private ServerExtendProtocol() {

        server = new Networking(this);
        addressIntegerHashMap = new HashMap<>();
        acceptClients = true;
        clients = new ArrayList<>();
        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg)
            {
                if (msg.what == 1 && callback!=null)
                {
                    Bundle b = msg.getData();
                    String info = b.getString("INFO");
                    String addr = b.getString("ADDRESS");
                    try {
                        InetAddress address = InetAddress.getByName(addr);
                        callback.gotMessage("INFO "+info,address);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                }
                return false;
            }
        });
    }

    public static ServerExtendProtocol getSingleton() {
        if (serverProtocol == null)
            serverProtocol = new ServerExtendProtocol();
        return serverProtocol;
    }

    public void startProtocol()
    {
        exe = Executors.newFixedThreadPool(5);
        server.startReceiving();
    }

    public void stopProtocol()
    {
        server.stopReceiving();
        exe.shutdown();
        exe = null;
    }

    public void registerCallback(ServerProtocolMessage callback)
    {
        this.callback = callback;
    }

    public void unRegisterCallback()
    {
        this.callback = null;
    }

    public void discover(String ip)
    {
        devNo = 0;
        addressIntegerHashMap.clear();
        acceptClients = true;
        final String classDSubnet = ip.substring(0, ip.lastIndexOf('.'));
        exe.execute(new Runnable() {
            @Override
            public void run() {
                sendDiscoveryRequest(classDSubnet, 1, 50);
            }
        });
        exe.execute(new Runnable() {
            @Override
            public void run() {
                sendDiscoveryRequest(classDSubnet, 50, 100);
            }
        });
        exe.execute(new Runnable() {
            @Override
            public void run() {
                sendDiscoveryRequest(classDSubnet, 100, 150);
            }
        });
        exe.execute(new Runnable() {
            @Override
            public void run() {
                sendDiscoveryRequest(classDSubnet, 150, 200);
            }
        });
        exe.execute(new Runnable() {
            @Override
            public void run() {
                sendDiscoveryRequest(classDSubnet, 200, 255);
            }
        });
    }

    private void sendDiscoveryRequest(String subnet, int start, int end)
    {
        final byte[] buf = (serverStartHeader + delimiter + discoveryHeader).getBytes();
        for (int i = start; i < end; i++)
        {
            try
            {
                InetAddress temp = InetAddress.getByName(subnet + "." + i);
                DatagramPacket pkt = new DatagramPacket(buf, buf.length, temp, 9999);
                server.send(pkt);
            } catch (UnknownHostException e)
            {
                e.printStackTrace();
            }

        }
    }

    public void sendConnectionRequest(ArrayList<InetAddress> addresses)
    {
        acceptClients = false;
        devNo = 0;
        exe.shutdownNow();
        exe = null;
        int noOfThreads = (addresses.size()<5)?addresses.size():5;
        exe = Executors.newFixedThreadPool(noOfThreads);
        for (int i = 0;i<addresses.size();i++)
        {
            exe.execute(new ConnectRequestRunnable(addresses.get(i)));
        }

    }

    class ConnectRequestRunnable implements Runnable
    {

        InetAddress clientAddress;
        byte buf[];
        DatagramPacket pkt;
        ConnectRequestRunnable(InetAddress clientAddress)
        {
            this.clientAddress = clientAddress;
            buf = (serverStartHeader+delimiter+connectionRequestHeader).getBytes();
            pkt = new DatagramPacket(buf,buf.length,clientAddress,9999);
        }
        @Override
        public void run()
        {
            while(!addressIntegerHashMap.containsKey(clientAddress))
            {
                server.send(pkt);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private DatagramPacket buildPacket(ClientModel temp,String message)
    {
        /**construct the packet*/
        DatagramPacket pkt;
        byte buf[];
        buf = (serverStartHeader+delimiter+temp.getMsgNumber()+delimiter+message).getBytes();
        pkt = new DatagramPacket(buf,buf.length,temp.ipAddress,9999);
        return pkt;
    }

    private void sendMessage(final int devNo, final String msg, final boolean time)
    {
        try
        {
            final ClientModel temp = clients.get(devNo);
            exe.execute(new Runnable() {
                @Override
                public void run()
                {
                    DatagramPacket sendPkt;
                    int currentNo = temp.getMsgNumber();
                    if(time)
                        sendPkt = buildPacket(temp,msg+delimiter+SystemClock.elapsedRealtime());
                    else
                        sendPkt = buildPacket(temp,msg);
                    while (temp.getMsgNumber() == currentNo && temp.getTryNo() < MAXTRY)
                    {
                        Log.d(TAG, "msg no = " + temp.getMsgNumber() + " to client " + temp.devNo);
                        int tryNo = temp.getTryNo();
                        temp.setTryNo((tryNo + 1));
                        server.send(sendPkt);
                        try
                        {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    temp.setTryNo(0);
                }
            });

        }
        catch (IndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }
    }

    public void sendCommandToAll(String msg,boolean time)
    {
        for (int i = 0;i<clients.size();i++)
        {
            sendCommandMessage(i, msg, time);
        }
    }

    public void sendDataToAll(String msg)
    {
        for (int i = 0;i<clients.size();i++)
        {
            sendDataMessage(i, msg);
        }
    }

    public void sendCommandMessage(int devNo,String msg,boolean time)
    {
        String command = commandHeader+delimiter+msg;
        sendMessage(devNo,command,time);
    }

    public void sendDataMessage(int devNo,String msg)
    {
        String data = dataHeader+delimiter+msg;
        sendMessage(devNo,data,false);
    }



    @Override
    public void msgReceived(DatagramPacket pkt)
    {
        Log.d(TAG,"msg received");
        InetAddress clientAddr = pkt.getAddress();
        int port = pkt.getPort();
        String message = new String(pkt.getData(), 0, pkt.getLength());
        Log.d(TAG,"msg = "+message);
        String msgParts[] = message.split(delimiter);
        Log.d(TAG,"msgPart[0].equals "+msgParts[0].equals(clientStartHeader));
        Log.d(TAG,"acceptclients = "+acceptClients+" hashmap = "+!addressIntegerHashMap.containsKey(clientAddr)+" length = "+(msgParts.length == 2));
        try
        {
            if(msgParts[0].equals(clientStartHeader))
            {
                if (acceptClients && !addressIntegerHashMap.containsKey(clientAddr) && msgParts[1].equals(deviceInfoHeader))
                {
                    Log.d(TAG, "going to call");
                    Message msg = handler.obtainMessage();
                    Bundle b = msg.getData();
                    b.putString("INFO",message.substring(msgParts[0].length()+msgParts[1].length()+2));
                    b.putString("ADDRESS",clientAddr.getHostAddress());
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
                else if(!addressIntegerHashMap.containsKey(clientAddr) && msgParts[1].equals(deviceDetailsHeader))
                {
                    addressIntegerHashMap.put(clientAddr, devNo);
                    ClientModel temp = new ClientModel(clientAddr,port,devNo);
                    clients.add(devNo,temp);
                    String msg = deviceNumberHeader+delimiter+devNo;
                    sendMessage(devNo,msg,false);
                    devNo++;
                }
                else if(addressIntegerHashMap.containsKey(clientAddr) && msgParts[1].equals(ackHeader))
                {
                    ClientModel temp = clients.get(addressIntegerHashMap.get(clientAddr));
                    int messageNo = Integer.parseInt(msgParts[2]);
                    if(temp.getMsgNumber()==messageNo)
                        temp.setMsgNo(messageNo+1);
                }

            }

        }
        catch (IndexOutOfBoundsException e)
        {
            return;
        }

    }

    public int noOfClients()
    {
        try
        {
            return clients.size();
        }
        catch (NullPointerException e)
        {
            return 0;
        }
    }
}
