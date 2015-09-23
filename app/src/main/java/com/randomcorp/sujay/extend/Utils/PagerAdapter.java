package com.randomcorp.sujay.extend.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.randomcorp.sujay.extend.fragments.ImageFragment;
import com.randomcorp.sujay.extend.fragments.VideoFragment;

import org.opencv.video.Video;

/**
 * Created by sujay on 24/9/15.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                VideoFragment tab1 = new VideoFragment();
                return tab1;
            case 1:
                ImageFragment tab2 = new ImageFragment();
                return tab2;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
