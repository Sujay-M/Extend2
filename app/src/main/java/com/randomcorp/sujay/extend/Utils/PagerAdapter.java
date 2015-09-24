package com.randomcorp.sujay.extend.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.randomcorp.sujay.extend.fragments.ServerImageFragment;
import com.randomcorp.sujay.extend.fragments.ServerVideoFragment;

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
                ServerVideoFragment tab1 = new ServerVideoFragment();
                return tab1;
            case 1:
                ServerImageFragment tab2 = new ServerImageFragment();
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
