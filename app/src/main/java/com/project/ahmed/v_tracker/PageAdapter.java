package com.project.ahmed.v_tracker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Ahmed on 11/17/2017.
 */

public class PageAdapter extends FragmentStatePagerAdapter {

    public PageAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new DevicesFragment();
            case 1:
                return new MapFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
