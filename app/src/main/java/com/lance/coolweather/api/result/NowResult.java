package com.lance.coolweather.api.result;

/**
 * Created by lindan on 17-2-22.
 */

public class NowResult {
    public CondResult cond;//天气状况
    public String fl;//体感温度
    public String hum;//相对湿度%
    public String pcpn;//降水量mm
    public String pres;//气压
    public String tmp;//温度
    public String vis;//能见度km
    public WindResult wind;//风力风向
}
