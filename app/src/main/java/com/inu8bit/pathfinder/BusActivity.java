package com.inu8bit.pathfinder;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class BusActivity extends AppCompatActivity {

    private GPSInfo gpsInfo;
    private GoogleAPI googleAPI;
    private DataAPI dataAPI;
    private TTSManager ttsManager;
    private double lat, lon;
    private Map<Integer, String[]> list;
    private ImageView imageView;
    private String stopName = null;
    private String busNum = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);

        ttsManager = new TTSManager();
        ttsManager.init(this);

        gpsInfo = new GPSInfo(getApplicationContext());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                ttsManager.initQueue("주변 정류장 검색페이지입니다.");
                ttsManager.addQueue("위로 스와이프: 이름으로 정류장 검색");
                ttsManager.addQueue("아래로 스와이프, 인근 정류장 검색");
                ttsManager.addQueue("오른쪽으로 스와이프, 주변 500m 내 지하철 검색");
            }
        }, 1000);

        imageView = findViewById(R.id.imageView);
        imageView.setOnTouchListener(new SwipeListener(getApplicationContext()){
            @Override
            public void onLeft(){
                BusActivity.super.onBackPressed();
            }

            @Override
            public void onTop(){
                ttsManager.initQueue("정류장 이름을 말씀하세요");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());           // Call Package
                        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");                     // Set Language
                        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "정류장 명");                       // Prompt Message
                        startActivityForResult(i, 0);
                    }
                }, 1000);
            }

            @Override
            public void onBottom(){
                ttsManager.initQueue("근처 정류장을 탐색하겠습니다.");
                gpsInfo.getCurrentLocation();

                lat = gpsInfo.getLatitude();
                lon = gpsInfo.getLongitude();

                dataAPI = new DataAPI();
                try {
                    list = dataAPI.getNearbyBusStop(lat, lon);
                    ttsManager.addQueue(Integer.toString(list.size()) + "개의 정류장이 검색되었습니다");
                    ttsManager.addQueue("가장 가까운 정류장 3개소의 이름은 ");
                    int i = 1;
                    for (Map.Entry<Integer, String[]> entry : list.entrySet()){
                        // even if the size of list is smaller than 3, it will pass without any error.
                        if (i > 3)
                            break;
                        ttsManager.addQueue(entry.getValue()[1] + ", ");
                        i++;
                    }
                    ttsManager.addQueue("입니다. ");
                    ttsManager.addQueue("....");
                } catch (InterruptedException | ExecutionException | JSONException e){
                    // TODO: Differenciate Exception
                    Log.e("Error", "Exception happened: " + e.getMessage());
                }
            }

            @Override
            public void onRight() {
                ttsManager.initQueue("주변 500m 내 지하철 검색 중..");
                try {
                    gpsInfo = new GPSInfo(BusActivity.this);
                    gpsInfo.getCurrentLocation();
                    double lat = gpsInfo.getLatitude();
                    double lon = gpsInfo.getLongitude();
                    googleAPI = new GoogleAPI();

                    List<String> subway = googleAPI.getNearbyPlace(lat, lon, "subway_station", 500);
                    if(subway.isEmpty()){
                        ttsManager.initQueue("주변 500m 내에 지하철 역은 없습니다");
                    }
                    else {
                        ttsManager.initQueue("주변 500m 내에 " + subway.get(0) + "이 있습니다");
                    }
                } catch (Exception e){

                }
            }

            @Override
            public void onTapUp() {
                int cityCode = getCityCode();
                dataAPI = new DataAPI();
                try {
                    List<BusStop> stationList = dataAPI.getStationInfoByName(cityCode, stopName);
                    List<Bus> buses = dataAPI.getArrivalBusList(cityCode, stationList.get(0).nodeid);
                    ttsManager.initQueue("가장 가까운 정류장의 이름은 ");
                    ttsManager.addQueue(stationList.get(0).name + "입니다");
                    ttsManager.addQueue("도착 예정 버스는 다음과 같습니다.");

                    for(Bus bus:buses){
                        String num = bus.getNum().replaceAll("-", "다시");
                        int remainTime = bus.getRemainTime() / 60;
                        int remainBusStops = bus.getRemainStops();
                        ttsManager.addQueue(num + "번 버스");
                        if(remainBusStops == 1){
                            ttsManager.addQueue("잠시 후 ");
                        }
                        else {
                            ttsManager.addQueue(remainBusStops +"개정류장전");
                            ttsManager.addQueue(remainTime + "분 후");
                        }
                    }

                } catch (Exception e){
                    Log.d("error: ", e.getMessage());
                    ttsManager.initQueue("검색 결과가 없습니다");
                }
            }
        });
    }

    private int getCityCode(){
        googleAPI = new GoogleAPI();
        gpsInfo = new GPSInfo(BusActivity.this);
        gpsInfo.getCurrentLocation();
        try {
            String address = googleAPI.coordToAddress(gpsInfo.getLatitude(), gpsInfo.getLongitude());
            return CityCode.getCityCode(address);

        } catch (Exception e){
            Log.d("Error: ", e.getMessage());
            return -1;
        }
    }
    // TODO: Make this as a class and get return value via Listener
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            List<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            switch (requestCode) {
                case 0:
                    Log.d("stop name: ", matches.get(0));
                    stopName = matches.get(0);
                    break;
            }
            ttsManager.initQueue("검색을 시작하시려면 화면을 터치하세요");
        } catch (Exception e){
            // When Voice Recognition View is stopped or user touched outside the box
            ttsManager.initQueue("입력이 취소되었습니다");
            super.onActivityResult(requestCode, resultCode, data);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause(){
        ttsManager.stop();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsManager.stop();
        ttsManager.shutDown();
    }
}
