package com.lance.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.lance.common.util.DateUtil;
import com.lance.common.util.JSONUtil;
import com.lance.common.util.SPUtil;
import com.lance.coolweather.api.WeatherService;
import com.lance.coolweather.api.result.WeatherResult;
import com.lance.coolweather.config.AppConfig;
import com.lance.network.okhttputil.callback.Callback;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    private static final String TAG = "AutoUpdateService";

    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateImage();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 4 * 3600000;//4小时更新一次
        //int interval = 5000;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        List<String> cityIdList = new ArrayList<>();
        Map<String, ?> values = SPUtil.getAll(this);
        if (values != null && !values.isEmpty()) {
            Iterator<String> keyIterator = values.keySet().iterator();
            String key;
            while (keyIterator.hasNext()) {
                key = keyIterator.next();
                if (key.startsWith(AppConfig.SHARE_WEATHER)) {
                    cityIdList.add(key.substring(key.indexOf("_") + 1));
                }
            }
        }
        if (!cityIdList.isEmpty()) {
            for (final String cityId : cityIdList) {
                WeatherService.getInstance().getWeatherInfo(cityId, new Callback<WeatherResult>() {
                    @Override
                    public WeatherResult parseNetworkResponse(Response response, int id) throws Exception {
                        String json = response.body().string();
                        return JSONUtil.getObjectFromJson(json, WeatherResult.class);
                    }

                    @Override
                    public void onError(Call call, Response response, Exception exception, int id) {
                        Log.e(TAG, "onError: " + exception.toString());
                    }

                    @Override
                    public void onResponse(WeatherResult response, int id) {
                        if (response != null && response.HeWeather5 != null && response.HeWeather5.size() == 1) {
                            if ("ok".equalsIgnoreCase(response.HeWeather5.get(0).status)) {
                                String json = JSONUtil.getJsonFromObject(response);
                                SPUtil.put(AutoUpdateService.this, AppConfig.SHARE_WEATHER + cityId, json);
                                //发送广播通知界面
                                Intent intent = new Intent(AppConfig.ACTION_REFRESH_WEATHER);
                                intent.putExtra(AppConfig.SHARE_WEATHER + cityId, json);
                                LocalBroadcastManager.getInstance(AutoUpdateService.this).sendBroadcast(intent);
                            }
                        }
                    }
                }, this);
            }
        }
    }

    private void updateImage() {
        String imageDate = (String) SPUtil.get(this, AppConfig.SHARE_IMAGE_DATE, "");
        String imageUrl = (String) SPUtil.get(this, AppConfig.SHARE_IMAGE_URL, "");
        if (TextUtils.isEmpty(imageUrl)) {
            requestImage();
        } else {
            String nowDate = DateUtil.getDate(new Date(), "yyyyMMdd");
            if (!TextUtils.equals(imageDate, nowDate)) {
                requestImage();
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
            }

            @Override
            public void onResponse(String response, int id) {
                if (!TextUtils.isEmpty(response)) {
                    SPUtil.put(AutoUpdateService.this, AppConfig.SHARE_IMAGE_URL, response);
                    SPUtil.put(AutoUpdateService.this, AppConfig.SHARE_IMAGE_DATE, DateUtil.getDate(new Date(), "yyyyMMdd"));
                    //发送广播通知界面
                    Intent intent = new Intent(AppConfig.ACTION_REFRESH_IMAGE);
                    intent.putExtra(AppConfig.SHARE_IMAGE_URL, response);
                    LocalBroadcastManager.getInstance(AutoUpdateService.this).sendBroadcast(intent);
                }
            }
        }, this);
    }
}
