package com.randomcorp.sujay.extend.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.randomcorp.sujay.extend.R;
import com.randomcorp.sujay.extend.fragments.ServerVideoFragment;

/**
 * Created by sujay on 23/9/15.
 */
public class ControllerActivity extends AppCompatActivity
{
    ServerVideoFragment videoFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller_activity);
        videoFragment = new ServerVideoFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_layout, videoFragment, "VIDEO");
        transaction.commit();
    }

}
