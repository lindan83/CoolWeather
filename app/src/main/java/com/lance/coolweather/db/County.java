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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        County county = (County) o;

        return weatherId != null ? weatherId.equals(county.weatherId) : county.weatherId == null;

    }

    @Override
    public int hashCode() {
        return weatherId != null ? weatherId.hashCode() : 0;
    }
}
