package com.lance.coolweather.api.result;

import java.util.List;

/**
 * Created by lindan on 17-2-27.
 */
public class GetCityInfoResult {

    public List<HeWeather5Bean> HeWeather5;

    public static class HeWeather5Bean {
        public BasicResult basic;
        public String status;
    }
}
