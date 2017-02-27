package com.lance.coolweather.api;

import com.lance.network.okhttputil.OkHttpUtils;
import com.lance.network.okhttputil.callback.Callback;

/**
 * Created by lindan on 17-2-21.
 * 天气服务访问接口
 */
public class WeatherService {
    /**
     * 获取省份列表
     */
    private static final String ADDRESS_BASE_URL = "http://guolin.tech/api/china";

    /**
     * 天气预报集合接口
     * 包括7-10天预报、实况天气、每小时天气、灾害预警、生活指数、空气质量
     */
    private static final String WEATHER_URL = "https://free-api.heweather.com/v5/weather";

    /**
     * 城市代码查询接口
     */
    private static final String CITY_URL = "https://free-api.heweather.com/v5/search";

    /**
     * 获取每日一图
     */
    private static final String IMAGE_URL = "http://guolin.tech/api/bing_pic";

    private static final String KEY = "c47a6adc1761464aa7c16bc0208c93f4";

    private WeatherService() {
    }

    private static WeatherService instance;

    /**
     * 获取天气服务访问接口的唯一实例
     *
     * @return WeatherService 实例
     */
    public static WeatherService getInstance() {
        if (instance == null) {
            synchronized (WeatherService.class) {
                if (instance == null) {
                    instance = new WeatherService();
                }
            }
        }
        return instance;
    }

    /**
     * 获取省份列表
     */
    public void getProvinceList(Callback callback, Object tag) {
        OkHttpUtils.get().url(ADDRESS_BASE_URL).tag(tag).build().execute(callback);
    }

    /**
     * 获取城市列表
     */
    public void getCityList(long provinceCode, Callback callback, Object tag) {
        OkHttpUtils.get().url(ADDRESS_BASE_URL + "/" + provinceCode).tag(tag).build().execute(callback);
    }

    /**
     * 获取区县列表
     */
    public void getCountyList(long provinceCode, long cityCode, Callback callback, Object tag) {
        OkHttpUtils.get().url(ADDRESS_BASE_URL + "/" + provinceCode + "/" + cityCode).tag(tag).build().execute(callback);
    }

    /**
     * 获取天气预报信息
     *
     * @param city 城市名称 中英文名称、ID和IP地址
     */
    public void getWeatherInfo(String city, Callback callback, Object tag) {
        OkHttpUtils.get().url(WEATHER_URL).addParams("key", KEY).addParams("city", city).tag(tag).build().execute(callback);
    }

    /**
     * 获取每日一图
     */
    public void getTodaysImage(Callback callback, Object tag) {
        OkHttpUtils.get().url(IMAGE_URL).tag(tag).build().execute(callback);
    }

    /**
     * 获取城市代码
     *
     * @param cityName 城市名称
     */
    public void getCityCode(String cityName, Callback callback, Object tag) {
        OkHttpUtils.get().url(CITY_URL).addParams("key", KEY).addParams("city", cityName).tag(tag).build().execute(callback);
    }
}
