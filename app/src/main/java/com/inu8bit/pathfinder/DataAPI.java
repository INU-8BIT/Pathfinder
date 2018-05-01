package com.inu8bit.pathfinder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.inu8bit.pathfinder.BuildConfig.DataAPIKey;

/**
 * Class for datasets brought by Data.or.kr
 * Usage:
 *      1. Get the list of Nearby bus stop
 */
public class DataAPI extends APIWrapper {
    DataAPI(){
        serviceKey = DataAPIKey;
        url = new StringBuilder("http://openapi.tago.go.kr/openapi/service/BusSttnInfoInqireService/getCrdntPrxmtSttnList");
    }

    public List<String> getNearbyBusStop(double lat, double lon) throws InterruptedException, ExecutionException, JSONException{
        params.put("ServiceKey", this.serviceKey);
        params.put("gpsLati", String.valueOf(lat));
        params.put("gpsLong", String.valueOf(lon));
        params.put("_type", "json");

        this.method = "GET";
        return this.getLists(this.execute().get());
    }

    private List<String> getLists(JSONObject _obj) throws JSONException {
        // TODO: make these following steps as Class or structural object.
        JSONArray busStopList = _obj.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < busStopList.length() && i < 5; i++) {
            list.add(busStopList.getJSONObject(i).getString("nodenm") + " " + busStopList.getJSONObject(i).getInt("nodeno"));
        }
        return list;
    }
}
