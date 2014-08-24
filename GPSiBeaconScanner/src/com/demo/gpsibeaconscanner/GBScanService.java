package com.demo.gpsibeaconscanner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	
	// iBeacon component
	private iBeaconScanManager miScaner	= null;
	private List<ScanediBeacon> miBeacons	= new ArrayList<ScanediBeacon>();
	private Object mBeaconsObj = new Object();
	
	private int mEachScanInterval = 30000; //default 30s,  but needless???
	private int mEachScanTime = 5000; //default 5s
	private int mBeaconTimeout = 10000; //default 10s, means invalid if too old
	
	private final static String ACTION_SCANNING_START = "scanning.start"; // should be trigger by alarm manager
	private final static String ACTION_DB_UPDATED = "db.updated";
	private final static int MSG_START_SCAN_GPS = 2001;
	private final static int MSG_START_SCAN_IBEACON = 2002;
	private final static int MSG_STOP_SCAN_GPS = 2003;
	private final static int MSG_STOP_SCAN_IBEACON = 2004;
	private final static int MSG_STOP_ALL_SCAN = 2005;
	private final static int MSG_UPDATE_DATABASE = 2006;

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
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log("onStartCommand - intent " + intent);
		mContext = this.getBaseContext();
		showForegroundNotification();
		setupReceiver();
		setupHandler();
		setupiBeaconScanner();
		updateSettingPreferences();

		//Should it start scanning when first time service starts?
		sendStartScaniBeaconMsg();
		return super.onStartCommand(intent, flags, startId);
	}

	private void updateSettingPreferences() {
		// TODO: get SharedPreferences
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
			{
				long currTime	= System.currentTimeMillis();
				
				int len= miBeacons.size();
				ScanediBeacon beacon= null;
				
				for(int i= len- 1; 0 <= i; i--)
				{
					beacon= miBeacons.get(i);
					
					if(null != beacon && mBeaconTimeout < (currTime- beacon.lastUpdate))
					{
						miBeacons.remove(i);
					}
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
		// TODO: if type is ibeacon data
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
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
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
			switch (msg.what) {
			case MSG_START_SCAN_GPS :
			{
				// TODO: complete this
				log("MSG_START_SCAN_GPS");
			}
				break;
			case MSG_START_SCAN_IBEACON :
			{
				log("MSG_START_SCAN_IBEACON");
				// TODO: clean up miBeacons list before start scanning
				miBeacons.clear();
				int timeForScaning = msg.arg1;
				miScaner.startScaniBeacon(timeForScaning); //asynchronous
				this.sendMessageDelayed(
						this.obtainMessage(MSG_STOP_SCAN_IBEACON),
						timeForScaning + 1000);
			}
				break;
			case MSG_STOP_SCAN_GPS :
			{
				log("MSG_STOP_SCAN_GPS");
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

				// broadcast ACTION_SCANNING_COMPLETED
				Intent intent = new Intent(ACTION_DB_UPDATED);
				mContext.sendBroadcast(intent);
			}
				break;
			}
		}
    }

    private class ScanReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			log("onReceive : " + intent.getAction());
			if (ACTION_SCANNING_START.equals(intent.getAction())) {
				sendStartScaniBeaconMsg(); // should wait gps scanning completed?
				// TODO: send start gps scan message (should wait ibeacon scanning completed?)
			}
		}
    }

    private static void log(String s) {
    	Log.d(TAG, s);
    }
}