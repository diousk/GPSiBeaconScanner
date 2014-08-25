package com.demo.gpsibeaconscanner;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class GBPreferenceFrag extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	private static final String TAG = "GBPreferenceFrag";
	private GBiBeacon mIBeacon;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
        mIBeacon = (GBiBeacon) getActivity().getApplication();
        
        Log.d(TAG, "onSharedPreferenceChanged: key = " + key);
        if(key.equals("gpsPeriodOutter_pref")) {
            mIBeacon.gpsPeriodOutter = Integer.parseInt(sharedPreferences.getString(key, "0"));
            
        } else if(key.equals("gpstracking_pref")) {
            mIBeacon.gpstracking = Integer.parseInt(sharedPreferences.getString(key, "0"));
            
        } else if(key.equals("gpsPeriodIntter_pref")) {
            mIBeacon.gpsPeriodIntter = Integer.parseInt(sharedPreferences.getString(key, "0"));
            
        } else if(key.equals("gpsx1_pref")) {
            mIBeacon.gpsx1 = Double.parseDouble(sharedPreferences.getString(key, "0.0"));
            
        } else if(key.equals("gpsy1_pref")) {
            mIBeacon.gpsy1 = Double.parseDouble(sharedPreferences.getString(key, "0.0"));
            
        } else if(key.equals("gpsx2_pref")) {
            mIBeacon.gpsx2 = Double.parseDouble(sharedPreferences.getString(key, "0.0"));
            
        } else if(key.equals("gpsy2_pref")) {
            mIBeacon.gpsy2 = Double.parseDouble(sharedPreferences.getString(key, "0.0"));
            
        }
	}
}