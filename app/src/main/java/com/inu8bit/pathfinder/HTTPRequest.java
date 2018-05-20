package com.inu8bit.pathfinder;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by fenslett on 5/15/2018.
 */

public class HTTPRequest extends AsyncTask<String, Void, String> {
    protected HttpURLConnection conn;
    protected String response;

    @Override
    protected String doInBackground(String... strings) {
        try {
            conn = (HttpURLConnection) new URL(strings[0]).openConnection();
            conn.setRequestMethod("GET");

                 /*
                if (this.method.equals("POST")) {
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(this.params.toString());
                wr.flush();
                wr.close();
                }
                */
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
            return stringBuilder.toString();
        } catch (IOException e){
            return null;
        }
    }
}
