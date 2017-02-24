package com.lance.coolweather.api.result;

/**
 * Created by lindan on 17-2-22.
 */

public class BasicResult {
    public String city;//城市
    public String cnty;//国家
    public String id;//城市ID
    public String lat;//纬度
    public String lon;//经度
    public Update update;//更新时间

    public static class Update {
        public String loc;//当地时间
        public String utc;//UTC时间
    }
}
