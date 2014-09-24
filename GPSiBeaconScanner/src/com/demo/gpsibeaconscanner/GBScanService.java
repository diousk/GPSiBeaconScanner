package com.demo.gpsibeaconscanner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;
import com.demo.gpsibeaconscanner.MyLocation.LocationResult;

public class GBScanService extends Service implements iBeaconScanManager.OniBeaconScan{
    private final static String TAG = "GBScanService";
    private Context mContext;
    private int NOTIFICATION = R.string.local_service_started;
    private static boolean isReceiverRegistered = false;
    private ScanHandler mHandler;
    private ScanReceiver mReceiver;
    private AlarmManager am;
    private Boolean mIsGPSInRegion = false, mIsBTInRegion = false;
    private long startScanTime, stopScanTime;
    private MyLocation mMyLocation;
    private LocationResult mLocationResult;
    /*
    private LocationManager mLocationManager;
    private MyLocationListener mLocationListener;
    */
    public double[] addressX = new double[2];
    public double[] addressY = new double[2];

    // iBeacon component
    private iBeaconScanManager miScaner = null;
    private List<ScanediBeacon> miBeacons = new ArrayList<ScanediBeacon>();
    private Object mBeaconsObj = new Object();
    private int mBeaconScanTime = 30000; //default 30s
    private int mBeaconTimeout = 20000; //default 20s, means invalid if too old

    // GPS component
    private int gpstracking;
    private double gpsLocX1, gpsLocY1, gpsLocX2, gpsLocY2;

    // General component
    private int periodOuter, periodInner;
    private boolean autoSyncEnabled = false;
    private boolean keepScaniBeacon = false;
    private boolean dontSynciBeaconToDB = false;

    private final static String ACTION_SCANNING_START = "scanning.start"; // should be trigger by alarm manager
    private final static String ACTION_SCANNING_STOP = "scanning.stop"; // should be trigger by alarm manager

    private final static int MSG_START_SCAN_GPS = 2001;
    private final static int MSG_START_SCAN_IBEACON = 2002;
    private final static int MSG_STOP_SCAN_GPS = 2003;
    private final static int MSG_STOP_SCAN_IBEACON = 2004;
    private final static int MSG_STOP_ALL_SCAN = 2005;
    private final static int MSG_UPDATE_DATABASE = 2006;
    private final static int MSG_SCAN_TIME_OUT = 2007;
    private final static int MSG_GPS_LOCATION_FIXED = 2009;
    private final static int MSG_SET_ALARM = 2010;
    private final static int MSG_SYNC_DB_TO_SERVER = 2011;

    private final static int TYPE_DATA_IBEACON = 101;
    private final static int TYPE_DATA_GPS = 102;
    private final static String TYPE_STRING_IBEACON = "iBeacon";
    private final static String TYPE_STRING_GPS = "GPS";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mContext = this.getBaseContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand - intent " + intent);
        if (intent == null) {
            // server get killed by system and restart
            cancelScanAlarm(mContext);
        }

        showForegroundNotification();
        setupReceiver();
        setupHandler();
        setupiBeaconScanner();
        setupGps();
        updateSettingPreferences();

        // start scanning when first time service starts
        triggerScanProcedure();

