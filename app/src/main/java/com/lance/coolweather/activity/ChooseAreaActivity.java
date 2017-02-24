package com.lance.coolweather.activity;

import android.os.Bundle;

import com.lance.coolweather.R;
import com.lance.coolweather.fragment.ChooseAreaFragment;

public class ChooseAreaActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);
    }

    @Override
    public void onBackPressed() {
        ChooseAreaFragment fragment = (ChooseAreaFragment) getSupportFragmentManager().getFragments().get(0);
        int level = fragment.getCurrentLevel();
        if (level == ChooseAreaFragment.LEVEL_COUNTY) {
            fragment.queryCities();
        } else if (level == ChooseAreaFragment.LEVEL_CITY) {
            fragment.queryProvinces();
        } else {
            super.onBackPressed();
        }
    }
}
