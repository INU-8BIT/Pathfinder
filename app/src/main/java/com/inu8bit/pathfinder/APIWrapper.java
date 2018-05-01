package com.inu8bit.pathfinder;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Wrapper Class for API Call via GET, POST and so on.
 */

public class APIWrapper extends AsyncTask<Void, Void, JSONObject> {
    protected String serviceKey;
    protected StringBuilder url = new StringBuilder();
    protected String method;
    protected Map<String, String> params = new HashMap<>();
    protected HttpURLConnection conn;
    protected JSONObject response;
    protected boolean isException = false;

    @Override
    protected JSONObject doInBackground(Void... voids) {
        try {
            if (method.equals("GET")) {
                url.append("?");
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    url.append(entry.getKey());
                    url.append("=");
                    url.append(entry.getValue());
                    url.append("&");
                }
            }
            conn = (HttpURLConnection) new URL(url.toString()).openConnection();
            conn.setRequestMethod(this.method);

            if (this.method.equals("POST")) {
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(this.params.toString());
                wr.flush();
                wr.close();
            }

            BufferedReader bufferedReader;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300)
                bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            else
                bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line);

            bufferedReader.close();
            conn.disconnect();
            response = new JSONObject(stringBuilder.toString());
            return response;

        } catch (IOException | JSONException e) {
            isException = true;
            Log.d("Error", e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONObject s){
        // TODO: Handle Exception (send message for example?)
        super.onPostExecute(s);
    }

}
