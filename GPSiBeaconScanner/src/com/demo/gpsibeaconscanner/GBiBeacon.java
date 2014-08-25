package com.demo.gpsibeaconscanner;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class GBiBeacon extends Application {
	private static final String TAG = "IBeacon";
	public SharedPreferences mySharedPreferences;
	public int gpsPeriodOutter, gpstracking, gpsPeriodIntter;
	public double gpsx1, gpsy1, gpsx2, gpsy2;
	
	@Override
    public void onCreate() { 
        // TODO Auto-generated method stub 
        super.onCreate();
        Log.d(TAG, "onCreate");
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        gpsPeriodOutter = Integer.valueOf(mySharedPreferences.getString("gpsPeriodOutter_pref", "0"));
        gpstracking = Integer.valueOf(mySharedPreferences.getString("gpstracking_pref", "0"));
        gpsPeriodIntter = Integer.valueOf(mySharedPreferences.getString("gpsPeriodIntter_pref", "0"));
        
        gpsx1 = Double.valueOf(mySharedPreferences.getString("gpsx1_pref", "0.0"));
        gpsy1 = Double.valueOf(mySharedPreferences.getString("gpsy1_pref", "0.0"));
        gpsx2 = Double.valueOf(mySharedPreferences.getString("gpsx2_pref", "0.0"));
        gpsy2 = Double.valueOf(mySharedPreferences.getString("gpsy2_pref", "0.0"));
        
        Log.d(TAG, "Get Preference Value: gpsPeriodOutter = " + gpsPeriodOutter + ", gpstracking = " + gpstracking + ", gpsPeriodIntter = " + gpsPeriodIntter);
    } 
	
	public double getGpsAddress() {
		return gpsx1;
	}


}

