package com.project.ahmed.v_tracker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Ahmed on 3/16/2018.
 */

public class HowItWorksPageAdapter extends FragmentStatePagerAdapter {

    public HowItWorksPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new PageOneFragment();
            case 1:
                return new PageTwoFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
