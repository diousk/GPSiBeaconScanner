/** ============================================================== */
package com.demo.gpsibeaconscanner;
import java.util.LinkedList;

import android.util.Log;

import com.THLight.USBeacon.App.Lib.iBeaconData;
/** ============================================================== */

/** ============================================================== */
public class ScanediBeacon extends iBeaconData
{
	public long lastUpdate= 0;
	private Object mRssiObj = new Object();
	LinkedList<Byte> mRssiRecv = new LinkedList<Byte>();
	private int MAX_QUEUE_LENGTH = 100;
	
	/** ================================================ */
	public static ScanediBeacon copyOf(iBeaconData iBeacon)
	{
		ScanediBeacon newBeacon	= new ScanediBeacon();
		
		newBeacon.beaconUuid	= iBeacon.beaconUuid;
		newBeacon.major			= iBeacon.major;
		newBeacon.minor			= iBeacon.minor;
		newBeacon.oneMeterRssi	= iBeacon.oneMeterRssi;
		newBeacon.rssi			= iBeacon.rssi;
		newBeacon.lastUpdate	= 0;
		
		return newBeacon;
	}
	
	/** ================================================ */
	public static ScanediBeacon copyOf(ScanediBeacon scanBeacon)
	{
		ScanediBeacon newBeacon	= new ScanediBeacon();
		
		newBeacon.beaconUuid	= scanBeacon.beaconUuid;
		newBeacon.major			= scanBeacon.major;
		newBeacon.minor			= scanBeacon.minor;
		newBeacon.oneMeterRssi	= scanBeacon.oneMeterRssi;
		newBeacon.rssi			= scanBeacon.rssi;
		newBeacon.lastUpdate	= scanBeacon.lastUpdate;
		
		return newBeacon;
	}
	
	public byte getCalibratedRssi(Byte rssiNew) {
		byte avgRssi = 0;
		synchronized(mRssiObj)
		{
			int sum = 0;
			mRssiRecv.add(rssiNew);
			if (mRssiRecv.size() > MAX_QUEUE_LENGTH) {
				mRssiRecv.remove();
			}
			for (Byte rssi : mRssiRecv) {
				sum += rssi;
			}
			avgRssi = (byte) (sum/mRssiRecv.size());
		}
		return avgRssi;
	}

	public byte getAverageRssi() {
		byte avgRssi = 0;
		synchronized(mRssiObj)
		{
			int sum = 0;
			for (Byte rssi : mRssiRecv) {
				sum += rssi;
			}
			avgRssi = (byte) (sum/mRssiRecv.size());
		}
		return avgRssi;
	}

	public void resetRecentRssi() {
		synchronized(mRssiObj)
		{
			mRssiRecv.clear();
		}
	}

	public int getSamplesNumber() {
		synchronized(mRssiObj)
		{
			return mRssiRecv.size();
		}
	}
}

/** ============================================================== */

