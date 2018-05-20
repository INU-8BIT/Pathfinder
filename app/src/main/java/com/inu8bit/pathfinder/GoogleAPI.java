package com.inu8bit.pathfinder;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.inu8bit.pathfinder.BuildConfig.GoogleDirectionAPIKey;
import static com.inu8bit.pathfinder.BuildConfig.GooglePlaceAPIKey;

/**
 * Class for Google Direction API brought by Google and SK Map
 * Usage:
 *      1. Get the route to destination via transit
 */
public class GoogleAPI extends APIWrapper {
    private String directionURL = "https://maps.googleapis.com/maps/api/directions/json";
    private String placeURL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    private String detailedPlaceURL = "https://maps.googleapis.com/maps/api/place/details/json";


    public List<Route> getTransitRoute(double stLat, double stLon, double edLat, double edLon) throws InterruptedException, ExecutionException, JSONException {
        url = new StringBuilder(directionURL);
        params.put("origin", String.valueOf(stLat) + "," + String.valueOf(stLon));
        params.put("destination", String.valueOf(edLat) + "," + String.valueOf(edLon));
        params.put("mode", "transit");
        params.put("key", GoogleDirectionAPIKey);

        this.method = "GET";
        return this.getSteps(new JSONObject(this.send()));
    }

    public List<Route> getTransitRoute(String stName, String edName) throws InterruptedException, ExecutionException, JSONException {
        url = new StringBuilder(directionURL);
        params.put("origin", stName);
        params.put("destination", edName);
        params.put("mode", "transit");
        params.put("key", GoogleDirectionAPIKey);
        params.put("language", "ko");

        this.method = "GET";
        return this.getSteps(new JSONObject(this.send()));
    }

    private List<Route> getSteps(JSONObject jsonObject) throws InterruptedException, ExecutionException, JSONException {
        // TODO: make these following steps as Class or structural object.

        JSONObject routeInfo = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
        JSONArray steps = routeInfo.getJSONArray("steps");

        String total_departure_time = routeInfo.getJSONObject("departure_time").getString("text").replace(":", "시");
        String total_arrival_time = routeInfo.getJSONObject("arrival_time").getString("text").replace(":", "시");
        String distance = routeInfo.getJSONObject("distance").getString("text");
        String duration = routeInfo.getJSONObject("duration").getString("text");

        List<Route> route = new ArrayList<>();
        for(int i = 0; i < steps.length(); i++){
            JSONObject currentStep = steps.getJSONObject(i);
            String travel_mode = currentStep.getString("travel_mode");
            String instruction = currentStep.getString("html_instructions");

            String arrival = null;
            String arrival_time = null;
            String method = null;
            String dist = null;
            String agency = null;
            String agencyTel = null;

            if(travel_mode.equals("TRANSIT")) {
                JSONObject details = currentStep.getJSONObject("transit_details");
                Log.d("Details: ", instruction);
                arrival = details.getJSONObject("arrival_stop").getString("name");
                arrival_time = details.getJSONObject("arrival_time").getString("text").replace(":", "시") + "분";
                method = details.getJSONObject("line").getString("name") + details.getJSONObject("line").getString("short_name");
                dist = String.valueOf(details.getInt("num_stops"));
                agency = details.getJSONObject("line").getJSONArray("agencies").getJSONObject(0).getString("name");

                try {
                    // TODO: when place not found
                    String agencyID = this.getPlaceID(agency);
                    agencyTel = this.getPlaceTelephoneNumber(agencyID);
                } catch (JSONException ex){
                    agencyTel = null;
                }
            }
            else if (travel_mode.equals("WALKING")){
                dist = currentStep.getJSONObject("distance").getString("text");
            }
            /*
            route.add(new Route(
                    travel_mode, instruction, null, null, null, dist, null, null
            ));*/

            route.add(new Route(
                    travel_mode, instruction, arrival, arrival_time, method, dist, agency, agencyTel
            ));
        }

        return route;
    }

    // https://maps.googleapis.com/maps/api/place/textsearch/json?query=QUERY&key=YOUR_KEY&language=ko
    private String getPlaceID(String name) throws InterruptedException, ExecutionException, JSONException {
        url = new StringBuilder(placeURL);
        params.put("query", name + "한국");
        params.put("key", GooglePlaceAPIKey);
        params.put("language", "ko");

        this.method = "GET";
        String result = this.send();
        return new JSONObject(result)
                .getJSONArray("results")
                .getJSONObject(0)
                .getString("place_id");
    }

    // https://maps.googleapis.com/maps/api/place/details/json?placeid=PLACE_ID&key=YOUR_API_KEY
    private String getPlaceTelephoneNumber(String placeID) throws InterruptedException, ExecutionException, JSONException {
        url = new StringBuilder(detailedPlaceURL);
        params.put("placeid", placeID);
        params.put("key", GooglePlaceAPIKey);
        params.put("language", "ko");

        this.method = "GET";
        String result = this.send();
        return new JSONObject(result).getJSONObject("result")
                .getString("formatted_phone_number");
    }
}
