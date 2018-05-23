package com.inu8bit.pathfinder;

/**
 *
 */

public class Bus {
    private String num;
    private int remainStops;
    private int remainTime;
    private String routeType;
    private String busType;

    Bus(String _num, int _remainStops, int _remainTime, String _routeType, String _busType){
        num = _num;
        remainStops = _remainStops;
        remainTime = _remainTime;
        routeType = _routeType;
        busType = _busType;
    }

    public String getNum(){
        return num;
    }
    public int getRemainStops(){
        return remainStops;
    }
    public int getRemainTime(){
        return remainTime;
    }
    public String getRouteType(){
        return routeType;
    }
    public String getBusType(){
        return busType;
    }
}
