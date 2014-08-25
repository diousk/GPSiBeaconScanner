package com.demo.gpsibeaconscanner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.THLight.USBeacon.App.Lib.iBeaconData;
import com.THLight.USBeacon.App.Lib.iBeaconScanManager;

public class GBScanService extends Service implements iBeaconScanManager.OniBeaconScan{
	private final static String TAG = "GBScanService";
	private Context mContext;
	private int NOTIFICATION = R.string.local_service_started;
	private static boolean isReceiverRegistered = false;
	private ScanHandler mHandler;
	private ScanReceiver mReceiver;
	private GBiBeacon mGBiBeacon;
	private AlarmManager am;
	private Boolean isInRegion = false;
    private LocationManager mLocationManager; 
    private MyLocationListener mLocationListener;
	
	// iBeacon component
	private iBeaconScanManager miScaner	= null;
	private List<ScanediBeacon> miBeacons	= new ArrayList<ScanediBeacon>();
	private Object mBeaconsObj = new Object();
	
	private int mEachScanInterval = 30000; //default 30s,  but needless???
	private int mEachScanTime = 5000; //default 5s
	private int mBeaconTimeout = 10000; //default 10s, means invalid if too old
	
	private final static String ACTION_SCANNING_START = "scanning.start"; // should be trigger by alarm manager
	private final static String ACTION_SCANNING_STOP = "scanning.stop"; // should be trigger by alarm manager
	private final static String ACTION_FIX_LOCATION = "getfix.gps.location";
	private final static String ACTION_ENTER_REGION = "enter.setting.region";
	private final static String ACTION_EXIT_REGION = "exit.setting.region";
	
	private final static String ACTION_DB_UPDATED = "db.updated";
	private final static int MSG_START_SCAN_GPS = 2001;
	private final static int MSG_START_SCAN_IBEACON = 2002;
	private final static int MSG_STOP_SCAN_GPS = 2003;
	private final static int MSG_STOP_SCAN_IBEACON = 2004;
	private final static int MSG_STOP_ALL_SCAN = 2005;
	private final static int MSG_UPDATE_DATABASE = 2006;
	private final static int MSG_SCAN_TIME_OUT = 2007;
	private final static int MSG_CHECK_REGION = 2008;

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
		
		showForegroundNotification();
		setupReceiver();
		setupHandler();
		setupiBeaconScanner();
		updateSettingPreferences();
		
        String data = intent.getStringExtra("mode");
        Log.d(TAG, "data = " + data);
        
        if(data != null && data.equals("intentNextStart")) {
            Message mNextStart = new Message();
            mNextStart.what = MSG_START_SCAN_GPS;
            mHandler.sendMessage(mNextStart);
        }

