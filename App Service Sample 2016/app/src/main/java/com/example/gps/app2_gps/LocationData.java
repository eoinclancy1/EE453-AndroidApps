package com.example.gps.app2_gps;

import java.util.ArrayList;

/**
 * Created by finnk on 11/5/2016.
 */

public class LocationData {

    public double longitude;
    public double latitude;
    public ArrayList<String> BTinfo;

    public LocationData() {

    }

    public LocationData(double lati,double longi, ArrayList<String> s) {
        longitude = longi;
        latitude = lati;
        BTinfo = s;
    }

    public int getNumBTdevices(){
        if (BTinfo == null){
            return 0;
        }
        return BTinfo.size();
    }


}
