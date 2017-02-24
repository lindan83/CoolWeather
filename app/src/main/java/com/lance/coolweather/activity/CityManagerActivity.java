package com.lance.coolweather.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.lance.common.util.SPUtil;
import com.lance.common.widget.TopBar;
import com.lance.coolweather.R;
import com.lance.coolweather.adapter.CityListAdapter;
import com.lance.coolweather.config.AppConfig;
import com.lance.coolweather.db.County;
import com.lance.coolweather.db.DBAccessHelper;
import com.lance.coolweather.util.ParseCityIdUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 城市管理
 */
public class CityManagerActivity extends BaseActivity {
    private static final int RC_ADD_CITY = 1000;
    private CityListAdapter adapter;
    private List<County> cityList = new ArrayList<>();

    private boolean updated;//是否修改过城市列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manager);

        initData();
        initView();
    }

    private void initData() {
        String cityString = (String) SPUtil.get(this, AppConfig.SHARE_KEY_CITY_ID_LIST, "");
        if (!TextUtils.isEmpty(cityString)) {
            List<String> cityIdList = ParseCityIdUtil.parse(cityString);
            List<County> countyList = DBAccessHelper.findCountyList(cityIdList);
            if (countyList != null && !countyList.isEmpty() && countyList.size() == cityIdList.size()) {
                cityList.addAll(countyList);
            }
        }
        adapter = new CityListAdapter(this, cityList);
    }

    private void initView() {
        TopBar tbTitleBar = (TopBar) findViewById(R.id.tb_title_bar);
        tbTitleBar.setOnClickTopBarListener(new TopBar.OnClickListener() {
            @Override
            public void onClickLeft() {
                if (updated) {
                    saveResult();
                    setResult(RESULT_OK);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
            }

            @Override
            public void onClickTitle() {

            }

            @Override
            public void onClickRight() {
                Intent intent = new Intent(CityManagerActivity.this, ChooseAreaActivity.class);
                startActivityForResult(intent, RC_ADD_CITY);
            }
        });
        RecyclerView rvCity = (RecyclerView) findViewById(R.id.rv_city);
        rvCity.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvCity.setItemAnimator(new DefaultItemAnimator());
        rvCity.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvCity.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (updated) {
            saveResult();
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_ADD_CITY:
                if (resultCode == RESULT_OK) {
                    String cityId = data.getStringExtra(AppConfig.PARAM_CITY_ID);
                    String cityName = data.getStringExtra(AppConfig.PARAM_CITY_NAME);
                    if (!TextUtils.isEmpty(cityId) && !TextUtils.isEmpty(cityName)) {
                        County county = new County();
                        county.weatherId = cityId;
                        county.countyName = cityName;
                        cityList.add(county);
                        adapter.notifyItemInserted(cityList.size() - 1);
                        updated = true;
                    }
                }
                break;
        }
    }

    private void saveResult() {
        List<String> idList = new ArrayList<>();
        for (County county : cityList) {
            idList.add(county.weatherId);
        }
        String idString = ParseCityIdUtil.format(idList);
        SPUtil.put(this, AppConfig.SHARE_KEY_CITY_ID_LIST, idString);
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
}