        return super.onStartCommand(intent, flags, startId);
    }

    private void setupGps() {
        if (mMyLocation == null) {
        	mMyLocation = new MyLocation();
        }

        if (mLocationResult == null)
        	mLocationResult = new LocationResult(){
            @Override
            public void gotLocation(Location location){
                if (location == null) return;
                log("Got location fixed: Lat = " + location.getLatitude()
                		+ " , Long = " + location.getLongitude());
                sendLocationFixedMsg(
                        location.getLatitude(), location.getLongitude());
            }
        };
    }

    @Override
    public void onDestroy() {
        log("onDestroy");
        stopForeground(true);

        if (isReceiverRegistered) {
            this.unregisterReceiver(mReceiver);
            isReceiverRegistered = false;
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if (miScaner != null) {
            miScaner.stopScaniBeacon();
        }
        stopGpsIfPossible();
        cancelScanAlarm(mContext);
        setScanRunning(false);

        GBUtils.releaseCpuWakeLock();
    }

    private void setScanRunning(boolean state) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("scanRunning", state);
        editor.commit();        
    }

    @Override
    public void onScaned(iBeaconData iBeacon) {
        addOrUpdateiBeacon(iBeacon);
    }

    private long setupScanAlarms(Context context) {
        stopScanTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(stopScanTime);
        boolean inRegion = isInRegion();
        if (inRegion) {
            calendar.add(Calendar.MINUTE, periodInner);
        } else {
            calendar.add(Calendar.MINUTE, periodOuter);
        }

        log("setupScanAlarms - isInRegion: " + inRegion
                + ", gpsPeriodIntter:" + periodInner
                + ", gpsPeriodOutter:" + periodOuter);

        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(ACTION_SCANNING_START);
        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long totalScanTime = stopScanTime - startScanTime;
        //log("Time: totalScanTime = " + totalScanTime);
        //log("Time: lunchTime = " + (calendar.getTimeInMillis() - totalScanTime));
        long triggerTime = calendar.getTimeInMillis() - totalScanTime;
        am.set(AlarmManager.RTC_WAKEUP, triggerTime, sender); // API 19 setExact()
        return triggerTime;
    }

    private void triggerScanProcedure() {
        setScanRunning(true);
        // acquire wakelock to prevent from suspend
        GBUtils.acquireCpuWakeLock(getBaseContext());
        // we scan GPS first then iBeacon
        sendStartScanGPSMsg();
        if (keepScaniBeacon) {
            sendStartScaniBeaconMsg();
        }
    }

    public void cancelScanAlarm(Context context) {
        log("cancelScanAlarm");
        Intent intent = new Intent(ACTION_SCANNING_START);
        PendingIntent sender = PendingIntent.getBroadcast(
        		context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void startGps(){
    	startScanTime = System.currentTimeMillis();
        //log("Time: startScanTime = " + startScanTime);
        //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener); // start gps tracking
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
    	mMyLocation.startLocation(mContext, mLocationResult);
    }

    public void stopGpsIfPossible() {
        if (mMyLocation != null){
        	mMyLocation.stopLocation();
        }
    }

    private void updateSettingPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        SharedPreferences mySharedPreferences;
        mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        autoSyncEnabled = mySharedPreferences.getBoolean("autoSync_pref", false);
        log("autoSyncEnabled= "+ autoSyncEnabled);
        keepScaniBeacon = mySharedPreferences.getBoolean("keepScanBeacon_pref", false);
        log("keepScaniBeacon= "+ keepScaniBeacon);
        dontSynciBeaconToDB = mySharedPreferences.getBoolean("dontSynciBeaconToDB_pref", false);
        log("dontSynciBeaconToDB= "+ dontSynciBeaconToDB);
        // update GPS part
        gpstracking = Integer.valueOf(mySharedPreferences.getString("gpstracking_pref", "0"));
        periodOuter = Integer.valueOf(mySharedPreferences.getString("gpsPeriodOutter_pref", "0"));
        periodInner = Integer.valueOf(mySharedPreferences.getString("gpsPeriodIntter_pref", "0"));
        gpsLocX1 = Double.valueOf(mySharedPreferences.getString("gpsx1_pref", "0.0"));
        gpsLocY1 = Double.valueOf(mySharedPreferences.getString("gpsy1_pref", "0.0"));
        gpsLocX2 = Double.valueOf(mySharedPreferences.getString("gpsx2_pref", "0.0"));
        gpsLocY2 = Double.valueOf(mySharedPreferences.getString("gpsy2_pref", "0.0"));

        // update iBeacon part
        int scanTime = Integer.valueOf(mySharedPreferences.getString("iBeaconScanningTracking_pref", "60"));
        mBeaconScanTime = scanTime * 1000;
        int beaconTimeout = Integer.valueOf(mySharedPreferences.getString("iBeaconInvalidTimeout_pref", "30"));
        mBeaconTimeout = beaconTimeout * 1000;
    }

    private void setupiBeaconScanner() {
        miScaner = new iBeaconScanManager(this.getBaseContext(), this);
    }

    private void setupHandler() {
        mHandler = new ScanHandler();
    }

    private void setupReceiver() {
        if (!isReceiverRegistered) {
            mReceiver = new ScanReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_SCANNING_START);
            filter.addAction(ACTION_SCANNING_STOP);
            this.registerReceiver(mReceiver, filter);
            isReceiverRegistered = true;
        }
    }

    public void sendStartScaniBeaconMsg(){
        log("sendStartScaniBeaconMsg");
        mHandler.removeMessages(MSG_START_SCAN_IBEACON);
        Message msg = mHandler.obtainMessage(
                MSG_START_SCAN_IBEACON, mBeaconScanTime, 0);
        msg.sendToTarget();
    }

    public void sendStartScanGPSMsg(){
        log("sendStartScanGPSMsg");
        mHandler.removeMessages(MSG_START_SCAN_GPS);
        Message msg = mHandler.obtainMessage(MSG_START_SCAN_GPS);
        msg.sendToTarget();
    }

    public void sendSetAlarmMsg(){
        log("sendSetAlarmMsg");
        mHandler.removeMessages(MSG_SET_ALARM);
        Message msg = mHandler.obtainMessage(MSG_SET_ALARM);
        msg.sendToTarget();
    }

    public void sendSyncDBMsg(){
        log("sendSyncDBMsg");
        mHandler.removeMessages(MSG_SYNC_DB_TO_SERVER);
        Message msg = mHandler.obtainMessage(MSG_SYNC_DB_TO_SERVER);
        msg.sendToTarget();
    }

    public void sendLocationFixedMsg(double latitude, double longitude) {
        mHandler.removeMessages(MSG_SCAN_TIME_OUT);

        Bundle bundle = new Bundle();
        bundle.putDouble("latitude", latitude);
        bundle.putDouble("longitude", longitude);
        Message msg = mHandler.obtainMessage(MSG_GPS_LOCATION_FIXED);
        msg.setData(bundle);
        msg.sendToTarget();
    }

    /**
     * Show a notification while this service is running.
     */
    private void showForegroundNotification() {
        CharSequence text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, GBMainActivity.class), 0);

        Notification notification = new Notification.Builder(this.getBaseContext())
                 .setTicker(text)
                 .setContentTitle("Scan Service Starts")
                 .setSmallIcon(android.R.drawable.stat_notify_more)
                 .setWhen(System.currentTimeMillis())
                 .setContentIntent(contentIntent)
                 .build();

        startForeground(NOTIFICATION, notification);
    }

    private void updateForegroundNotification(String msg, String content) {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, GBMainActivity.class), 0);

        Notification notification = new Notification.Builder(this.getBaseContext())
            .setTicker(msg)
            .setContentTitle(msg)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(contentIntent)
            .build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION, notification);
    }

    private void verifyValidiBeacons() {
        synchronized (mBeaconsObj) {
            long currTime   = System.currentTimeMillis();

            int len= miBeacons.size();
            ScanediBeacon beacon= null;

            for(int i= len- 1; 0 <= i; i--) {
                beacon= miBeacons.get(i);

                if(null != beacon && mBeaconTimeout < (currTime- beacon.lastUpdate))
                {
                	log("remove beacon too old :" + beacon.beaconUuid);
                    miBeacons.remove(i);
                }
            }
        }
    }

    public void addOrUpdateiBeacon(iBeaconData iBeacon) {
        synchronized (mBeaconsObj) {
            long currTime= System.currentTimeMillis();

            ScanediBeacon beacon= null;
            for(ScanediBeacon b : miBeacons) {
                if(b.equals(iBeacon, false)) {
                    beacon= b;
                    break;
                }
            }

            if(null == beacon) {
                beacon= ScanediBeacon.copyOf(iBeacon);
                miBeacons.add(beacon);
                log("addOrUpdateiBeacon : " + beacon.beaconUuid
                		+ ", major: " + beacon.major
                		+ ", minor: " + beacon.minor + " scanned");
                updateForegroundNotification("iBeacon found!",""
                		+ " major: " + beacon.major
                		+ " minor: " + beacon.minor
                		+ " UUID: " + beacon.beaconUuid
                		);
            } else {
                beacon.rssi= beacon.getCalibratedRssi(iBeacon.rssi);
            }
            beacon.lastUpdate= currTime;
        }
    }

    private void updateDatabase(int type, Bundle bundle) {
        if (TYPE_DATA_IBEACON == type) {
            synchronized (mBeaconsObj) {
                log("updateDatabase - TYPE_DATA_IBEACON " + miBeacons.size());

                GBDatabaseHelper dbHelper =
                        GBDatabaseHelper.getInstance(getBaseContext());
                for (ScanediBeacon beacon : miBeacons) {
                    HashMap<String, String> dataValues =
                            new HashMap<String, String>();
                    dataValues.put(GBDatabaseHelper.COLUMN_TYPE, TYPE_STRING_IBEACON);
                    dataValues.put(GBDatabaseHelper.COLUMN_DATA, "" + beacon.beaconUuid);
                    dataValues.put(GBDatabaseHelper.COLUMN_EXTRA1,
                            "major:" + beacon.major + ", minor:" + beacon.minor);
                    dataValues.put(GBDatabaseHelper.COLUMN_EXTRA2,
                            "rssi:" + beacon.getAverageRssi()
                            + ", distance:" + beacon.calDistance());

                    // use Unix time by request.
                    dataValues.put(GBDatabaseHelper.COLUMN_TIMESTAMP, Long.toString(beacon.lastUpdate));

                    dumpiBeaconData(beacon);
                    if (!dontSynciBeaconToDB) {
                    	dbHelper.insertData(dataValues);
                    }
                }
            }
        } else if (TYPE_DATA_GPS == type) {
            GBDatabaseHelper dbHelper = GBDatabaseHelper.getInstance(getBaseContext());
            HashMap<String, String> gpsValues = new HashMap<String, String>();
            String latitude = String.valueOf(bundle.getDouble("latitude"));
            String longitude = String.valueOf(bundle.getDouble("longitude"));

            gpsValues.put(GBDatabaseHelper.COLUMN_TYPE, TYPE_STRING_GPS);
            gpsValues.put(GBDatabaseHelper.COLUMN_DATA, "" + latitude + ", " + longitude);

            // use Unix time by request.
            gpsValues.put(GBDatabaseHelper.COLUMN_TIMESTAMP, Long.toString(System.currentTimeMillis()));
            dbHelper.insertData(gpsValues);
        }
    }

    private void dumpiBeaconData(ScanediBeacon beacon) {
        synchronized (mBeaconsObj) {
    	log("dumpiBeaconData : "
    			+ "UUID: " + beacon.beaconUuid
    			+ ", major: " + beacon.major
    			+ ", minor: " + beacon.minor
    			+ ", getAverageRssi: " + beacon.getAverageRssi()
    			+ ", lastUpdate: " + beacon.lastUpdate
    			+ ", oneMeterRssi: " + beacon.oneMeterRssi
    			+ ", sample: " + beacon.getSamplesNumber()
    			);
        }
	}

	private void updateGPSRegion(double Lat, double Long) {
        if(addressX[1] >= Lat && Lat >= addressX[0]
                && addressY[1] >= Long && Long >= addressY[0]) {
            mIsGPSInRegion = true;
        } else {
            mIsGPSInRegion = false;
        }
        log("updateGPSRegion - mIsGPSInRegion : " + mIsGPSInRegion);
    }

    private void updateBTRegion() {
        synchronized (mBeaconsObj) {
        	mIsBTInRegion = false;
            for (ScanediBeacon beacon : miBeacons) {
                if (checkIfSpecifiediBeacons(beacon)) {
                    mIsBTInRegion = true;
                    break;
                }
            }
        }
        log("updateBTRegion - mIsBTInRegion : " + mIsBTInRegion);
    }

    private boolean checkIfSpecifiediBeacons(ScanediBeacon beacon) {
        boolean isWatched = false;
        isWatched = GBWatchList.isiBeaconWatched(mContext,
                String.valueOf(beacon.beaconUuid) + ":" +
                String.valueOf(beacon.major) + ":" +
                String.valueOf(beacon.minor));
        log("GBWatchList getDefCount : " + GBWatchList.getDefCount());
        log("checkIfSpecifiediBeacons " + isWatched);
        return isWatched;
    }

    private boolean isInRegion() {
        log("isInRegion : mIsGPSInRegion " + mIsGPSInRegion
                + ", mIsBTInRegion " + mIsBTInRegion);
        return (mIsGPSInRegion || mIsBTInRegion);
    }


    private void getAddress() {
        addressX[0] = gpsLocX1; //use array to sort address
        addressX[1] = gpsLocX2;
        addressY[0] = gpsLocY1;
        addressY[1] = gpsLocY2;
        Arrays.sort(addressX);
        Arrays.sort(addressY);
    }

    private void endScanPeriod(boolean bSetAlarm) {
		// treat this type as end of scan procedure,
        if (autoSyncEnabled) {
            // will set alarm after sync
            sendSyncDBMsg();
        } else {
            if (bSetAlarm) {
                // set next alarm and release wakelock
                sendSetAlarmMsg();
            }
        }
    }

    private class ScanHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //log("--- handlerMessage --- " + msg.what);
            switch (msg.what) {
            case MSG_START_SCAN_GPS :
            {
                log("MSG_START_SCAN_GPS");
                updateForegroundNotification("Start GPS scanning", "");
                getAddress();
                startGps();

                // set timeout to stop GPS scanning
                Message msgStartTimeOut = new Message();
                msgStartTimeOut.what = MSG_SCAN_TIME_OUT;
                mHandler.sendMessageDelayed(msgStartTimeOut, gpstracking * 60 * 1000);
            }
                break;
            case MSG_GPS_LOCATION_FIXED:
            {
                log("MSG_GPS_LOCATION_FIXED");
                Bundle bundle = msg.getData();
                updateGPSRegion(bundle.getDouble("latitude"),
                        bundle.getDouble("longitude"));

                Message msgStop = new Message();
                msgStop.what = MSG_STOP_SCAN_GPS;
                msgStop.setData(bundle);
                mHandler.sendMessage(msgStop);
            }
                break;
            case MSG_STOP_SCAN_GPS :
            {
                log("MSG_STOP_SCAN_GPS");
                stopGpsIfPossible();

                Message msgUpdateDB = new Message();
                msgUpdateDB.what = MSG_UPDATE_DATABASE;
                msgUpdateDB.arg1 = TYPE_DATA_GPS;
                msgUpdateDB.setData(msg.getData());
                mHandler.sendMessage(msgUpdateDB);
            }
                break;
            case MSG_START_SCAN_IBEACON :
            {
                log("MSG_START_SCAN_IBEACON");
                if (!keepScaniBeacon) {
                    // no need to inform because user treat it as always enable
                    updateForegroundNotification("Start iBeacon scanning", "");
                }
                synchronized (mBeaconsObj) {
                    miBeacons.clear();
                }

                int timeForScaning = msg.arg1;
                log("MSG_START_SCAN_IBEACON - timeForScaning : " + timeForScaning);

                miScaner.startScaniBeacon(timeForScaning); //asynchronous
                this.removeMessages(MSG_STOP_SCAN_IBEACON);
                this.sendMessageDelayed(
                        this.obtainMessage(MSG_STOP_SCAN_IBEACON),
                        timeForScaning + 1000);
            }
                break;
            case MSG_STOP_SCAN_IBEACON :
            {
                log("MSG_STOP_SCAN_IBEACON");
                miScaner.stopScaniBeacon();
                verifyValidiBeacons();
                updateBTRegion();
                // update database
                this.sendMessage(this.obtainMessage(
                        MSG_UPDATE_DATABASE, TYPE_DATA_IBEACON, 0));
                if (keepScaniBeacon) {
                    sendStartScaniBeaconMsg();
                }
            }
                break;
            case MSG_STOP_ALL_SCAN :
            {
                log("MSG_STOP_ALL_SCAN");
                //TODO: do something?
            }
                break;
            case MSG_UPDATE_DATABASE :
            {
                int updateType = msg.arg1;
                log("MSG_UPDATE_DATABASE - type: " + updateType);
                updateDatabase(updateType, msg.getData());

                if (TYPE_DATA_GPS == updateType) {
                    // GPS scanning completed, start scan iBeacon
                    if (!keepScaniBeacon) {
                        // scan iBeacon only when this flag not set
                        sendStartScaniBeaconMsg();
                    } else {
                        endScanPeriod(true);// set alarm
                    }
                } else if (TYPE_DATA_IBEACON == updateType) {
                    if (!keepScaniBeacon) {
                        endScanPeriod(true);// set alarm
                    } else {
                        endScanPeriod(false);// do not set alarm
                    }
                }
            }
                break;
            case MSG_SCAN_TIME_OUT :
            {
                log("MSG_SCAN_TIME_OUT");
                // fake address 0 0
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", 0.0);
                bundle.putDouble("longitude", 0.0);

                Message msgStop = new Message();
                msgStop.what = MSG_STOP_SCAN_GPS;
                msgStop.setData(bundle);
                mHandler.sendMessage(msgStop);
            }
                break;
            case MSG_SET_ALARM :
            {
            	log("MSG_SET_ALARM");
            	long triggerTime = setupScanAlarms(mContext);
            	// calculate readable time and show on notification
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                Date resultdate = new Date(triggerTime);
                String readableDate = sdf.format(resultdate);
                log("Next scanning: "+ readableDate);
                updateForegroundNotification("Next scanning: ", readableDate);

                if (!keepScaniBeacon) {
                    GBUtils.releaseCpuWakeLock();
                }
            	break;
            }
            case MSG_SYNC_DB_TO_SERVER:
            {
                log("MSG_SYNC_DB_TO_SERVER");
                if (GBUtils.isNetworkOnline(mContext)) {
                    Message msgBack = mHandler.obtainMessage(MSG_SET_ALARM);
                    GBUtils.syncLocalDBtoServer(mContext, new ServerResponseHandler(mContext,msgBack));
                } else {
                	GBUtils.showToastIfEnabled(mContext, "Please connect to network first!");
                }
                break;
            }
            default :
            	log("Should not be here.");
            	break;
            }
        }
    }

    private class ScanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            log("onReceive : " + intent.getAction());
            if (ACTION_SCANNING_START.equals(intent.getAction())) {
                triggerScanProcedure();

            } else if(ACTION_SCANNING_STOP.equals(intent.getAction())) {
                // TODO: who should broadcast this intent?
                Message msg = mHandler.obtainMessage(MSG_STOP_ALL_SCAN);
                msg.sendToTarget();
                cancelScanAlarm(mContext);
            }
        }
    }
/*
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            log("Got location fixed: Lat = " + location.getLatitude() + " , Long = " + location.getLongitude());
            //stopGps();
            sendLocationFixedMsg(
                    location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
        	GBUtils.showToastIfEnabled(mContext, "GPS turned off");
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }*/

    private static void log(String s) {
        Log.d(TAG, s);
    }
}