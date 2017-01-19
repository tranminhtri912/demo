package com.framgia.rss_6.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.framgia.rss_6.ui.activity.fragment.HistoryFragment;
import com.framgia.rss_6.ui.activity.fragment.ListNewsFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    public PagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new ListNewsFragment();
                break;
            case 1:
                fragment = new HistoryFragment();
                break;
            default:
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = "News";
                break;
            case 1:
                title = "History";
                break;
            default:
                break;
        }
        return title;
    }
}