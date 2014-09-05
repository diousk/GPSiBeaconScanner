package com.demo.gpsibeaconscanner;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class GBPreferences extends Activity {
    private static final String TAG = "GBPreferences";
    private static void log(String s) {
        Log.d(TAG, s);
    }

	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	     super.onCreate(savedInstanceState);
	     getFragmentManager().beginTransaction().replace(android.R.id.content,
	             new GBPreferenceFrag()).commit();
	     getActionBar().setDisplayShowTitleEnabled(true);
	     ActionBar actionBar = getActionBar();
	     actionBar.setDisplayHomeAsUpEnabled(true);
	 }

	 @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
                 finish();
                 return true;
         default:
                 return super.onOptionsItemSelected(item);
         }
     }
}
