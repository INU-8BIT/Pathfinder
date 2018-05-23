package com.inu8bit.pathfinder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */

public class CityCode {
    private static final Map<String, Integer> cityCode;
    static {
        Map<String, Integer> _cityCode = new HashMap<>();
        _cityCode.put("세종특별시", 12);
        _cityCode.put("대구광역시", 22);
        _cityCode.put("인천광역시", 23);
        _cityCode.put("광주광역시", 24);
        _cityCode.put("대전광역시", 25);
        _cityCode.put("울산광역시", 26);
        _cityCode.put("제주도", 39);
        _cityCode.put("춘천", 32010);
        _cityCode.put("원주", 32020);
        _cityCode.put("청주", 33010);
        _cityCode.put("천안", 34010);
        _cityCode.put("아산", 34040);
        _cityCode.put("전주", 35010);
        _cityCode.put("군산", 35020);
        _cityCode.put("목포", 36010);
        _cityCode.put("여수", 36020);
        _cityCode.put("순천", 36030);
        _cityCode.put("광영", 36060);
        _cityCode.put("포함", 37010);
        _cityCode.put("김천", 37030);
        _cityCode.put("구미", 37050);
        _cityCode.put("경산", 37100);
        _cityCode.put("창원", 38010);
        _cityCode.put("진주", 38030);
        _cityCode.put("통영", 38050);
        _cityCode.put("김해", 38070);
        _cityCode.put("밀양", 38080);
        _cityCode.put("거제", 38090);
        _cityCode.put("양산", 38100);
        cityCode = Collections.unmodifiableMap(_cityCode);
    }
    public static int getCityCode(String addr){
        for (Map.Entry<String, Integer> entry : cityCode.entrySet()){
            // Address contains key, return its value
            if(addr.contains(entry.getKey())){
                return entry.getValue();
            }
        }
        return -1;
    }
}
