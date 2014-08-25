package com.demo.gpsibeaconscanner;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

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
}
