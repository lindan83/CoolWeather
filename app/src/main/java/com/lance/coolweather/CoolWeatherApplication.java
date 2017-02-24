package com.lance.coolweather;

import android.app.Application;

import org.litepal.LitePal;

/**
 * Created by lindan on 17-2-21.
 */

public class CoolWeatherApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        LitePal.initialize(getApplicationContext());
    }
}
