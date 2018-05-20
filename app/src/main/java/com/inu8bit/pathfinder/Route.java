package com.inu8bit.pathfinder;

/**
 *
 */

public class Route {
    public String travel_mode;
    public String instruction;
    public String destination;
    public String arrival_time;
    public String method;
    public String length;
    public String agency;
    public String agencyNumber;

    // 수단(버스, 지하철 = transit, 도보 = walking), 간단한 설명, 목적지, 탑승 수단, 거리/정류장 수, 운수회사 이름, 운수회사 전화번호
    public Route(String _travel_mode, String _instruction, String _destination, String _arrival_time, String _method, String _length, String _agency, String _agencyNumber) {
        this.travel_mode = _travel_mode;
        this.instruction = _instruction;
        this.destination = _destination;
        this.arrival_time = _arrival_time;
        this.method = _method;
        this.length = _length;
        this.agency = _agency;
        this.agencyNumber = _agencyNumber;
    }
}
