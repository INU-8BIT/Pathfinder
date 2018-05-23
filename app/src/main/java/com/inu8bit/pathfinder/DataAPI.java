package com.inu8bit.pathfinder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    private String GET_NEARBY_BUS_STOP = "http://openapi.tago.go.kr/openapi/service/BusSttnInfoInqireService/getCrdntPrxmtSttnList";
    private String GET_STATION_BY_NAME = "http://openapi.tago.go.kr/openapi/service/BusSttnInfoInqireService/getSttnNoList";
    private String GET_BUS_ARRIVAL_INFO = "http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList";

    public Map<Integer, String[]> getNearbyBusStop(double lat, double lon) throws InterruptedException, ExecutionException, JSONException{
        String url = this.url + GET_NEARBY_BUS_STOP;
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
        List<String> prevList = new ArrayList<>();
        for (int i = 0; i < busStopList.length() && i < 4; i++) {
            JSONObject elem = busStopList.getJSONObject(i);
            if(prevList.contains(elem.getString("nodenm"))){
                continue;
            }
            else {
                busList.put(elem.getInt("nodeno"), new String[]{
                        Integer.toString(elem.getInt("citycode")),
                        elem.getString("nodenm"),
                        elem.getString("nodeid")
                });
                prevList.add(elem.getString("nodenm"));
            }
        }
        return busList;
    }

    public List<BusStop> getStationInfoByName(int citycode, String name) throws InterruptedException, ExecutionException, JSONException{
        url = new StringBuilder(GET_STATION_BY_NAME);
        params.put("ServiceKey", DataAPIKey);
        params.put("cityCode", String.valueOf(citycode));
        params.put("nodeNm", name.replaceAll(" ", ""));
        params.put("_type", "json");

        this.method = "GET";
        JSONObject result = new JSONObject(this.send()).getJSONObject("response");
        JSONArray stationList = result.getJSONObject("body").getJSONObject("items").getJSONArray("item");
        int num = result.getJSONObject("body").getInt("totalCount");
        List<BusStop> stopList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            JSONObject station = stationList.getJSONObject(i);
            stopList.add(new BusStop(
                    station.getString("nodeid"),
                    station.getString("nodenm"),
                    station.getInt("nodeno"),
                    station.getDouble("gpslati"),
                    station.getDouble("gpslong"))
            );
        }
        return stopList;
    }
    public List<Bus> getArrivalBusList(int citycode, String stationID) throws InterruptedException, ExecutionException, JSONException{
        url = new StringBuilder(GET_BUS_ARRIVAL_INFO);
        params.put("ServiceKey", DataAPIKey);
        params.put("cityCode", String.valueOf(citycode));
        params.put("nodeId", stationID);
        params.put("_type", "json");


        this.method = "GET";
        JSONObject stationInfo = new JSONObject(this.send())
                .getJSONObject("response")
                .getJSONObject("body");

        List<Bus> busList = new ArrayList<>();
        int numOfBus = stationInfo.getInt("totalCount");
        stationInfo = stationInfo.getJSONObject("items");
        for (int i = 0; i < numOfBus; i++) {
            JSONObject busInfo = stationInfo.getJSONArray("item").getJSONObject(i);
            // Bus(int _num, int _remainStops, int _remainTime, String _routeType, String _busType){
            busList.add(new Bus(
                    busInfo.getString("routeno"),
                    busInfo.getInt("arrprevstationcnt"),
                    busInfo.getInt("arrtime"),
                    busInfo.getString("routetp"),
                    busInfo.getString("vehicletp")));
        }
        Collections.sort(busList, new Comparator<Bus>() {
            @Override
            public int compare(Bus o1, Bus o2) {
                if(o1.getRemainTime() == o2.getRemainTime())
                    return 0;
                return o1.getRemainTime() < o2.getRemainTime()? -1:1;
            }
        });

        return busList;



    }
}
