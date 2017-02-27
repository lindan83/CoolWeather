package com.lance.coolweather.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.reflect.TypeToken;
import com.lance.common.recyclerview.adapter.AbstractRecyclerViewAdapter;
import com.lance.common.recyclerview.adapter.CommonRecyclerViewAdapter;
import com.lance.common.recyclerview.adapter.base.CommonRecyclerViewHolder;
import com.lance.common.util.JSONUtil;
import com.lance.common.util.ToastUtil;
import com.lance.common.widget.dialog.CustomizableAlertDialog;
import com.lance.common.widget.dialog.CustomizableConfirmDialog;
import com.lance.common.widget.dialog.DialogUtil;
import com.lance.coolweather.R;
import com.lance.coolweather.api.WeatherService;
import com.lance.coolweather.api.result.GetAreaListResult;
import com.lance.coolweather.api.result.GetCityInfoResult;
import com.lance.coolweather.config.AppConfig;
import com.lance.coolweather.db.City;
import com.lance.coolweather.db.County;
import com.lance.coolweather.db.DBAccessHelper;
import com.lance.coolweather.db.Province;
import com.lance.coolweather.util.ParseAreaUtil;
import com.lance.network.okhttputil.callback.Callback;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by lindan on 17-2-21.
 * 选择区域
 */

