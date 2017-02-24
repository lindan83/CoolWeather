package com.lance.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lindan on 17-2-21.
 */

public class County extends DataSupport implements Area {
    public long code;
    public String countyName;
    public String weatherId;
    public long cityCode;
}
