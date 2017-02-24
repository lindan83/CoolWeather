package com.lance.coolweather.api.result;

/**
 * Created by lindan on 17-2-22.
 */

public class SuggestionResult {
    public Air air;//空气质量
    public Comf comf;//舒适度指数
    public Cw cw;//洗车指数
    public Drsg drsg;//穿衣指数
    public Flu flu;//感冒指数
    public Sport sport;//运动指数
    public Trav trav;//旅游指数
    public Uv uv;//紫外线指数

    public static class Air {
        public String brf;//空气质量等级
        public String txt;//空气质量描述
    }

    public static class Comf {
        public String brf;//舒适度指数等级
        public String txt;//舒适度指数描述
    }

    public static class Cw {
        public String brf;//洗车指数等级
        public String txt;//洗车指数详细描述
    }

    public static class Drsg {
        public String brf;//穿衣指数等级
        public String txt;//穿衣指数描述
    }

    public static class Flu {
        public String brf;//感冒指数等级
        public String txt;//感冒指数描述
    }

    public static class Sport{
        public String brf;//运动指数等级
        public String txt;//运动指数描述
    }

    public static class Trav {
        public String brf;//旅游指数等级
        public String txt;//旅游指数描述
    }

    public static class Uv {
        public String brf;//紫外线指数等级
        public String txt;//紫外线指数描述
    }
}
