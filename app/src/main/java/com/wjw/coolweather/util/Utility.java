package com.wjw.coolweather.util;

import android.text.TextUtils;

import com.wjw.coolweather.db.CoolWeatherDB;
import com.wjw.coolweather.model.City;
import com.wjw.coolweather.model.County;
import com.wjw.coolweather.model.Province;

/**
 * Created by wangjianwei on 2016/6/1.
 */
public class Utility {
    /**
     * 解析和处理服务器返回的升级数据
     * 10|北京,02|河北,03|河南
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response){
        if(!TextUtils.isEmpty(response)){
            String[] allProvince = response.split(";");
            if(allProvince!=null && allProvince.length>0){
                for(String p : allProvince){
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceName( array[1]);
                    province.setProvinceCode(array[0]);
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的市级数据
     * 0201|石家庄,0202|秦皇岛,0203|唐山,0204|保定
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if(allCities!=null && allCities.length>0){
                for (String p : allCities){
                    String[] array = p.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的县级数据
     * 020101|桥西区,020102|长安区,020103|裕华区,020104|新华区
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if(allCounties!=null && allCounties.length>0){
                for(String p: allCounties){
                    String[] array = p.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }
}
