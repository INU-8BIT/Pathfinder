package com.inu8bit.pathfinder;

/**
 * 
 */

public class BusStop {
    public String nodeid;
    public String name;
    public int nodenum;
    public double lat, lon;

    BusStop(String _nodeid, String _name, int _nodenum, double _lat, double _lon){
        nodeid = _nodeid;
        name = _name;
        nodenum = _nodenum;
        lat = _lat;
        lon = _lon;
    }
}
