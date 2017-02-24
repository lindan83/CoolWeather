package com.lance.coolweather.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lindan on 17-2-23.
 * 城市ID字符串解析工具
 */
public class ParseCityIdUtil {
    public static List<String> parse(String cityIdString) {
        ArrayList<String> cityIds = new ArrayList<>();
        if (!TextUtils.isEmpty(cityIdString)) {
            try {
                String[] array = cityIdString.split(",");
                if (array != null && array.length > 0) {
                    cityIds.addAll(Arrays.asList(array));
                }
            } catch (Exception e) {

            }
        }
        return cityIds;
    }

    public static String format(List<String> cityIds) {
        if (cityIds == null || cityIds.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String cityId : cityIds) {
            result.append(cityId).append(",");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
}
