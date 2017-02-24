package com.lance.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lindan on 17-2-21.
 * 省份实体，对应Province表
 */

public class Province extends DataSupport implements Area {
    public String provinceName;
    public long code;
}
