package com.lance.coolweather.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.lance.common.util.JSONUtil;
import com.lance.common.util.SPUtil;
import com.lance.common.widget.TopBar;
import com.lance.coolweather.R;
import com.lance.coolweather.adapter.CityListAdapter;
import com.lance.coolweather.config.AppConfig;
import com.lance.coolweather.db.County;

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
        String countyListJson = (String) SPUtil.get(this, AppConfig.SHARE_KEY_CITY_LIST, "");
        if (!TextUtils.isEmpty(countyListJson)) {
            List<County> countyList = JSONUtil.getObjectFromJson(countyListJson, new TypeToken<List<County>>() {
            }.getType());
            if (countyList != null && !countyList.isEmpty()) {
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
                        if (!cityList.contains(county)) {
                            cityList.add(county);
                            adapter.notifyItemInserted(cityList.size() - 1);
                            updated = true;
                        }
                    }
                }
                break;
        }
    }

    //返回或关闭时保存城市列表
    private void saveResult() {
        String countyJson = JSONUtil.getJsonFromObject(cityList);
        if (countyJson == null) {
            countyJson = "";
        }
        SPUtil.put(this, AppConfig.SHARE_KEY_CITY_LIST, countyJson);
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
}
