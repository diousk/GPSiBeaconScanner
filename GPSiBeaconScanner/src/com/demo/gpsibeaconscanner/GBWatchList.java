package com.demo.gpsibeaconscanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class GBWatchList {
    private static Object mWatchListObj = new Object();
    private static ArrayList<SimpleBeaconData> mWatchList =
            new ArrayList<SimpleBeaconData>() {
                private static final long serialVersionUID = 9177429518534250592L;
            {
                // default watch list
                add(new SimpleBeaconData("e2c56db5-dffb-48d2-b060-d0f5a71096e0", "0", "1"));
                add(new SimpleBeaconData("e2c56db5-dffb-48d2-b060-d0f5a71096e0", "99", "2"));
                //add more here
            }};
    private final static String TAG = "GBWatchList";

    public static boolean isiBeaconInDefaultList(String uuid, String major, String minor) {
        SimpleBeaconData other = new SimpleBeaconData(uuid, major, minor);
        synchronized (mWatchListObj) {
            for (SimpleBeaconData beacon : mWatchList) {
                if (beacon.equals(other)) {
                    return true;
                }
            }
        }
        return false;
    }
    /*
    private static boolean containsiBeacon(String uuid, String major, String minor) {
        SimpleBeaconData other = new SimpleBeaconData(uuid, major, minor);
        synchronized (mWatchListObj) {
            for (SimpleBeaconData beacon : mWatchList) {
                if (beacon.equals(other)) {
                    return true;
                }
            }
        }
        return false;
    }*/

    /*
    private static boolean addiBeacon(String uuid, String major, String minor) {
        SimpleBeaconData other = new SimpleBeaconData(uuid, major, minor);
        synchronized (mWatchListObj) {
            if (mWatchList != null) {
                mWatchList.add(other);
            }
        }
        return false;
    }*/

    public static int getDefCount() {
        int count = 0;
        synchronized (mWatchListObj) {
            if (mWatchList != null) {
                count = mWatchList.size();
            }
        }
        return count;
    }

    public static ArrayList<SimpleBeaconData> getDefWatchListCopy() {
        ArrayList<SimpleBeaconData> copy = null;
        synchronized (mWatchListObj) {
            if (mWatchList != null) {
                copy = new ArrayList<SimpleBeaconData>(mWatchList);
            }
        }
        return copy;
    }

    public static ArrayList<HashMap<String,String>> getCompleteWatchList(Context ctx) {
        ArrayList<HashMap<String,String>> array= new ArrayList<HashMap<String,String>>();
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(ctx);
        Set<String> watchList = settings.getStringSet("watchList", new HashSet<String>());

        synchronized (mWatchListObj) {
            for (String uuidMajMin : watchList) {
                HashMap<String,String> item = new HashMap<String,String>();
                String[] arrayUUIDMajMin = uuidMajMin.split(":");
                item.put("data", arrayUUIDMajMin[0]);
                item.put("extra1", "major:"+arrayUUIDMajMin[1]+", minor:"+arrayUUIDMajMin[2]);
                array.add(item);
            }
        }
        return array;
    }

    // preference part
    public static void initializeWatchList(Context ctx) {
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        Set<String> watchList = settings.getStringSet("watchList", new HashSet<String>());

        synchronized (mWatchListObj) {
            for (SimpleBeaconData beacon : mWatchList) {
                watchList.add(
                    beacon.getUUID() + ":" +
                    beacon.getMajor() + ":" +
                    beacon.getMinor());
            }
        }

        log("initializeWatchList - watchList.size " + watchList.size());
        editor.putStringSet("watchList", watchList);
        editor.commit();
        dump(ctx);
    }

    public static void addiBeaconToWatchList(Context ctx, String uuidMajorMinor) {
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        Set<String> watchList = settings.getStringSet("watchList", new HashSet<String>());
        watchList.add(uuidMajorMinor);
        log("addiBeaconToWatchList - watchList.size " + watchList.size());
        editor.putStringSet("watchList", watchList);
        editor.commit();
        dump(ctx);
    }

    public static void removeiBeaconFromWatchList(Context ctx, String uuidMajorMinor){
        SharedPreferences settings = 
                PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        Set<String> watchList = settings.getStringSet("watchList", new HashSet<String>());
        watchList.remove(uuidMajorMinor);
        log("removeiBeaconFromWatchList - watchList.size " + watchList.size());
        editor.putStringSet("watchList", watchList);
        editor.commit();
        dump(ctx);
    }

    public static boolean isiBeaconWatched(Context ctx, String uuidMajorMinor) {
        SharedPreferences settings = 
                PreferenceManager.getDefaultSharedPreferences(ctx);
        Set<String> watchList = settings.getStringSet("watchList", new HashSet<String>());
        dump(ctx);
        return (watchList == null) ? false : watchList.contains(uuidMajorMinor);
    }

    // log part
    private static void dump(Context ctx) {
        SharedPreferences settings = 
                PreferenceManager.getDefaultSharedPreferences(ctx);
        Set<String> watchList = settings.getStringSet("watchList", new HashSet<String>());
        synchronized (mWatchListObj){
            log("dump watch list :");
            for (String beaconStr : watchList) {
                log(beaconStr);
            }
        }
    }

    private static void log(String string) {
        Log.d(TAG, string);
    }
}
