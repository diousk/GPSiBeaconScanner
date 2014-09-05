package com.demo.gpsibeaconscanner;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class MyLocation {
    private static String TAG = "GBMyLocation";
    LocationManager mLocationManager;
    LocationResult mLocationResult;
    boolean isGpsEnabled=false;
    boolean isNetworkEnabled=false;

    public boolean startLocation(Context context, LocationResult result) {
    	mLocationResult = result;
        if(mLocationManager == null) {
        	mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        try{
        	isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex){log(ex.toString());}
        try{
        	isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex){log(ex.toString());}

        log("isGpsLocEnabled: " + isGpsEnabled);
        log("isNetworkLocEnabled: " + isNetworkEnabled);
        // just return if no provider is enabled
        if(!isGpsEnabled && !isNetworkEnabled) {
            return false;
        }

        if(isGpsEnabled) {
        	mLocationManager.requestLocationUpdates(
        			LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        }
        if(isNetworkEnabled) {
        	mLocationManager.requestLocationUpdates(
        			LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        }
        return true;
    }

    private static void log(String s) {
    	Log.d(TAG , s);
	}

    public void stopLocation() {
    	if (mLocationManager != null) {
    		mLocationManager.removeUpdates(locationListenerGps);
    		mLocationManager.removeUpdates(locationListenerNetwork);
    	}
    }
	LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
        	log("locationListenerGps - onLocationChanged");
        	mLocationResult.gotLocation(location);
        	mLocationManager.removeUpdates(this);
        	mLocationManager.removeUpdates(locationListenerNetwork);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
        	log("locationListenerNetwork - onLocationChanged");
        	mLocationResult.gotLocation(location);
        	mLocationManager.removeUpdates(this);
        	mLocationManager.removeUpdates(locationListenerGps);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}