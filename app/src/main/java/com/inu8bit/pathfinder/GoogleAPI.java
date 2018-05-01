package com.inu8bit.pathfinder;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.inu8bit.pathfinder.BuildConfig.GoogleAPIKey;

/**
 * Class for Google Direction API brought by Google and SK Map
 * Usage:
 *      1. Get the route to destination via transit
 */
public class GoogleAPI extends APIWrapper {
    // TODO: Let user to select when to go.
    GoogleAPI(){
        serviceKey = GoogleAPIKey;
        url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json");
    }

    public List<String> getTransitRoute(double stLat, double stLon, double edLat, double edLon) throws InterruptedException, ExecutionException, JSONException {
        params.put("origin", String.valueOf(stLat) + "," + String.valueOf(stLon));
        params.put("destination", String.valueOf(edLat) + "," + String.valueOf(edLon));
        params.put("mode", "transit");
        params.put("key", this.serviceKey);

        this.method = "GET";
        return this.getSteps(this.execute().get());
    }

    public List<String> getTransitRoute(String stName, String edName) throws InterruptedException, ExecutionException, JSONException {
        params.put("origin", stName);
        params.put("destination", edName);
        params.put("mode", "transit");
        params.put("key", this.serviceKey);
        params.put("language", "ko");
        params.put("departure_time", "1525327737");

        this.method = "GET";
        return this.getSteps(this.execute().get());

    }

    private List<String> getSteps(JSONObject _obj) throws JSONException {
        // TODO: make these following steps as Class or structural object.
        JSONObject obj = _obj.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
        JSONArray steps = obj.getJSONArray("steps");
        List<String> route = new ArrayList<>();
        for(int i = 0; i < steps.length(); i++){
            JSONObject step = steps.getJSONObject(i);

            String travel_mode = step.getString("travel_mode");
            if(travel_mode.equals("TRANSIT")) {
                JSONObject detail = step.getJSONObject("transit_details");
                route.add(detail.getJSONObject("line").getString("name") + " " + detail.getJSONObject("line").getString("short_name") + " " + detail.getString("headsign") + " 행 탑승");
                StringBuilder departure = new StringBuilder();
                departure.append("출발역: ");
                departure.append(detail.getJSONObject("departure_stop").getString("name"));
                departure.append(" (예정 시간: ");
                departure.append(detail.getJSONObject("departure_time").getString("text"));
                departure.append(") ");
                route.add(departure.toString());

                StringBuilder arrival = new StringBuilder();
                arrival.append("도착역: ");
                arrival.append(detail.getJSONObject("arrival_stop").getString("name"));
                arrival.append(" (예정 시간: ");
                arrival.append(detail.getJSONObject("arrival_time").getString("text"));
                arrival.append(") ");

                route.add(arrival.toString());
            }

            else if(travel_mode.equals("WALKING")){
                route.add(step.getString("html_instructions"));
                route.add("이동거리: " + step.getJSONObject("distance").getString("text"));
            }
        }

        return route;
    }
}