public class ChooseAreaFragment extends BaseFragment implements AbstractRecyclerViewAdapter.OnItemClickListener, View.OnClickListener, BDLocationListener {
    public static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private TextView tvTitle;
    private Button btnBack;
    private RecyclerView rvArea;
    private CommonRecyclerViewAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        progressDialog = new ProgressDialog(getActivity());
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        btnBack = (Button) view.findViewById(R.id.btn_back);
        rvArea = (RecyclerView) view.findViewById(R.id.rv_area);
        rvArea.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvArea.setItemAnimator(new DefaultItemAnimator());
        adapter = new CommonRecyclerViewAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, dataList) {
            @Override
            protected void convert(CommonRecyclerViewHolder holder, String item, int position) {
                holder.setText(android.R.id.text1, item);
            }
        };
        rvArea.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter.setOnItemClickListener(this);
        btnBack.setOnClickListener(this);

        initPosition();
    }

    private void initPosition() {
        LocationClient locationClient = new LocationClient(getActivity());
        LocationClientOption option = new LocationClientOption();
        //设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("bd09ll");
        //设置发起定位请求的间隔需要大于等于1000ms才是有效的.默认0，即仅定位一次
        option.setScanSpan(0);
        //设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //默认false,设置是否使用gps
        option.setOpenGps(false);
        //默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setLocationNotify(false);
        //默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(false);
        //默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(false);
        //默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(false);
        //默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);
        //默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setEnableSimulateGps(false);

        locationClient.setLocOption(option);
        locationClient.registerLocationListener(this);
        locationClient.start();
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        if (currentLevel == LEVEL_PROVINCE) {
            selectedProvince = provinceList.get(position);
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            selectedCity = cityList.get(position);
            queryCounties();
        } else if (currentLevel == LEVEL_COUNTY) {
            String cityId = countyList.get(position).weatherId;
            String cityName = countyList.get(position).countyName;
            County county = DBAccessHelper.findCounty(cityId);
            if (county == null) {
                //本地数据库不存在，查询接口
                queryCounties();
            } else {
                Activity activity = getActivity();
                Intent data = new Intent();
                data.putExtra(AppConfig.PARAM_CITY_ID, cityId);
                data.putExtra(AppConfig.PARAM_CITY_NAME, cityName);
                activity.setResult(Activity.RESULT_OK, data);
                activity.finish();
            }
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.btn_back:
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
        }
    }

    public void queryCities() {
        tvTitle.setText(selectedProvince.provinceName);
        btnBack.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceCode=?", String.valueOf(selectedProvince.code)).find(City.class);
        if (cityList != null && !cityList.isEmpty()) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.cityName);
            }
            adapter.notifyDataSetChanged();
            rvArea.scrollToPosition(0);
            currentLevel = LEVEL_CITY;
        } else {
            showProgressDialog();
            long provinceCode = selectedProvince.code;
            WeatherService.getInstance().getCityList(provinceCode, new Callback<List<GetAreaListResult>>() {
                @Override
                public List<GetAreaListResult> parseNetworkResponse(Response response, int id) throws Exception {
                    String json = response.body().string();
                    return JSONUtil.getObjectFromJson(json, new TypeToken<List<GetAreaListResult>>() {
                    }.getType());
                }

                @Override
                public void onError(Call call, Response response, Exception exception, int id) {
                    closeProgressDialog();
                    Log.e(TAG, "onError: " + exception.toString());
                    DialogUtil.showAlertDialog(getActivity(), "", getString(R.string.app_common_error_msg), getString(R.string.app_common_button_text_i_know));
                }

                @Override
                public void onResponse(List<GetAreaListResult> response, int id) {
                    closeProgressDialog();
                    if (response != null) {
                        cityList = (List<City>) ParseAreaUtil.parse(response, ParseAreaUtil.TYPE.CITY, selectedProvince.code);
                        DataSupport.deleteAll(City.class, "provinceCode=" + selectedProvince.code);
                        DataSupport.saveAll(cityList);
                        queryCities();
                    } else {
                        DialogUtil.showAlertDialog(getActivity(), "", getString(R.string.app_common_error_msg), getString(R.string.app_common_button_text_i_know));
                    }
                }
            }, getActivity());
        }
    }

    private void queryCounties() {
        tvTitle.setText(selectedCity.cityName);
        btnBack.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityCode=?", String.valueOf(selectedCity.code)).find(County.class);
        if (countyList != null && !countyList.isEmpty()) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.countyName);
            }
            adapter.notifyDataSetChanged();
            rvArea.scrollToPosition(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            showProgressDialog();
            long provinceCode = selectedProvince.code;
            long cityCode = selectedCity.code;
            WeatherService.getInstance().getCountyList(provinceCode, cityCode, new Callback<List<GetAreaListResult>>() {
                @Override
                public List<GetAreaListResult> parseNetworkResponse(Response response, int id) throws Exception {
                    String json = response.body().string();
                    return JSONUtil.getObjectFromJson(json, new TypeToken<List<GetAreaListResult>>() {
                    }.getType());
                }

                @Override
                public void onError(Call call, Response response, Exception exception, int id) {
                    closeProgressDialog();
                    Log.e(TAG, "onError: " + exception.toString());
                    DialogUtil.showAlertDialog(getActivity(), "", getString(R.string.app_common_error_msg), getString(R.string.app_common_button_text_i_know));
                }

                @Override
                public void onResponse(List<GetAreaListResult> response, int id) {
                    closeProgressDialog();
                    if (response != null) {
                        countyList = (List<County>) ParseAreaUtil.parse(response, ParseAreaUtil.TYPE.COUNTY, selectedCity.code);
                        DataSupport.deleteAll(County.class, "cityCode=" + selectedCity.code);
                        DataSupport.saveAll(countyList);
                        queryCounties();
                    } else {
                        DialogUtil.showAlertDialog(getActivity(), "", getString(R.string.app_common_error_msg), getString(R.string.app_common_button_text_i_know));
                    }
                }
            }, getActivity());
        }
    }

    //查询省份
    public void queryProvinces() {
        tvTitle.setText("中国");
        btnBack.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList != null && !provinceList.isEmpty()) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.provinceName);
            }
            adapter.notifyDataSetChanged();
            rvArea.scrollToPosition(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            showProgressDialog();
            WeatherService.getInstance().getProvinceList(new Callback<List<GetAreaListResult>>() {
                @Override
                public List<GetAreaListResult> parseNetworkResponse(Response response, int id) throws Exception {
                    String json = response.body().string();
                    return JSONUtil.getObjectFromJson(json, new TypeToken<List<GetAreaListResult>>() {
                    }.getType());
                }

                @Override
                public void onError(Call call, Response response, Exception exception, int id) {
                    closeProgressDialog();
                    Log.e(TAG, "onError: " + exception.toString());
                    DialogUtil.showAlertDialog(getActivity(), "", getString(R.string.app_common_error_msg), getString(R.string.app_common_button_text_i_know));
                }

                @Override
                public void onResponse(List<GetAreaListResult> response, int id) {
                    closeProgressDialog();
                    if (response != null) {
                        provinceList = (List<Province>) ParseAreaUtil.parse(response, ParseAreaUtil.TYPE.PROVINCE, -1);
                        DataSupport.deleteAll(Province.class);
                        DataSupport.saveAll(provinceList);
                        queryProvinces();
                    } else {
                        DialogUtil.showAlertDialog(getActivity(), "", getString(R.string.app_common_error_msg), getString(R.string.app_common_button_text_i_know));
                    }
                }
            }, getActivity());
        }
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    private void showLocationSuccessDialog(final String cityName) {
        final CustomizableConfirmDialog confirmDialog = new CustomizableConfirmDialog(getActivity());
        confirmDialog.setTitle(getString(R.string.app_location_success_title));
        confirmDialog.setMessage(getString(R.string.app_location_success_message, cityName));
        confirmDialog.setPositiveButton(getString(R.string.app_common_button_text_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                WeatherService.getInstance().getCityCode(cityName, new Callback<GetCityInfoResult>() {
                    @Override
                    public GetCityInfoResult parseNetworkResponse(Response response, int id) throws Exception {
                        String json = response.body().string();
                        return JSONUtil.getObjectFromJson(json, GetCityInfoResult.class);
                    }

                    @Override
                    public void onError(Call call, Response response, Exception exception, int id) {
                        Log.e(TAG, "onError: " + exception.toString());
                        closeProgressDialog();
                        DialogUtil.showAlertDialog(getActivity(), "", getString(R.string.app_common_error_msg), getString(R.string.app_common_button_text_i_know));
                    }

                    @Override
                    public void onResponse(GetCityInfoResult response, int id) {
                        if (response != null && response.HeWeather5 != null && !response.HeWeather5.isEmpty()) {
                            GetCityInfoResult.HeWeather5Bean weather = response.HeWeather5.get(0);
                            if (TextUtils.equals(weather.status, "ok")) {
                                Activity activity = getActivity();
                                Intent data = new Intent();
                                data.putExtra(AppConfig.PARAM_CITY_ID, weather.basic.id);
                                data.putExtra(AppConfig.PARAM_CITY_NAME, weather.basic.city);
                                activity.setResult(Activity.RESULT_OK, data);
                                activity.finish();
                            } else {
                                ToastUtil.showShort(getActivity(), weather.status);
                            }
                        } else {
                            showLocationErrorDialog();
                        }
                    }
                }, getActivity());
            }
        });
        confirmDialog.setNegativeButton(getString(R.string.app_common_button_text_cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        queryProvinces();
                    }
                });
            }
        });
        confirmDialog.show();
    }

    private void showLocationErrorDialog() {
        final CustomizableAlertDialog alertDialog = new CustomizableAlertDialog(getActivity());
        alertDialog.setTitle(getString(R.string.app_location_error_title));
        alertDialog.setMessage(getString(R.string.app_location_error_message));
        alertDialog.setButton(getString(R.string.app_common_button_text_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                queryProvinces();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        int locType = bdLocation.getLocType();
        if (locType == BDLocation.TypeServerError
                || locType == BDLocation.TypeNetWorkException
                || locType == BDLocation.TypeCriteriaException) {
            Log.d(TAG, "onReceiveLocation: error = " + locType);
            showLocationErrorDialog();
        } else {
            String city = bdLocation.getAddress().city;
            showLocationSuccessDialog(city);
        }
    }

    @Override
    public void onConnectHotSpotMessage(String s, int i) {

    }
}