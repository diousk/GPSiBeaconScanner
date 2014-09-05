package com.demo.gpsibeaconscanner;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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
	        showGBWatchList();
	    }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void showGBWatchList() {
        ArrayList<HashMap<String,String>> list = GBWatchList.getCompleteWatchList(mContext);
        final SimpleAdapter adapter = new SimpleAdapter( 
                mContext, 
                list,
                android.R.layout.simple_list_item_2,
                new String[] { "extra1","data" },
                new int[] { android.R.id.text1, android.R.id.text2 } );

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Click to remove");
        alertDialog.setIcon(android.R.drawable.ic_menu_delete);
        alertDialog.setAdapter(adapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                @SuppressWarnings("unchecked")
                final HashMap<String,String> item = 
                        (HashMap<String, String>)adapter.getItem(which);
                log("data : "+item.get("data")+" "+item.get("extra1"));
                AlertDialog.Builder builderInner = new AlertDialog.Builder(mContext);
                builderInner.setTitle("Remove");
                builderInner.setMessage("Do you want to remove this from watch list?");
                builderInner.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface dialog,
                                int which) {
                            String uuid = item.get("data");
                            String[] majorminor = item.get("extra1").split("[,:]");
                            String major = majorminor[1];
                            String minor = majorminor[3];
                            if (GBWatchList.isiBeaconInDefaultList(uuid, major, minor)) {
                                log("Cannot remove default iBeacon settings");
                                Toast.makeText(mContext, "Cannot remove default iBeacon settings", Toast.LENGTH_SHORT).show();
                            } else {
                                String uuidMajMin = uuid + ":" + major + ":" + minor;
                                log("to be removed : " + uuidMajMin);
                                GBWatchList.removeiBeaconFromWatchList(mContext, uuidMajMin);
                            }
                            dialog.dismiss();
                        }
                    });
                builderInner.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface dialog,
                                int which) {
                            dialog.dismiss();
                        }
                    });
                builderInner.show();
            }
        });
        alertDialog.show();
    }

    @Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
        log("onSharedPreferenceChanged: key = " + key);
        updatePreference(key);
	}
}