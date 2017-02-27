package com.lance.coolweather.config;

import android.content.Context;

import com.lance.common.util.SPUtil;

/**
 * Created by lindan on 17-2-27.
 */
public class AppUtil {
    public static boolean isAutoUpdateEnabled(Context context) {
        return (boolean) SPUtil.get(context, AppConfig.SHARE_AUTO_UPDATE, true);
    }

    public static int getAutoUpdateInterval(Context context) {
        return (int) SPUtil.get(context, AppConfig.SHARE_AUTO_UPDATE_INTERVAL, 4);
    }

    public static boolean isNotificationDisplayEnabled(Context context) {
        return (boolean) SPUtil.get(context, AppConfig.SHARE_SHOW_NOTIFICATION, false);
    }

    public static void setAutoUpdateEnabled(Context context, boolean autoUpdate) {
        SPUtil.put(context, AppConfig.SHARE_AUTO_UPDATE, autoUpdate);
    }

    public static void setAutoUpdateInterval(Context context, int hour) {
        SPUtil.put(context, AppConfig.SHARE_AUTO_UPDATE, true);
        SPUtil.put(context, AppConfig.SHARE_AUTO_UPDATE_INTERVAL, hour);
    }

    public static void setNotificationDisplayEnabled(Context context, boolean notificationDisplay) {
        SPUtil.put(context, AppConfig.SHARE_SHOW_NOTIFICATION, notificationDisplay);
    }
}