		//Should it start scanning when first time service starts?
		sendStartScaniBeaconMsg();
		return super.onStartCommand(intent, flags, startId);
	}
	
    public void startGps(){
        Log.d(TAG, "startGps");
        mGBiBeacon = (GBiBeacon) getApplication();
        
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener); // start gps tracking
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        GBUtils.acquireCpuWakeLock(getBaseContext());
    }

    public void stopGps() {
        Log.d(TAG, "stopGps");
        mLocationManager.removeUpdates(mLocationListener);
        isInRegion = false;
        GBUtils.releaseCpuWakeLock();
    }

	private void updateSettingPreferences() {
		// TODO: get SharedPreferences
	    //mEachScanTime, mEachScanInterval
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
			filter.addAction(ACTION_FIX_LOCATION);
			filter.addAction(ACTION_ENTER_REGION);
			filter.addAction(ACTION_EXIT_REGION);
			this.registerReceiver(mReceiver, filter);
			isReceiverRegistered = true;
		}
	}
	
	public void sendStartScaniBeaconMsg(){
		Message msg = mHandler.obtainMessage(
				MSG_START_SCAN_IBEACON, mEachScanTime, mEachScanInterval);
		msg.sendToTarget();
	}

	@Override
	public void onDestroy() {
		stopForeground(true);

		if (isReceiverRegistered) {
			this.unregisterReceiver(mReceiver);
			isReceiverRegistered = false;
		}

		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		GBUtils.releaseCpuWakeLock();
	}
	
	@Override
	public void onScaned(iBeaconData iBeacon) {
	    addOrUpdateiBeacon(iBeacon);
	}

    /**
     * Show a notification while this service is running.
     */
    private void showForegroundNotification() {
        CharSequence text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
/*        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, LocalServiceActivities.Controller.class), 0);*/

        Notification notification = new Notification.Builder(this.getBaseContext())
        	     .setTicker(text)
        	     .setSmallIcon(android.R.drawable.stat_notify_more)
        	     .setWhen(System.currentTimeMillis())
        	     .build();

        startForeground(NOTIFICATION, notification);
    }

    private void verifyValidiBeacons() {
    	synchronized (mBeaconsObj) {
			long currTime	= System.currentTimeMillis();

			int len= miBeacons.size();
			ScanediBeacon beacon= null;

			for(int i= len- 1; 0 <= i; i--) {
				beacon= miBeacons.get(i);
				
				if(null != beacon && mBeaconTimeout < (currTime- beacon.lastUpdate))
				{
					miBeacons.remove(i);
				}
			}
    	}
    }

	public void addOrUpdateiBeacon(iBeaconData iBeacon) {
		//log("addOrUpdateiBeacon - rssi=" + iBeacon.rssi + ", uuid=" + iBeacon.beaconUuid);
		synchronized (mBeaconsObj) {
			long currTime= System.currentTimeMillis();

			ScanediBeacon beacon= null;
			for(ScanediBeacon b : miBeacons) {
				if(b.equals(iBeacon, false)) {
					beacon= b;
					break;
				}
			}

			//TODO : filter only specified UUIDs, if not, just return;
			log("beacon : " + iBeacon.beaconUuid + ", rssi: " + iBeacon.rssi + " scanned");
			if(null == beacon) {
				beacon= ScanediBeacon.copyOf(iBeacon);
				miBeacons.add(beacon);
			} else {
				beacon.rssi= beacon.getCalibratedRssi(iBeacon.rssi);
			}
			log("beacon : getCalibratedRssi : " + beacon.rssi);
			beacon.lastUpdate= currTime;
		}
	}

	private void updateDatabase(int type) {
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
					        "major:" + beacon.major + " minor:" + beacon.minor);
					dataValues.put(GBDatabaseHelper.COLUMN_EXTRA2,
					        "rssi:" + beacon.getAverageRssi()
					        + " distance:" + beacon.calDistance());

					SimpleDateFormat sdf = new SimpleDateFormat(
					        "yyyy/MM/dd HH:mm:ss", Locale.getDefault());
					Date resultdate = new Date(beacon.lastUpdate);
					dataValues.put(GBDatabaseHelper.COLUMN_TIMESTAMP, sdf.format(resultdate));

					dbHelper.insertData(dataValues);
				}
			}
		} else if (TYPE_DATA_GPS == type) {
			// TODO: else if gps data
		}
	}

    private class ScanHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			log("--- handlerMessage --- " + msg.what);
			switch (msg.what) {
			case MSG_START_SCAN_GPS :
			{
				// TODO: complete this
				log("MSG_START_SCAN_GPS");
				startGps();
			}
				break;
			case MSG_START_SCAN_IBEACON :
			{
				log("MSG_START_SCAN_IBEACON");
				synchronized (mBeaconsObj) {
				    miBeacons.clear();
				}

				int timeForScaning = msg.arg1;
				miScaner.startScaniBeacon(timeForScaning); //asynchronous
				this.sendMessageDelayed(
						this.obtainMessage(MSG_STOP_SCAN_IBEACON),
						timeForScaning + 1000);
			}
				break;
			case MSG_STOP_SCAN_GPS :
			{
                // TODO: complete this
				log("MSG_STOP_SCAN_GPS");
				stopGps();
			}
				break;
			case MSG_STOP_SCAN_IBEACON :
			{
				log("MSG_STOP_SCAN_IBEACON");
				miScaner.stopScaniBeacon();
				// TODO: verify valid iBeacon (some too old)
				verifyValidiBeacons();
				// update database
				this.sendMessage(this.obtainMessage(
						MSG_UPDATE_DATABASE, TYPE_DATA_IBEACON, 0));
			}
				break;
			case MSG_STOP_ALL_SCAN :
			{
				log("MSG_STOP_ALL_SCAN");
			}
				break;
			case MSG_UPDATE_DATABASE :
			{
				log("MSG_UPDATE_DATABASE");
				// TODO: implement update database
				int updateType = msg.arg1;
				updateDatabase(updateType);

				if (TYPE_DATA_GPS == updateType) {
				    // GPS scanning completed, start scan iBeacon

				} else if (TYPE_DATA_IBEACON == updateType) {
                    // treat this type as end of scan procedure,
    				// broadcast ACTION_SCANNING_COMPLETED
                    Intent intent = new Intent(ACTION_DB_UPDATED);
                    mContext.sendBroadcast(intent);
                    GBUtils.releaseCpuWakeLock();
				}
			}
				break;
			case MSG_SCAN_TIME_OUT :
			{
				log("MSG_SCAN_TIME_OUT"); 
				stopGps();
			}
				break;
			case MSG_CHECK_REGION:
			{
				log("MSG_CHECK_REGION");
				
			}
				break;
			}
			
		}
    }

    private class ScanReceiver extends BroadcastReceiver {
    	private double mLat, mLong, mAcc, mBear, mSpeed, mTime;
    	
		@Override
		public void onReceive(Context context, Intent intent) {
			log("onReceive : " + intent.getAction());
			mGBiBeacon = (GBiBeacon) getApplication();
			if (ACTION_SCANNING_START.equals(intent.getAction())) {
				
                Message mStart = new Message();
                mStart.what = MSG_START_SCAN_GPS;
                mHandler.sendMessage(mStart);
                    
                Message mStartTimeOut = new Message();
                mStartTimeOut.what = MSG_SCAN_TIME_OUT;
                mHandler.sendMessageDelayed(mStartTimeOut, mGBiBeacon.gpstracking * 60 * 1000);
                    
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                    
                if (isInRegion) {
                    calendar.add(Calendar.MINUTE, mGBiBeacon.gpsPeriodIntter);
                } else {
                    calendar.add(Calendar.MINUTE, mGBiBeacon.gpsPeriodOutter);
                }
                    
                am = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent intentNextStart = new Intent();
                intentNextStart.putExtra("mode", "intentNextStart");
                intentNextStart.setClass(context, GBScanService.class);
                PendingIntent sender = PendingIntent.getService(context, 0, intentNextStart, PendingIntent.FLAG_UPDATE_CURRENT);
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender); // API 19 setExact()
                
			    // TODO: wait GPS scanning completed and then start scan iBeacon
				sendStartScaniBeaconMsg(); 
				
			} else if(ACTION_SCANNING_STOP.equals(intent.getAction())) {
                Message m = new Message();
                m.what = MSG_STOP_SCAN_GPS;
                mHandler.sendMessage(m);
                    
                Intent intentStop = new Intent();
                intentStop.putExtra("mode", "intentStop");
                intentStop.setClass(context, GBScanService.class);
                PendingIntent sender = PendingIntent.getService(context, 0, intentStop, PendingIntent.FLAG_UPDATE_CURRENT);
                am.cancel(sender);
			} else if(ACTION_FIX_LOCATION.equals(intent.getAction())) {
                mLat = intent.getDoubleExtra("Lat", 0.0);
                mLong = intent.getDoubleExtra("Long", 0.0);
                    
                Log.d(TAG, "Lat: " + mLat + ", Long: " + mLong);
                
                Message mStop = new Message();
                mStop.what = MSG_STOP_SCAN_GPS;
                mHandler.sendMessage(mStop);
                
                Message mCheckRegion = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putString("Lat", String.valueOf(mLat));
                mBundle.putString("Long", String.valueOf(mLong));
                mCheckRegion.setData(mBundle);
                mCheckRegion.what = 1;
                mHandler.sendMessage(mCheckRegion);
                
                
            } else if("ACTION_ENTER_REGION".equals(intent.getAction())) {
            	log("ACTION_ENTER_REGION");
                isInRegion = true;
            } else if ("ACTION_EXIT_REGION".equals(intent.getAction())) {
            	log("ACTION_EXIT_REGION");
                isInRegion = false;
            }   
		}
    }
    
    private class MyLocationListener implements LocationListener {

		@Override
        public void onLocationChanged(Location location) {
        	log("onLocationChanged: Lat = " + location.getLatitude() + " , Long = " + location.getLongitude());
        	log("Got location fixed, stop gps");
            stopGps();
             
            Intent intentFixLocation = new Intent();
            intentFixLocation.setAction("getfix.gps.location");
            intentFixLocation.putExtra("Lat", location.getLatitude());
            intentFixLocation.putExtra("Long", location.getLongitude());
            sendBroadcast(intentFixLocation);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
    } 

    private static void log(String s) {
    	Log.d(TAG, s);
    }
    

}