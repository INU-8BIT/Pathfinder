package com.inu8bit.pathfinder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.inu8bit.pathfinder.BuildConfig.DataAPIKey;

/**
 * Class for datasets brought by Data.or.kr
 * Usage:
 *      1. Get the list of Nearby bus stop
 */
public class DataAPI extends APIWrapper {

    public static class BusStop {
        public String nodeNo;
        public String nodeNm;
        public String nodeId;
        public String cityCode;

        BusStop(String nodeId, String nodeNm, String nodeNo, String citycode){
            this.nodeId = nodeId;
            this.nodeNm = nodeNm;
            this.nodeNo = nodeNo;
            this.cityCode = citycode;
        }
    }

    private String GET_NEARBY_BUS_STOP = "BusSttnInfoInqireService/getCrdntPrxmtSttnList";
    private String GET_BUS_ARRIVAL_INFO = "ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";
    private String GET_BUS_SPECIFIC_ARRIVAL_INFO = "ArvlInfoInqireService/getSttnAcctoSpcifyRouteBusArvlPrearngeInfoList";

    DataAPI(){
        url = new StringBuilder("http://openapi.tago.go.kr/openapi/service/");
    }

    public Map<Integer, String[]> getNearbyBusStop(double lat, double lon) throws InterruptedException, ExecutionException, JSONException{
        url.append(GET_NEARBY_BUS_STOP);
        params.put("ServiceKey", DataAPIKey);
        params.put("gpsLati", String.valueOf(lat));
        params.put("gpsLong", String.valueOf(lon));
        params.put("_type", "json");

        this.method = "GET";
        return this.parseList(new JSONObject(this.send()));
    }

    private Map<Integer, String[]> parseList(JSONObject jsonObject) throws JSONException {
        JSONArray busStopList = jsonObject.getJSONObject("response")
                .getJSONObject("body")
                .getJSONObject("items").getJSONArray("item");

        Map<Integer, String[]> busList = new HashMap<>();
        for (int i = 0; i < busStopList.length() && i < 3; i++) {
            JSONObject elem = busStopList.getJSONObject(i);
            busList.put(elem.getInt("nodeno"), new String [] {
                    Integer.toString(elem.getInt("citycode")),
                    elem.getString("nodenm"),
                    elem.getString("nodeid")
            });
        }
        return busList;
    }
}
