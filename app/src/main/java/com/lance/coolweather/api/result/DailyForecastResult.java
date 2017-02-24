package com.lance.coolweather.api.result;

/**
 * Created by lindan on 17-2-22.
 */

public class DailyForecastResult {
    public Astro astro;//天文指数
    public Cond cond;//天气状况
    public String date;//日期
    public String hum;//相对湿度
    public String pcpn;//降水量
    public String pop;//降水概率
    public String pres;//气压
    public Tmp tmp;//温度
    public String uv;//紫外线指数
    public String vis;//能见度
    public WindResult wind;//风力情况

    public static class Astro {
        public String mr;//月升时间
        public String ms;//月落时间
        public String sr;//日出时间
        public String ss;//日落时间
    }

    public static class Cond {
        public String code_d;//白天天气状况代码
        public String code_n;//夜间天气状况代码
        public String txt_d;//白天天气状况描述
        public String txt_n;//夜间天气状况描述
    }

    public static class Tmp {
        public String max;//最高温度
        public String min;//最高温度
    }
}
