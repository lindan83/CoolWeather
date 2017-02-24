package com.lance.coolweather.api.result;

import java.util.List;

/**
 * Created by lindan on 17-2-22.
 * 天气预报集合接口
 * https://free-api.heweather.com/v5/weather
 */

public class WeatherResult {
    public List<HeWeather5Bean> HeWeather5;

    public static class HeWeather5Bean {
        public AQIResult aqi;
        public BasicResult basic;
        public NowResult now;
        public String status;
        public SuggestionResult suggestion;
        public List<DailyForecastResult> daily_forecast;
        public List<HourlyForecastResult> hourly_forecast;
    }
}
