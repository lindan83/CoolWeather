package com.lance.coolweather.db;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lindan on 17-2-24.
 */

public class DBAccessHelper {
    /**
     * 根据市或区镇的ID查询
     *
     * @param cityIdList ID列表
     */
    public static List<County> findCountyList(List<String> cityIdList) {
        List<County> countyList = new ArrayList<>();
        if (cityIdList != null && !cityIdList.isEmpty()) {
            StringBuilder where = new StringBuilder();
            where.append("weatherId in (");
            for (String cityId : cityIdList) {
                where.append("'").append(cityId).append("'").append(",");
            }
            where.setCharAt(where.length() - 1, ')');
            List<County> temp = DataSupport.where(where.toString()).find(County.class);
            if (temp != null && !temp.isEmpty()) {
                countyList.addAll(temp);
            }
        }
        return countyList;
    }

    public static County findCounty(String cityId) {
        return DataSupport.where("weatherId=?", cityId).findFirst(County.class);
    }

    public static boolean saveCounty(County county) {
        return county.save();
    }

    public static void initDB() {

    }
}
