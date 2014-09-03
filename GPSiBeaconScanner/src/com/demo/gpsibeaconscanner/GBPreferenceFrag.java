package com.demo.gpsibeaconscanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class GBPreferenceFrag extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	private static final String TAG = "GBPreferenceFrag";
	private Context mContext;
    private static void log(String s) {
        Log.d(TAG, s);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    mContext = this.getActivity();
	}
	
	@Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(mContext).
            unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.registerOnSharedPreferenceChangeListener(this);
        updatePreferences();
    }

    private void updatePreferences() {
        updatePreference("gpsPeriodOutter_pref");
        updatePreference("gpstracking_pref");
        updatePreference("gpsPeriodIntter_pref");
        updatePreference("gpsx1_pref");
        updatePreference("gpsy1_pref");
        updatePreference("gpsx2_pref");
        updatePreference("gpsy2_pref");
        updatePreference("iBeaconScanningTracking_pref");
        updatePreference("iBeaconInvalidTimeout_pref");
    }

    private void updatePreference(String key) {
        
        if(key.equals("gpsPeriodOutter_pref")) {
            Preference preference = findPreference(key);
            EditTextPreference editTextPreference =  (EditTextPreference)preference;
            if (editTextPreference.getText().trim().length() > 0) {
                editTextPreference.setSummary(editTextPreference.getText() + " minutes");
            }
        } else if(key.equals("gpstracking_pref")) {
            Preference preference = findPreference(key);
            EditTextPreference editTextPreference =  (EditTextPreference)preference;
            if (editTextPreference.getText().trim().length() > 0) {
                editTextPreference.setSummary(editTextPreference.getText() + " minutes");
            }
        } else if(key.equals("gpsPeriodIntter_pref")) {
            Preference preference = findPreference(key);
            EditTextPreference editTextPreference =  (EditTextPreference)preference;
            if (editTextPreference.getText().trim().length() > 0) {
                editTextPreference.setSummary(editTextPreference.getText() + " minutes");
            }
        } else if(key.equals("gpsx1_pref")) {
            Preference preference = findPreference(key);
            EditTextPreference editTextPreference =  (EditTextPreference)preference;
            if (editTextPreference.getText().trim().length() > 0) {
                editTextPreference.setSummary(editTextPreference.getText());
            }
        } else if(key.equals("gpsy1_pref")) {
            Preference preference = findPreference(key);
            EditTextPreference editTextPreference =  (EditTextPreference)preference;
            if (editTextPreference.getText().trim().length() > 0) {
                editTextPreference.setSummary(editTextPreference.getText());
            }
        } else if(key.equals("gpsx2_pref")) {
            Preference preference = findPreference(key);
            EditTextPreference editTextPreference =  (EditTextPreference)preference;
            if (editTextPreference.getText().trim().length() > 0) {
                editTextPreference.setSummary(editTextPreference.getText());
            }
        } else if(key.equals("gpsy2_pref")) {
            Preference preference = findPreference(key);
            EditTextPreference editTextPreference =  (EditTextPreference)preference;
            if (editTextPreference.getText().trim().length() > 0) {
                editTextPreference.setSummary(editTextPreference.getText());
            }
        } else if (key.equals("iBeaconScanningTracking_pref")) {
            Preference preference = findPreference(key);
            EditTextPreference editTextPreference =  (EditTextPreference)preference;
            if (editTextPreference.getText().trim().length() > 0) {
                editTextPreference.setSummary(editTextPreference.getText() + " seconds");
            }
        } else if (key.equals("iBeaconInvalidTimeout_pref")) {
            Preference preference = findPreference(key);
            EditTextPreference editTextPreference =  (EditTextPreference)preference;
            if (editTextPreference.getText().trim().length() > 0) {
                editTextPreference.setSummary(editTextPreference.getText() + " seconds");
            }
        }
	}

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
	    log("key: " + preference.getKey());
	    //TODO : not used yet
	    if ("iBeaconWatchlist_pref".equals(preference.getKey())) {
	    }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
        log("onSharedPreferenceChanged: key = " + key);
        updatePreference(key);
	}
}