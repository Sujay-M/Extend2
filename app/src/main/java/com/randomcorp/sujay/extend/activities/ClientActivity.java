package com.randomcorp.sujay.extend.activities;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.randomcorp.sujay.extend.R;
import com.randomcorp.sujay.extend.fragments.ClientImageFragment;
import com.randomcorp.sujay.extend.fragments.ClientVideoFragment;
import com.randomcorp.sujay.extend.networking.ClientExtendProtocol;
import com.randomcorp.sujay.extend.networking.ExtendProtocol;

import java.io.File;
import java.net.DatagramPacket;

/**
 * Created by sujay on 21/9/15.
 */
public class ClientActivity extends AppCompatActivity implements ClientExtendProtocol.CommandFromServer,ExtendProtocol
{
    private static final String TAG = "CLIENT ACTIVITY";
    private ClientExtendProtocol clientProtocol;
    private LinearLayout mainView;
    private long clock_skew=0;
    private int sync_no;
    private ClientImageFragment imageFragment;
    private ClientVideoFragment videoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.client_layout);
        mainView = (LinearLayout)findViewById(R.id.ll_main_view);
        clientProtocol = new ClientExtendProtocol(this);
        videoFragment = new ClientVideoFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.ll_main_view,videoFragment,"VIDEO");
        transaction.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        clientProtocol.startClientProtocol();
    }

    @Override
    protected void onPause() {
        super.onPause();
        clientProtocol.stopClientProtocol();
    }

    @Override
    public void commandReceived(String type, String data)
    {
        ((TextView)findViewById(R.id.tv_msgrceived)).setText(data);
        if(type.equals(initImage))
        {

            return;
        }
        else if(type.equals(initVideo))
        {

            return;
        }
        String dataParts[] = data.split(delimiter);
        switch(type)
        {
            case commandHeader:
                long serverTime,clientTime,sleepTime;
                FragmentTransaction transaction;
                switch (dataParts[0])
                {
                    case initImage:
                        imageFragment = new ClientImageFragment();
                        transaction = getFragmentManager().beginTransaction();
                        if(transaction.isEmpty())
                        {
                            transaction.add(R.id.ll_main_view,imageFragment,"IMAGE");
                        }
                        else
                        {
                            transaction.replace(R.id.ll_main_view,imageFragment,"IMAGE");
                            transaction.addToBackStack(null);
                        }
                        transaction.commit();
                        videoFragment = null;
                        break;

                    case initVideo:
                        videoFragment = new ClientVideoFragment();
                        transaction = getFragmentManager().beginTransaction();
                        if(transaction.isEmpty())
                        {
                            transaction.add(R.id.ll_main_view,videoFragment,"VIDEO");
                        }
                        else
                        {
                            transaction.replace(R.id.ll_main_view,videoFragment,"VIDEO");
                            transaction.addToBackStack(null);
                        }
                        transaction.commit();
                        imageFragment = null;
                        break;
                    case commandWhite:
                        mainView.setBackgroundColor(Color.WHITE);
                        break;
                    case commandRed:
                        mainView.setBackgroundColor(Color.RED);
                        break;
                    case commandPlay:
                        serverTime = Long.parseLong(dataParts[1])+1000;
                        clientTime = SystemClock.elapsedRealtime();
                        sleepTime = (serverTime+clock_skew) - clientTime;
                        videoFragment.controlMedialPlayer(commandPlay,sleepTime);
                        break;
                    case commandPause:
                        serverTime = Long.parseLong(dataParts[1])+1000;
                        clientTime = SystemClock.elapsedRealtime();
                        sleepTime = (serverTime+clock_skew) - clientTime;
                        videoFragment.controlMedialPlayer(commandPause,sleepTime);
                        break;
                    case commandSeek:

                        break;
                    case commandStop:
                        serverTime = Long.parseLong(dataParts[1])+1000;
                        clientTime = SystemClock.elapsedRealtime();
                        sleepTime = (serverTime+clock_skew) - clientTime;
                        videoFragment.controlMedialPlayer(commandStop,sleepTime);
                        break;
                    case commandSync:
                        initiateSynchronization();
                        break;
                }
                break;
            case dataHeader:
                switch (dataParts[0])
                {
                    case caliberationHeader:
                        int orientation = Integer.parseInt(dataParts[1]);
                        float[] calibData = new float[8];
                        for (int i = 2;i<10;i++)
                            calibData[i-2] = Float.parseFloat(dataParts[i]);
                        setTextureView(orientation,calibData);
                        break;
                    case fileNameHeader:

                        String fileName = ""+dataParts[1];
                        for(int i=2;i<dataParts.length;i++)
                            fileName+=" "+dataParts[i];
                        String file = Environment.getExternalStorageDirectory().toString()+fileName;
                        Log.d(TAG,"file header called, file = "+file);
                        if(new File(file).exists())
                        {
                            Log.d(TAG,"File exists");
                            videoFragment.init(file);
                        }
                        break;
                    case syncHeader:
                        if(clock_skew==0)
                        {
                            sync_no = 1;
                            clock_skew = Long.parseLong(dataParts[1]);
                            Log.d(TAG, "Initial clock skew = " + clock_skew);
                        }
                        else
                        {
                            clock_skew = (clock_skew*sync_no+Long.parseLong(dataParts[1]))/(sync_no+1);
                            sync_no++;
                            Log.d(TAG,"obtained skew = "+dataParts[1]+" current = "+clock_skew);
                        }
                        break;
                }


        }

    }



    private void initiateSynchronization()
    {
        sync_no = 0;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run()
            {
                for (int i=1;i<=10;i++)
                {
                    clientProtocol.sendSyncPacket();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    private void setTextureView(int orientation,float values[])
    {
        videoFragment.setTextureView(orientation, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7]);
    }
}
