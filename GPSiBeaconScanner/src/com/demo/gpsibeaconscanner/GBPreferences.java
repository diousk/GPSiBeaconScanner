package com.demo.gpsibeaconscanner;

import java.util.HashSet;
import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

	 public static void addiBeaconToWatchList(Context ctx, String uuid) {
	     SharedPreferences settings = 
                 PreferenceManager.getDefaultSharedPreferences(ctx);
	     SharedPreferences.Editor editor = settings.edit();
	     Set<String> watchList = settings.getStringSet("watchList", new HashSet<String>());
	     watchList.add(uuid);
	     log("watchList.size" + watchList.size());
	     editor.putStringSet("watchList", watchList);
	     editor.commit();
	 }

	 public static void removeiBeaconFromWatchList(Context ctx, String uuid){
	     SharedPreferences settings = 
	             PreferenceManager.getDefaultSharedPreferences(ctx);
	     SharedPreferences.Editor editor = settings.edit();
	     Set<String> watchList = settings.getStringSet("watchList", new HashSet<String>());
         watchList.remove(uuid);
         log("watchList.size" + watchList.size());
         editor.putStringSet("watchList", watchList);
         editor.commit();
	 }

	 public static boolean isiBeaconWatched(Context ctx, String uuid) {
	     SharedPreferences settings = 
	             PreferenceManager.getDefaultSharedPreferences(ctx);
	     Set<String> watchList = settings.getStringSet("watchList", new HashSet<String>());
	     return (watchList == null) ? false : watchList.contains(uuid);
	 } 
}
