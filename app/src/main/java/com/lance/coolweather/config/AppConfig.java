package com.lance.coolweather.config;

/**
 * Created by lindan on 17-2-23.
 * App的配置参数
 */
public class AppConfig {
    /**
     * 刷新天气信息，必须传入PARAM_WEATHER_ID
     */
    public static final String ACTION_REFRESH_WEATHER = "action_refresh_weather";

    /**
     * 城市ID参数名称
     */
    public static final String PARAM_CITY_ID = "city_id";

    /**
     * 城市名称参数名称
     */
    public static final String PARAM_CITY_NAME = "city_name";

    /**
     * 刷新每日一图
     */
    public static final String ACTION_REFRESH_IMAGE = "action_refresh_image";

    /**
     * 保存在SharedPreference的城市ID列表Key
     */
    public static final String SHARE_KEY_CITY_ID_LIST = "city_list";

    /**
     * 图片的URL
     */
    public static final String SHARE_IMAGE_URL = "image_url";

    /**
     * 天气信息
     */
    public static final String SHARE_WEATHER = "weather_";

    /**
     * 图片的日期
     */
    public static final String SHARE_IMAGE_DATE = "image_date";

    /**
     * 天气状况图标，#为要替换的天气code
     */
    public static final String WEATHER_ICON_URL = "http://files.heweather.com/cond_icon/#.png";
}
