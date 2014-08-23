package com.demo.gpsibeaconscanner;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

public class GBPreferences extends Activity {
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	  // TODO Auto-generated method stub
	  super.onCreate(savedInstanceState);
	  getFragmentManager().beginTransaction().replace(android.R.id.content,
	                new GBPreferenceFrag()).commit();
	  getActionBar().setDisplayShowTitleEnabled(true);
	  ActionBar actionBar = getActionBar();
	  actionBar.setDisplayHomeAsUpEnabled(true);
	 }
}
