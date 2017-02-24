package com.lance.coolweather.bean;

import com.lance.common.util.JSONUtil;

/**
 * Created by lindan on 17-2-23.
 */
public class CityBean {
    public String cityId;
    public String cityName;

    public CityBean() {
    }

    public CityBean(String cityId, String cityName) {
        this.cityId = cityId;
        this.cityName = cityName;
    }

    @Override
    public String toString() {
        return JSONUtil.getJsonFromObject(this);
    }
}
