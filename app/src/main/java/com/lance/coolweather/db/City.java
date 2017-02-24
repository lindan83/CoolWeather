package com.lance.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lindan on 17-2-21.
 * 城市实体，对应City表
 */

public class City extends DataSupport implements Area {
    public String cityName;
    public long code;
    public long provinceCode;
}
