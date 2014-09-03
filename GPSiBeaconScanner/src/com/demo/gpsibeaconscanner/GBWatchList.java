package com.demo.gpsibeaconscanner;

import java.util.ArrayList;

public class GBWatchList {
    private static final ArrayList<SimpleBeaconData> watchList =
            new ArrayList<SimpleBeaconData>() {
                private static final long serialVersionUID = 1L;
            {
                add(new SimpleBeaconData("e2c56db5-dffb-48d2-b060-d0f5a71096e0", "0", "1"));
                add(new SimpleBeaconData("e2c56db5-dffb-48d2-b060-d0f5a71096e0", "99", "1"));
                //add more here
            }};

    public static boolean containsiBeacon(String uuid, String major, String minor) {
        SimpleBeaconData other = new SimpleBeaconData(uuid, major, minor);
        for (SimpleBeaconData beacon : watchList) {
            if (beacon.equals(other)) {
                return true;
            }
        }
        return false;
    }
}
