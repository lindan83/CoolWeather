package com.lance.coolweather.api.result;

/**
 * Created by lindan on 17-2-22.
 */

public class HourlyForecastResult {
    public CondResult cond;//天气状况
    public String date;//日期
    public String hum;//相对湿度
    public String pop;//降水概率
    public String pres;//气压
    public String tmp;//温度
    public WindResult wind;//风力
}
