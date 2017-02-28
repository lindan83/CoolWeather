package com.lance.coolweather.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;

import com.lance.coolweather.fragment.CityWeatherFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lindan on 17-2-23.
 */
public class CityPagerAdapter extends FragmentPagerAdapter {
    private final List<CityWeatherFragment> fragments = new ArrayList<>();

    public CityPagerAdapter(FragmentManager fm, List<CityWeatherFragment> fragments) {
        super(fm);
        this.fragments.clear();
        this.fragments.addAll(fragments);
    }

    @Override
    public Fragment getItem(int index) {
        if (fragments == null) {
            return null;
        }
        if (index < 0 || index > fragments.size() - 1) {
            return null;
        }
        return fragments.get(index);
    }

    @Override
    public int getCount() {
        if (fragments == null) {
            return 0;
        }
        return fragments.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addFragment(CityWeatherFragment fragment) {
        fragments.add(fragment);
        notifyDataSetChanged();
    }

    public void removeFragment(String cityId) {
        for (CityWeatherFragment fragment : fragments) {
            if (TextUtils.equals(fragment.getCityId(), cityId)) {
                fragments.remove(fragment);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void clearFragments() {
        fragments.clear();
        notifyDataSetChanged();
    }
}