package com.randomcorp.sujay.extend.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.randomcorp.sujay.extend.R;
import com.randomcorp.sujay.extend.Utils.ClientListAdapter;
import com.randomcorp.sujay.extend.networking.ServerExtendProtocol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Created by sujay on 21/9/15.
 */
public class ClientsSelectionActivity extends AppCompatActivity implements ServerExtendProtocol.ServerProtocolMessage, View.OnClickListener
{
    private static final String TAG = "CLIENT LIST ACTIVITY";
    private RecyclerView mRecyclerView;
    private ClientListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String IP;
    private ServerExtendProtocol server;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_selection_activity);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ClientListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        IP = getHostIp();
        server = ServerExtendProtocol.getSingleton();
        server.registerCallback(this);
        server.startProtocol();
        findViewById(R.id.b_done).setOnClickListener(this);
        Log.d(TAG, "IP fetched = " + IP);
        discover();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_client_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_search:
                discover();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void discover()
    {
        if(IP!=null && IP.length()!=0)
        {
            Log.d(TAG,"IP = "+IP);
            mAdapter.clearList();
            server.discover(IP);
        }
        else
        {
            Toast.makeText(this, "Wifi not available. Please check connection", Toast.LENGTH_SHORT).show();
        }
    }

    private String getHostIp()
    {
        String ip = null;
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        if (mWifi.isConnected())
        {
            Log.d(TAG,"getting connection info");
            WifiInfo info =  wifiManager.getConnectionInfo();
            ip = getWifiIpAddress(info);
        }
        else
        {
            Method method = null;
            try
            {
                Log.d(TAG,"getting hotspot details");
                method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
                int actualState = (Integer) method.invoke(wifiManager, (Object[]) null);
                method.setAccessible(true);
                if(actualState==13)
                {
                    String mac = wifiManager.getConnectionInfo().getMacAddress();
                    if(mac!=null)
                    {
                        ip = getIpAddress(getMac(mac));
                    }
                }

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG,"ip = "+ip);
        return ip;
    }

    protected String getWifiIpAddress(WifiInfo info)
    {
        int ipAddress = info.getIpAddress();
        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }
        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to get host address.");
            ipAddressString = null;
        }
        return ipAddressString;
    }

    private String getIpAddress(final byte[] macBytes)
    {
        String ip = null;

        try
        {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements())
            {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                if(Arrays.equals(networkInterface.getHardwareAddress(), macBytes))
                {
                    Enumeration<InetAddress> enumInetAddress = networkInterface
                            .getInetAddresses();
                    while (enumInetAddress.hasMoreElements())
                    {
                        InetAddress inetAddress = enumInetAddress.nextElement();
                        if (inetAddress.isSiteLocalAddress())
                        {
                            return inetAddress.getHostAddress();
                        }

                    }
                }


            }

        }
        catch (SocketException e)
        {
            e.printStackTrace();
            ip = null;
        }

        return ip;
    }
    private  byte[] getMac(String mac)
    {
        final String[] macParts = mac.split(":");
        final byte[] macBytes = new byte[macParts.length];
        for(int i = 0;i<macParts.length;i++)
        {
            macBytes[i] = (byte)Integer.parseInt(macParts[i],16);
        }
        return macBytes;
    }

    @Override
    public void gotMessage(String msg,InetAddress clientAddress)
    {
        final String delimitier = " ";
        String[] msgParts = msg.split(delimitier);
        if(msgParts[0].equals("INFO"))
        {
            String deviceName = msgParts[1];
            String modelName = msgParts[2];
            mAdapter.add(deviceName,modelName,clientAddress);
        }

    }

    @Override
    public void onClick(View v)
    {
        ArrayList<InetAddress> addresses;
        if(v.getId()==R.id.b_done)
        {
            addresses = mAdapter.getSelectedAddresses();
            if(addresses.size()==0)
            {
                Toast.makeText(this,"Select Clients",Toast.LENGTH_SHORT).show();
            }
            else
            {
                server.sendConnectionRequest(addresses);
                Intent i = new Intent(this,LayoutSelectionActivity.class);
                startActivity(i);
            }

        }
    }
}
