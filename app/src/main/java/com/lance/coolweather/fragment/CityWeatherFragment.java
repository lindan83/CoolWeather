package com.lance.coolweather.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lance.common.glideimageloader.GlideImageLoader;
import com.lance.common.util.JSONUtil;
import com.lance.common.util.SPUtil;
import com.lance.common.util.ToastUtil;
import com.lance.common.widget.dialog.DialogUtil;
import com.lance.coolweather.R;
import com.lance.coolweather.activity.ChooseAreaActivity;
import com.lance.coolweather.api.WeatherService;
import com.lance.coolweather.api.result.DailyForecastResult;
import com.lance.coolweather.api.result.HourlyForecastResult;
import com.lance.coolweather.api.result.WeatherResult;
import com.lance.coolweather.config.AppConfig;
import com.lance.coolweather.service.AutoUpdateService;
import com.lance.network.okhttputil.callback.Callback;

import okhttp3.Call;
import okhttp3.Response;

import static android.view.View.GONE;

/**
 * 城市天气
 */
public class CityWeatherFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "CityWeatherFragment";

    private ScrollView scrollWeather;
    private TextView tvCity, tvDegree, tvWeatherInfo, tvUpdateTime, tvAQI, tvPM25, tvComfort, tvCarWash, tvSport;
    private ImageView ivWeatherIcon;
    private LinearLayout llHourlyForecast, llDailyForecast;
    private SwipeRefreshLayout swipeRefresh;

    private String cityId;

    private BroadcastReceiver refreshWeatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.equals(AppConfig.ACTION_REFRESH_WEATHER, action)) {
                String weatherJson = intent.getStringExtra(AppConfig.SHARE_WEATHER + cityId);
                Log.d(TAG, "onReceive: weatherJson = " + weatherJson);
                if (!TextUtils.isEmpty(weatherJson)) {
                    WeatherResult weatherResult = JSONUtil.getObjectFromJson(weatherJson, WeatherResult.class);
                    if (weatherResult != null) {
                        showWeatherInfo(weatherResult);
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            cityId = bundle.getString("city_id");
            if (TextUtils.isEmpty(cityId)) {
                throw new IllegalArgumentException("city_id must not be null");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_city_weather, container, false);
        initViews(view);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConfig.ACTION_REFRESH_WEATHER);
        //intentFilter.addAction(AppConfig.ACTION_REFRESH_IMAGE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshWeatherReceiver, intentFilter);
        return view;
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(refreshWeatherReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String weatherString = (String) SPUtil.get(getActivity(), AppConfig.SHARE_WEATHER + cityId, "");
        if (!TextUtils.isEmpty(weatherString)) {
            //有缓存时直接解析
            WeatherResult weather = JSONUtil.getObjectFromJson(weatherString, WeatherResult.class);
            cityId = weather.HeWeather5.get(0).basic.id;
            showWeatherInfo(weather);
        } else {
            //无缓存时查询接口
            if (TextUtils.isEmpty(cityId)) {
                startActivity(new Intent(getActivity(), ChooseAreaActivity.class));
                return;
            }
            scrollWeather.setVisibility(View.INVISIBLE);
            requestWeather(cityId);
        }
    }

    private void initViews(View view) {
        scrollWeather = (ScrollView) view.findViewById(R.id.scroll_weather);
        tvCity = (TextView) view.findViewById(R.id.tv_city);
        ivWeatherIcon = (ImageView) view.findViewById(R.id.iv_weather_icon);
        ivWeatherIcon.setVisibility(View.GONE);
        tvDegree = (TextView) view.findViewById(R.id.tv_degree);
        tvWeatherInfo = (TextView) view.findViewById(R.id.tv_weather_info);
        tvUpdateTime = (TextView) view.findViewById(R.id.tv_update_time);
        tvAQI = (TextView) view.findViewById(R.id.tv_aqi);
        tvPM25 = (TextView) view.findViewById(R.id.tv_pm);
        tvComfort = (TextView) view.findViewById(R.id.tv_comfort);
        tvCarWash = (TextView) view.findViewById(R.id.tv_car_wash);
        tvSport = (TextView) view.findViewById(R.id.tv_sport);
        llDailyForecast = (LinearLayout) view.findViewById(R.id.ll_daily_forecast);
        llHourlyForecast = (LinearLayout) view.findViewById(R.id.ll_hourly_forecast);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(this);
    }

    /**
     * 创建并返回CityWeatherFragment实例
     *
     * @param cityId 城市ID
     */
    public static CityWeatherFragment newInstance(String cityId) {
        CityWeatherFragment fragment = new CityWeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putString("city_id", cityId);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * 修改城市，刷新天气
     */
    public void refreshCityWeather() {
        swipeRefresh.setRefreshing(true);
        requestWeather(cityId);

        Intent intent = new Intent(getActivity(), AutoUpdateService.class);
        getActivity().startService(intent);
    }

    //向接口请求天气信息
    private void requestWeather(String weatherId) {
        showProgressDialog();
        WeatherService.getInstance().getWeatherInfo(weatherId, new Callback<WeatherResult>() {
            @Override
            public WeatherResult parseNetworkResponse(Response response, int id) throws Exception {
                String json = response.body().string();
                return JSONUtil.getObjectFromJson(json, WeatherResult.class);
            }

            @Override
            public void onError(Call call, Response response, Exception exception, int id) {
                closeProgressDialog();
                swipeRefresh.setRefreshing(false);
                Log.d(TAG, "onError: " + exception.toString());
                DialogUtil.showAlertDialog(getActivity(), "", getString(R.string.app_common_error_msg), getString(R.string.app_common_button_text_i_know));
            }

            @Override
            public void onResponse(WeatherResult response, int id) {
                closeProgressDialog();
                swipeRefresh.setRefreshing(false);
                if (response != null && response.HeWeather5 != null && response.HeWeather5.size() == 1) {
                    if ("ok".equalsIgnoreCase(response.HeWeather5.get(0).status)) {
                        SPUtil.put(getActivity(), AppConfig.SHARE_WEATHER + cityId, JSONUtil.getJsonFromObject(response));
                        showWeatherInfo(response);
                    } else {
                        ToastUtil.showShort(getActivity(), getString(R.string.weather_info_fail));
                    }
                } else {
                    DialogUtil.showAlertDialog(getActivity(), "", getString(R.string.app_common_error_msg), getString(R.string.app_common_button_text_i_know));
                }
            }
        }, this);
    }

    private void showWeatherInfo(WeatherResult weather) {
        WeatherResult.HeWeather5Bean weatherBean = weather.HeWeather5.get(0);
        String cityName = weatherBean.basic.city;
        String updateTime = weatherBean.basic.update.loc;
        String degree = weatherBean.now.tmp + "℃";
        String weatherInfo = weatherBean.now.cond.txt;
        tvCity.setText(cityName);
        tvDegree.setText(degree);
        tvWeatherInfo.setText(weatherInfo);
        tvUpdateTime.setText(getString(R.string.weather_update_time) + updateTime);
        GlideImageLoader.loadStringResource(ivWeatherIcon, AppConfig.WEATHER_ICON_URL.replaceAll("#", weatherBean.now.cond.code));
        ivWeatherIcon.setVisibility(View.VISIBLE);

        if (weatherBean.aqi != null) {
            tvAQI.setText(weatherBean.aqi.city.aqi);
            tvPM25.setText(weatherBean.aqi.city.pm25);
        }

        if (weatherBean.suggestion != null) {
            String comfort = getString(R.string.weather_comfort_label) + weatherBean.suggestion.comf.txt;
            String carWash = getString(R.string.weather_car_wash_label) + weatherBean.suggestion.cw.txt;
            String sport = getString(R.string.weather_sport_label) + weatherBean.suggestion.sport.txt;

            tvComfort.setText(comfort);
            tvCarWash.setText(carWash);
            tvSport.setText(sport);
        }

        if (weatherBean.daily_forecast != null && !weatherBean.daily_forecast.isEmpty()) {
            llDailyForecast.setVisibility(View.VISIBLE);
            llDailyForecast.removeAllViews();
            for (DailyForecastResult forecast : weatherBean.daily_forecast) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item, llDailyForecast, false);
                TextView tvDate = (TextView) view.findViewById(R.id.tv_date);
                TextView tvInfo = (TextView) view.findViewById(R.id.tv_info);
                TextView tvMax = (TextView) view.findViewById(R.id.tv_max);
                TextView tvMin = (TextView) view.findViewById(R.id.tv_min);
                tvDate.setText(forecast.date);
                tvInfo.setText(forecast.cond.txt_d);
                tvMax.setText(forecast.tmp.max);
                tvMin.setText(forecast.tmp.min);
                llDailyForecast.addView(view);
            }
        } else {
            llDailyForecast.setVisibility(GONE);
        }

        if (weatherBean.hourly_forecast != null && !weatherBean.hourly_forecast.isEmpty()) {
            llHourlyForecast.setVisibility(View.VISIBLE);
            llHourlyForecast.removeAllViews();
            for (HourlyForecastResult forecast : weatherBean.hourly_forecast) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item, llHourlyForecast, false);
                TextView tvDate = (TextView) view.findViewById(R.id.tv_date);
                TextView tvInfo = (TextView) view.findViewById(R.id.tv_info);
                TextView tvMax = (TextView) view.findViewById(R.id.tv_max);
                tvDate.setText(forecast.date);
                tvInfo.setText(forecast.cond.txt);
                tvMax.setText(forecast.tmp);
                llHourlyForecast.addView(view);
            }
        } else {
            llHourlyForecast.setVisibility(GONE);
        }

        scrollWeather.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        requestWeather(cityId);
    }


    public String getCityId() {
        return cityId;
    }
}
