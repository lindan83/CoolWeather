package com.lance.coolweather.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.reflect.TypeToken;
import com.lance.common.util.DateUtil;
import com.lance.common.util.JSONUtil;
import com.lance.common.util.SPUtil;
import com.lance.coolweather.R;
import com.lance.coolweather.adapter.CityPagerAdapter;
import com.lance.coolweather.api.WeatherService;
import com.lance.coolweather.config.AppConfig;
import com.lance.coolweather.config.AppUtil;
import com.lance.coolweather.db.County;
import com.lance.coolweather.fragment.CityWeatherFragment;
import com.lance.coolweather.service.AutoUpdateService;
import com.lance.network.okhttputil.OkHttpUtils;
import com.lance.network.okhttputil.callback.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Response;

public class WeatherActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "WeatherActivity";
    private static final int RC_ADD_CITY = 1000;
    private static final int RC_MANAGER_CITY = 1001;

    private LinearLayout llIndicators;
    private ViewPager vpCities;
    private RelativeLayout rlMain;
    private Button btnAddCity;
    private final List<View> cityIndicatorViews = new ArrayList<>();
    private CityPagerAdapter pagerAdapter;
    private final Map<String, CityWeatherFragment> fragmentMap = new HashMap<>();
    private final List<County> countyList = new ArrayList<>();
    private int currentIndex;

    private final BroadcastReceiver refreshImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.equals(AppConfig.ACTION_REFRESH_IMAGE, action)) {
                String imageUrl = intent.getStringExtra(AppConfig.SHARE_IMAGE_URL);
                Log.d(TAG, "onReceive: image = " + imageUrl);
                setBackground(imageUrl);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initData();
        initViews();

        if (AppUtil.isAutoUpdateEnabled(this)) {
            //开启后台更新服务
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.ACTION_REFRESH_IMAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshImageReceiver, intentFilter);

        requestImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshImageReceiver);
    }

    private void initData() {
        String countyListJson = (String) SPUtil.get(this, AppConfig.SHARE_KEY_CITY_LIST, "");
        List<County> temp = JSONUtil.getObjectFromJson(countyListJson, new TypeToken<List<County>>() {
        }.getType());
        if (temp != null && !temp.isEmpty()) {
            countyList.addAll(temp);
            for (County county : countyList) {
                CityWeatherFragment fragment = CityWeatherFragment.newInstance(county.weatherId);
                fragmentMap.put(county.weatherId, fragment);
            }
        }
        pagerAdapter = new CityPagerAdapter(getSupportFragmentManager(), new ArrayList<>(fragmentMap.values()));
    }

    private void refreshCityList() {
        String countyListJson = (String) SPUtil.get(this, AppConfig.SHARE_KEY_CITY_LIST, "");
        List<County> temp = JSONUtil.getObjectFromJson(countyListJson, new TypeToken<List<County>>() {
        }.getType());
        countyList.clear();
        if (temp != null && !temp.isEmpty()) {
            countyList.addAll(temp);
        }
        if (!countyList.isEmpty()) {
            //增加新添加的城市
            for (County county : countyList) {
                if (!fragmentMap.containsKey(county.weatherId)) {
                    CityWeatherFragment newFragment = CityWeatherFragment.newInstance(county.weatherId);
                    fragmentMap.put(county.weatherId, newFragment);
                    pagerAdapter.addFragment(newFragment);
                }
            }
            //删除被删除的城市
            List<String> countyIdList = new ArrayList<>(countyList.size());
            for (County county : countyList) {
                countyIdList.add(county.weatherId);
            }
            Set<String> keySet = fragmentMap.keySet();
            for (String oldCityId : keySet) {
                if (!countyIdList.contains(oldCityId)) {
                    fragmentMap.remove(oldCityId);
                    pagerAdapter.removeFragment(oldCityId);
                }
            }
        } else {
            fragmentMap.clear();
            pagerAdapter.clearFragments();
        }
        if (currentIndex >= countyList.size()) {
            currentIndex = countyList.size() - 1;
        }
        vpCities.setVisibility(fragmentMap.isEmpty() ? View.GONE : View.VISIBLE);
        vpCities.setCurrentItem(currentIndex);
        btnAddCity.setVisibility(countyList.isEmpty() ? View.VISIBLE : View.GONE);
        addIndicatorView(llIndicators, currentIndex, countyList.size());
    }

    private void initViews() {
        rlMain = (RelativeLayout) findViewById(R.id.rl_main);
        Button btnCityManager = (Button) findViewById(R.id.btn_city_manager);
        btnCityManager.setOnClickListener(this);
        Button btnSetting = (Button) findViewById(R.id.btn_setting);
        btnSetting.setOnClickListener(this);
        btnAddCity = (Button) findViewById(R.id.btn_add_city);
        btnAddCity.setVisibility(countyList.isEmpty() ? View.VISIBLE : View.GONE);
        btnAddCity.setOnClickListener(this);
        vpCities = (ViewPager) findViewById(R.id.vp_cities);
        vpCities.setVisibility(fragmentMap.isEmpty() ? View.GONE : View.VISIBLE);
        llIndicators = (LinearLayout) findViewById(R.id.ll_indicators);

        vpCities.setAdapter(pagerAdapter);
        vpCities.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                for (int i = 0, count = cityIndicatorViews.size(); i < count; i++) {
                    cityIndicatorViews.get(i).setSelected(i == position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vpCities.setCurrentItem(currentIndex);
        addIndicatorView(llIndicators, currentIndex, countyList.size());
    }

    private void addIndicatorView(LinearLayout guideGroup, int startPos, int count) {
        guideGroup.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
        cityIndicatorViews.clear();
        guideGroup.removeAllViews();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                View view = new View(this);
                view.setBackgroundResource(R.drawable.bg_selector_city);
                view.setSelected(i == startPos);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.indicator_view_width),
                        getResources().getDimensionPixelSize(R.dimen.indicator_view_height));
                layoutParams.setMargins(10, 0, 0, 0);
                guideGroup.addView(view, layoutParams);
                cityIndicatorViews.add(view);
            }
        }
    }

    //请求图片
    private void requestImage() {
        WeatherService.getInstance().getTodaysImage(new Callback<String>() {
            @Override
            public String parseNetworkResponse(Response response, int id) throws Exception {
                return response.body().string();
            }

            @Override
            public void onError(Call call, Response response, Exception exception, int id) {
                rlMain.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }

            @Override
            public void onResponse(String response, int id) {
                SPUtil.put(WeatherActivity.this, AppConfig.SHARE_IMAGE_URL, response);
                SPUtil.put(WeatherActivity.this, AppConfig.SHARE_IMAGE_DATE, DateUtil.getDate(new Date(), "yyyyMMdd"));
                setBackground(response);
            }
        }, this);
    }

    private void setBackground(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            rlMain.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            OkHttpUtils.get().url(imageUrl).build().execute(new Callback<Drawable>() {
                @Override
                public Drawable parseNetworkResponse(Response response, int id) throws Exception {
                    InputStream is = null;
                    try {
                        is = response.body().byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        return new BitmapDrawable(getResources(), bitmap);
                    } catch (Exception e) {
                        return null;
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                Log.e(TAG, "parseNetworkResponse: " + e.toString());
                            }
                        }
                    }
                }

                @Override
                public void onError(Call call, Response response, Exception exception, int id) {
                    Log.e(TAG, "onError: " + exception.toString());
                    rlMain.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }

                @Override
                public void onResponse(Drawable response, int id) {
                    if (response != null) {
                        rlMain.setBackground(response);
                    } else {
                        rlMain.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.btn_city_manager:
                startActivityForResult(new Intent(this, CityManagerActivity.class), RC_MANAGER_CITY);
                break;
            case R.id.btn_setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.btn_add_city:
                startActivityForResult(new Intent(this, ChooseAreaActivity.class), RC_ADD_CITY);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_ADD_CITY:
                if (resultCode == RESULT_OK) {
                    String countyId = data.getStringExtra(AppConfig.PARAM_CITY_ID);
                    String countyName = data.getStringExtra(AppConfig.PARAM_CITY_NAME);
                    if (!TextUtils.isEmpty(countyId)) {
                        County county = new County();
                        county.weatherId = countyId;
                        county.countyName = countyName;
                        countyList.add(county);
                        String countyListJson = JSONUtil.getJsonFromObject(countyList);
                        if (countyListJson == null) {
                            countyListJson = "";
                        }
                        SPUtil.put(this, AppConfig.SHARE_KEY_CITY_LIST, countyListJson);
                        btnAddCity.setVisibility(countyList.isEmpty() ? View.VISIBLE : View.GONE);
                        refreshCityList();
                    }
                }
                break;
            case RC_MANAGER_CITY:
                refreshCityList();
                break;
        }
    }
}