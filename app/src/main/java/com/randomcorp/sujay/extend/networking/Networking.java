package com.randomcorp.sujay.extend.networking;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sujay on 7/9/15.
 */
public class Networking
{

    public interface MessageReceivedCallback
    {
        void msgReceived(DatagramPacket pkt);
    }
    private static final String TAG = "NETWORKING";
    private DatagramSocket senderSocket,receiverSocket;
    private Thread receiverThread;
    private final int port = 9999;
    private MessageReceivedCallback callback;
    private ReceiverRunnable receiverRunnable;

    public Networking(MessageReceivedCallback callback)
    {
        try
        {
            receiverSocket = new DatagramSocket(null);
            receiverSocket.setReuseAddress(true);
            receiverSocket.bind(new InetSocketAddress(port));
            senderSocket = new DatagramSocket();
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        this.callback = callback;
    }
    public void send(DatagramPacket pkt)
    {
        try
        {
            senderSocket.send(pkt);
        }
        catch (IOException e)
        {
            Log.d(TAG,"Can't Send");
            e.printStackTrace();
        }
    }

    public void startReceiving()
    {
        receiverRunnable = new ReceiverRunnable(receiverSocket);
        receiverThread = new Thread(receiverRunnable);
        receiverThread.start();
    }

    public void stopReceiving()
    {
        Log.d(TAG, "STOP CALLED");
        receiverRunnable.setIsRunning(false);
        receiverRunnable = null;
        receiverThread = null;
    }


    private void messageReceived(DatagramPacket msg)
    {
        callback.msgReceived(msg);
    }


    class ReceiverRunnable implements Runnable
    {
        DatagramSocket receiverSocket;
        boolean isRunning;
        public void setIsRunning(boolean isRunning)
        {
            this.isRunning = isRunning;
        }
        public ReceiverRunnable(DatagramSocket receiverSocket)
        {
            this.receiverSocket = receiverSocket;
            isRunning = true;
        }
        @Override
        public void run()
        {
            Log.d(TAG,"new thread started");
            ExecutorService exe = Executors.newFixedThreadPool(1);
            while(isRunning)
            {
                byte[] buf = new byte[1024];
                DatagramPacket message = new DatagramPacket(buf, 1024);
                try
                {
                    receiverSocket.receive(message);
                    if(message.getAddress().isLoopbackAddress())
                        break;
                    final DatagramPacket temp = message;
                    exe.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            messageReceived(temp);
                        }
                    });

                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            exe.shutdown();
            exe = null;
            Log.d(TAG,"thread exiting");
        }
    }

}


