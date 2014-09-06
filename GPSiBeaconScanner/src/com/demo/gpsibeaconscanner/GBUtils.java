package com.demo.gpsibeaconscanner;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class GBUtils {
    private final static String TAG = "GBUtils";
    private static PowerManager.WakeLock cpuWakeLock;
    public static void acquireCpuWakeLock(Context context) {
        if ( cpuWakeLock != null ) {
            Log.d(TAG, "cpu wake lock already exists.");
            return;
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        cpuWakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, TAG);
        cpuWakeLock.acquire();
        Log.d(TAG, "acquiring cpu wake lock. OK");
        pm=null;
    }

    public static void releaseCpuWakeLock() {
        if (cpuWakeLock != null) {
            cpuWakeLock.release();
            cpuWakeLock = null;
            Log.d(TAG, "releasing cpu wake lock. OK");
        } else {
            Log.d(TAG, "cpu wake lock already gone.");
        }
    }

    public static boolean isNetworkOnline(Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static boolean isAutoSyncChecked(Context context) {
        SharedPreferences settings = 
                PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean("autoSync_pref", false);
    }

    public static void syncLocalDBtoServer(Context ctx, AsyncHttpResponseHandler handlerResp) {
        GBDatabaseHelper dbHelper =
                GBDatabaseHelper.getInstance(ctx);
        String jsonDBStr = dbHelper.genJSONfromDB();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        log("dbHelper.getAllDBDataCount() : " + dbHelper.getAllDBDataCount());
        log("dbHelper.getNonSyncDBDataCount() : " + dbHelper.getNonSyncDBDataCount());
        if (dbHelper.getAllDBDataCount() != 0 &&
                dbHelper.getNonSyncDBDataCount() != 0) {
            log("syncLocalDBtoServer: " + jsonDBStr);
            showToastIfEnabled(ctx, "Sync DB to server ...");
            params.put("recbeacons", jsonDBStr);
            client.post("http://www.yiezi.com/beacons/common/insertrec.php", params, handlerResp);
        } else {
        	showToastIfEnabled(ctx, "No data needed to sync");
        }
    }

    public static void showToastIfEnabled(Context ctx, String str) {
    	showToastIfEnabled(ctx, str, false);
    }

    public static void showToastIfEnabled(Context ctx, String str, boolean ignorePref) {
    	boolean isToastEnabled = PreferenceManager.getDefaultSharedPreferences(ctx)
    			.getBoolean("enableToast_pref", false);
    	if (ignorePref || isToastEnabled) {
    		Toast.makeText(ctx, str, Toast.LENGTH_LONG).show();
    	}
    }

    private static void log(String s) {
        Log.d(TAG, s);
    }
}
