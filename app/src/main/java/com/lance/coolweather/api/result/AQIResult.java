package com.lance.coolweather.api.result;

/**
 * Created by lindan on 17-2-22.
 */

public class AQIResult {
    public City city;

    public static class City {
        public String aqi;//AQI
        public String co;//CO
        public String no2;//NO2
        public String o3;//O3
        public String pm10;//PM1010
        public String pm25;//PM2.5
        public String qlty;//空气质量
        public String so2;//SO2
    }
}
