package com.lance.coolweather.util;

import com.lance.coolweather.api.result.GetAreaListResult;
import com.lance.coolweather.db.Area;
import com.lance.coolweather.db.City;
import com.lance.coolweather.db.County;
import com.lance.coolweather.db.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lindan on 17-2-21.
 * 将接口的结果解析为本地实体列表工具类
 */
public class ParseAreaUtil {
    /**
     * 要解析的目的类型
     */
    public enum TYPE {
        /**
         * 省份
         */
        PROVINCE,
        /**
         * 城市
         */
        CITY,
        /**
         * 区县
         */
        COUNTY
    }

    /**
     * 将接口的结果解析为本地实体列表
     *
     * @param resultList 接口的结果集合
     * @param type       要解析的目标类型
     * @param code       对应解析类型的上一层实体Code
     */
    public static List<? extends Area> parse(List<GetAreaListResult> resultList, TYPE type, long code) {
        if (resultList != null && !resultList.isEmpty()) {
            switch (type) {
                case PROVINCE:
                    List<Province> provinces = new ArrayList<>(resultList.size());
                    for (GetAreaListResult result : resultList) {
                        Province province = new Province();
                        province.provinceName = result.name;
                        province.code = result.id;
                        provinces.add(province);
                    }
                    return provinces;
                case CITY:
                    List<City> cities = new ArrayList<>(resultList.size());
                    for (GetAreaListResult result : resultList) {
                        City city = new City();
                        city.cityName = result.name;
                        city.code = result.id;
                        city.provinceCode = code;
                        cities.add(city);
                    }
                    return cities;
                case COUNTY:
                    List<County> counties = new ArrayList<>(resultList.size());
                    for (GetAreaListResult result : resultList) {
                        County county = new County();
                        county.code = result.id;
                        county.countyName = result.name;
                        county.weatherId = result.weather_id;
                        county.cityCode = code;
                        counties.add(county);
                    }
                    return counties;
            }
        }
        return null;
    }
}
