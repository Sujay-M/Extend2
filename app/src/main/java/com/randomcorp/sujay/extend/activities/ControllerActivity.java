package com.randomcorp.sujay.extend.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.randomcorp.sujay.extend.R;
import com.randomcorp.sujay.extend.Utils.PagerAdapter;
import com.randomcorp.sujay.extend.networking.ExtendProtocol;
import com.randomcorp.sujay.extend.networking.ServerExtendProtocol;

/**
 * Created by sujay on 23/9/15.
 */
public class ControllerActivity extends AppCompatActivity implements ExtendProtocol
{
    TabLayout tabLayout;
    TabLayout.Tab video,image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("VIDEO"));
        tabLayout.addTab(tabLayout.newTab().setText("IMAGE"));
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Log.d("Controller", "tab no = " + tab.getPosition() + " text = " + tab.getText());
                if(tab.getText().toString().equals("VIDEO"))
                    ServerExtendProtocol.getSingleton().sendCommandToAll(initVideo,false);
                else if(tab.getText().toString().equals("IMAGE"))
                    ServerExtendProtocol.getSingleton().sendCommandToAll(initImage,false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

}
