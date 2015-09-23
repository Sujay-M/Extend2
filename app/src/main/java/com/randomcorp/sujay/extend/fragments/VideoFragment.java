package com.randomcorp.sujay.extend.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.randomcorp.sujay.extend.R;

/**
 * Created by sujay on 23/9/15.
 */
public class VideoFragment extends Fragment
{
    private TextView nowPlaying,currentTime,finalTime;
    private ImageButton bPrev,bPlay,bNext;
    private SeekBar seekBar;
    private Spinner videoList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.video_fragment, container, false);

    }
}
