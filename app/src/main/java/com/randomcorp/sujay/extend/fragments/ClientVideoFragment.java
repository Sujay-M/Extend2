package com.randomcorp.sujay.extend.fragments;

import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.randomcorp.sujay.extend.R;
import com.randomcorp.sujay.extend.models.DeviceModel;
import com.randomcorp.sujay.extend.networking.ExtendProtocol;

import java.io.IOException;

/**
 * Created by sujay on 24/9/15.
 */
public class ClientVideoFragment extends Fragment implements TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener,ExtendProtocol {
    private static final String TAG = "Video Fragment";
    private TextureView textureView;
    private MediaPlayer mediaPlayer;
    private Surface surface;
    private boolean mediaPrepared;
    private DeviceModel dev;
    private FrameLayout frameLayout;

    public ClientVideoFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v =  inflater.inflate(R.layout.client_video_fragment, container, false);
        frameLayout = (FrameLayout)v.findViewById(R.id.fl_main_view);
        textureView = (TextureView)v.findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        mediaPlayer = null;
        return v;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surface = new Surface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void initMediaPlayer()
    {
        try
        {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);
            mediaPlayer.setOnPreparedListener(this);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.getMessage());
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
        }
        changeBackgroundColor("BLACK");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPrepared = true;
        Log.d(TAG,"Media prepared");
    }

    public void stopPlayer()
    {
        if(mediaPlayer!=null)
        {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            mediaPrepared = false;
        }
    }

    public void init(String file)
    {
        dev = new DeviceModel();
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        dev.devHeight = metrics.heightPixels;
        dev.devWidth  = metrics.widthPixels;
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        try
        {
            metaRetriever.setDataSource(file);
            String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            dev.vidHeight = Integer.parseInt(height);
            dev.vidWidth = Integer.parseInt(width);
        }catch(IllegalArgumentException e)
        {
            Toast.makeText(getActivity(), "File cant be opened", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        try
        {
            stopPlayer();
            initMediaPlayer();
            mediaPlayer.setDataSource(file);
            mediaPlayer.prepareAsync();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        metaRetriever.release();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void setTextureView(int orientation,
                                float x1,float x2,float y1,float y2,
                                float offsetX1,float offsetX2,
                                float offsetY1,float offsetY2)
    {
        int DEVHEIGHT,DEVWIDTH;
        if(orientation==1)
        {
            DEVHEIGHT = dev.devHeight;
            DEVWIDTH = dev.devWidth;
        }
        else
        {
            DEVWIDTH = dev.devHeight;
            DEVHEIGHT= dev.devWidth;
        }
        int heightDisp = (int)((y2-y1)*dev.vidHeight);
        int heightScaled = (int)((offsetY2-offsetY1)*DEVHEIGHT);
        int widthScaled = (int)((offsetX2-offsetX1)*DEVWIDTH);
        int h1 = (int)(offsetY1*DEVHEIGHT);
        int h2 = (int)((1.0-offsetY2)*DEVHEIGHT);
        int w1 = (int)(offsetX1*DEVWIDTH);
        int w2 = (int)((1.0-offsetX2)*DEVWIDTH);

        Matrix matrix = new Matrix();

        float scaleH = (float)dev.vidHeight/(float)heightDisp;
        float scaleW = ((float)heightScaled/(float)heightDisp)/((float)(DEVWIDTH)/(float)dev.vidWidth);
        float scale = (float)heightScaled/(float)heightDisp;
        float x = -x1*dev.vidWidth*scale;
        float y = -y1*dev.vidHeight*scale;
        matrix.setScale(scaleW,scaleH,0,0);
        matrix.postTranslate(x,y);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(widthScaled,heightScaled);
        layoutParams.setMargins(w1,h1,w2,h2);
        textureView.setTransform(matrix);
        textureView.setLayoutParams(layoutParams);
    }

    public void controlMedialPlayer(String command, final long sleepTime)
    {
        if(mediaPrepared)
        {
            switch (command)
            {
                case commandPlay:
                    Log.d(TAG, "Play command sleep time - "+sleepTime);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(sleepTime);
                                mediaPlayer.start();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
                    break;

                case commandPause:
                    Log.d(TAG, "Pause command sleep time - "+sleepTime);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(sleepTime);
                                mediaPlayer.pause();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
                    break;
                case commandStop:
                    Log.d(TAG, "Stop command sleep time - "+sleepTime);
                    try {
                        Thread.sleep(sleepTime);
                        mediaPlayer.stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case commandSeek:
                    break;
            }
        }

    }
    public void changeBackgroundColor(String color)
    {
        if(color.equals("WHITE"))
            frameLayout.setBackgroundColor(Color.WHITE);
        else if(color.equals("RED"))
            frameLayout.setBackgroundColor(Color.RED);
        else if(color.equals("BLACK"))
            frameLayout.setBackgroundColor(Color.BLACK);
    }
}
