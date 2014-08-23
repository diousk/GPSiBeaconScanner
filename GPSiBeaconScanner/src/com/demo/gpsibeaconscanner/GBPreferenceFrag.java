package com.demo.gpsibeaconscanner;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class GBPreferenceFrag extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	}
}