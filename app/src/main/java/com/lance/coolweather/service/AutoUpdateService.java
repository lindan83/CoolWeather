package com.lance.coolweather.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.lance.common.util.DateUtil;
import com.lance.common.util.JSONUtil;
import com.lance.common.util.SPUtil;
import com.lance.coolweather.R;
import com.lance.coolweather.activity.WeatherActivity;
import com.lance.coolweather.api.WeatherService;
import com.lance.coolweather.api.result.WeatherResult;
import com.lance.coolweather.config.AppConfig;
import com.lance.coolweather.config.AppUtil;
import com.lance.coolweather.db.County;
import com.lance.coolweather.db.DBAccessHelper;
import com.lance.coolweather.util.ParseCityIdUtil;
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
    private boolean autoUpdate;
    private int autoUpdateInterval;

    public AutoUpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        autoUpdate = AppUtil.isAutoUpdateEnabled(this);
        autoUpdateInterval = AppUtil.getAutoUpdateInterval(this);
        boolean showNotification = AppUtil.isNotificationDisplayEnabled(this);
        if (showNotification) {
            County county = getCountyInfo();
            if (county != null) {
                String weatherString = (String) SPUtil.get(this, AppConfig.SHARE_WEATHER + county.weatherId, "");
                WeatherResult weather = JSONUtil.getObjectFromJson(weatherString, WeatherResult.class);
                WeatherResult.HeWeather5Bean weatherBean;
                String cityName = null, degree = null, weatherInfo = null;
                if (weather != null && weather.HeWeather5 != null && !weather.HeWeather5.isEmpty()) {
                    weatherBean = weather.HeWeather5.get(0);
                    cityName = weatherBean.basic.city;
                    degree = weatherBean.now.tmp;
                    weatherInfo = weatherBean.now.cond.txt;
                }
                String txt = "";
                if (!TextUtils.isEmpty(cityName) && !TextUtils.isEmpty(degree) && !TextUtils.isEmpty(weatherInfo)) {
                    txt = String.format("%s 气温%s %s", cityName, degree + "℃", weatherInfo);
                }
                Intent i = new Intent(this, WeatherActivity.class);
                PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
                Notification notification = new NotificationCompat.Builder(this)
                        .setContentTitle(!TextUtils.isEmpty(cityName) ? cityName : "")
                        .setContentText(txt)
                        .setSmallIcon(R.mipmap.ic_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_icon))
                        .setAutoCancel(false)
                        .setContentIntent(pi).build();
                startForeground(1, notification);
            }
        }
    }

    private County getCountyInfo() {
        String cityIdString = (String) SPUtil.get(this, AppConfig.SHARE_KEY_CITY_ID_LIST, "");
        String cityId = null;
        if (!TextUtils.isEmpty(cityIdString)) {
            List<String> cityIds = new ArrayList<>();
            cityIds.addAll(ParseCityIdUtil.parse(cityIdString));
            cityId = cityIds.get(0);
        }
        if (!TextUtils.isEmpty(cityId)) {
            return DBAccessHelper.findCounty(cityId);
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (autoUpdate) {
            updateWeather();
            updateImage();
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            int interval = autoUpdateInterval * 3600000;//指定N小时更新一次
            Intent i = new Intent(this, AutoUpdateService.class);
            PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
            alarmManager.cancel(pi);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, pi);
            Log.d(TAG, "onStartCommand: interval = " + interval);
        } else {
            stopSelf();
        }
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

    @Override
    public void onDestroy() {
        stopAlarmManager();
        super.onDestroy();
    }

    private void stopAlarmManager() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        alarmManager.cancel(pi);
    }
}
