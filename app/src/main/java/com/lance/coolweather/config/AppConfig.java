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
     * 刷新每日一图，必须传入SHARE_IMAGE_URL
     */
    public static final String ACTION_REFRESH_IMAGE = "action_refresh_image";

    /**
     * 城市ID参数名称
     */
    public static final String PARAM_CITY_ID = "city_id";

    /**
     * 城市名称参数名称
     */
    public static final String PARAM_CITY_NAME = "city_name";

    /**
     * 保存在SharedPreference的城市ID列表Key
     */
    public static final String SHARE_KEY_CITY_LIST = "city_list";

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
     * 是否自动更新天气信息
     */
    public static final String SHARE_AUTO_UPDATE = "auto_update";

    /**
     * 自动更新天气的间隔时间，单位小时
     */
    public static final String SHARE_AUTO_UPDATE_INTERVAL = "auto_update_interval";

    /**
     * 是否通知栏显示
     */
    public static final String SHARE_SHOW_NOTIFICATION = "show_notification";

    /**
     * 天气状况图标，#为要替换的天气code
     */
    public static final String WEATHER_ICON_URL = "http://files.heweather.com/cond_icon/#.png";
}
