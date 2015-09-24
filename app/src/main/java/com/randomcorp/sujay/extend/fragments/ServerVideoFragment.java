package com.randomcorp.sujay.extend.fragments;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.randomcorp.sujay.extend.R;
import com.randomcorp.sujay.extend.imageProcessing.LayoutModel;
import com.randomcorp.sujay.extend.networking.ExtendProtocol;
import com.randomcorp.sujay.extend.networking.ServerExtendProtocol;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sujay on 23/9/15.
 */
public class ServerVideoFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener,ExtendProtocol {
    private static final String TAG = "Server Video Fragment" ;
    private TextView nowPlaying,currentTime,finalTime;
    private ImageButton bPrev,bPlay,bNext;
    private SeekBar seekBar;
    private Spinner videoList;
    private List<String> files;
    private ArrayAdapter<String> videoListAdapter;
    private ServerExtendProtocol server;
    private static final String storagePath = Environment.getExternalStorageDirectory().toString();
    private static final String folderPath = "/Extend/Videos/";
    private boolean isPlaying;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        server = ServerExtendProtocol.getSingleton();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.video_fragment, container, false);
        nowPlaying = (TextView)v.findViewById(R.id.tv_now_playing);
        currentTime = (TextView)v.findViewById(R.id.tv_initial);
        finalTime = (TextView)v.findViewById(R.id.tv_final);
        bPrev = (ImageButton)v.findViewById(R.id.ib_prev);
        bPlay = (ImageButton)v.findViewById(R.id.ib_play);
        bNext = (ImageButton)v.findViewById(R.id.ib_next);
        seekBar = (SeekBar)v.findViewById(R.id.sb_Seek);
        videoList = (Spinner)v.findViewById(R.id.spinner);
        files = new ArrayList<>();
        getFiles();
        videoListAdapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_item,files);
        videoList.setAdapter(videoListAdapter);
        bPrev.setOnClickListener(this);
        bPlay.setOnClickListener(this);
        bNext.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        videoList.setOnItemSelectedListener(this);
        server.sendCommandToAll(commandSync,false);
        return v;

    }

    private void getFiles()
    {

        files.clear();
        File f = new File(storagePath+folderPath);
        if(f.exists())
        {
            File file[] = f.listFiles();
            for (int i=0; i < file.length; i++)
                files.add(file[i].getName());

        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.ib_play:
                if(isPlaying)
                {
                    server.sendCommandToAll(commandPause,true);
                    isPlaying = false;
                    togglePlayButton();
                }
                else
                {
                    server.sendCommandToAll(commandPlay,true);
                    isPlaying = true;
                    togglePlayButton();
                }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        isPlaying = false;
        togglePlayButton();
        String selectedFile = parent.getItemAtPosition(position).toString();
        nowPlaying.setText(selectedFile);
        selectedFile = folderPath+selectedFile;
        server.sendDataToAll(fileNameHeader + delimiter+selectedFile);

        int videoHeight,videoWidth;
        try
        {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(storagePath+selectedFile);
            String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            videoHeight = Integer.parseInt(height);
            videoWidth = Integer.parseInt(width);
            calibrate(videoWidth, videoHeight);
            metaRetriever.release();
        }catch(IllegalArgumentException e)
        {
            Toast.makeText(getActivity(), "File cant be opened", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
    private void calibrate(int width, int height)
    {
        List <Integer[]> layout = LayoutModel.getSingleton().getLayout();
        if(layout!=null)
        {
            int max = 0,selHeight = 0,selWidth = 0;
            Integer[] selected = null;
            for(Integer[] rect:layout)
            {
                int newHeight,newWidth;
                int w = rect[2] - rect[0];
                int h = rect[3] - rect[1];
                float heightScale = ((float)h/(float)height);
                float widthScale = ((float)w/(float)width);
                if(heightScale>=widthScale)
                {
                    newHeight = (int)(height*widthScale);
                    newWidth = w;
                }
                else
                {
                    newWidth = (int)(width*heightScale);
                    newHeight = h;
                }
                Log.d(TAG, "dimens video = (" + width + "," + height + ")");
                Log.d(TAG,"dimens imageProcessing = ("+w+","+h+")");
                Log.d(TAG,"dimens scaled = ("+newWidth+","+newHeight+")");
                int area = newHeight*newWidth;
                if(area>max)
                {
                    max = area;
                    selected = rect;
                    selHeight = newHeight;
                    selWidth = newWidth;
                }
            }
            if(selected!=null)
            {
                int offH = (int)((float)((selected[3]-selected[1])-selHeight)/2);
                int offW = ((selected[2]-selected[0])-selWidth)/2;

                int left = selected[0]+offW;
                int right = selected[2]-offW;
                int top = selected[1]+offH;
                int bottom = selected[3]-offH;
                Log.d(TAG,"SELECTED left = "+left+" right = "+right+" top = "+top+" bottom = "+bottom);
                for(int i = 0;i < server.noOfClients();i++)
                {
                    Integer[] rect = LayoutModel.getSingleton().getClientRect(i);
                    int orientation = 1;
                    int l = (rect[0]>left)?rect[0]:left;
                    int ri = (rect[0]+rect[2]<right)?rect[0]+rect[2]:right;
                    int t = (rect[1]>top)?rect[1]:top;
                    int b = (rect[1]+rect[3]<bottom?rect[1]+rect[3]:bottom);
                    Log.d(TAG,"client actual l = "+rect[0]+" r = "+(rect[0]+rect[2])+" t = "+rect[1]+" b = "+(rect[1]+rect[3]));
                    Log.d(TAG,"client left = "+l+" right = "+ri+" top = "+t+" bottom = "+b);
                    float x1 = getPercentage(l-left,right-left);
                    float x2 = getPercentage(ri-left,right-left);
                    float y1 = getPercentage(t-top,bottom-top);
                    float y2 = getPercentage(b-top,bottom-top);
                    float offx1 = getPercentage(l-rect[0],rect[2]);
                    float offx2 = getPercentage(ri-rect[0],rect[2]);
                    float offy1 = getPercentage(t-rect[1],rect[3]);
                    float offy2 = getPercentage(b-rect[1],rect[3]);
                    String msg = caliberationHeader + delimiter+ orientation + delimiter+
                            x1 +delimiter+x2 +delimiter+y1 +delimiter+y2 +
                            delimiter+offx1 +delimiter+offx2 +delimiter+offy1 +delimiter+offy2;
                    server.sendDataMessage(i,msg);
                }
            }
        }
        else
            Toast.makeText(getActivity(),"Layout not Selected",Toast.LENGTH_LONG).show();
    }
    private float getPercentage(int val,int total)
    {
        return (float)val/(float)total;
    }

    private void togglePlayButton()
    {
        if(isPlaying)
            bPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_pause_white_24dp));
        else
            bPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_play_arrow_white_24dp));
    }
}
