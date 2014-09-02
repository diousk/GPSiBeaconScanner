package com.demo.gpsibeaconscanner;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

// ref: http://kirill-poletaev.blogspot.tw/2013/01/android-beginner-tutorial-part-66.html
// ref: http://programmerguru.com/android-tutorial/how-to-sync-sqlite-on-android-to-mysql-db/
// ref: http://stackoverflow.com/questions/5457699/cursor-adapter-and-sqlite-example
// TODO: complete this class
public class GBDatabaseHelper extends SQLiteOpenHelper{
	 private static final String DATABASE_NAME = "gbdatabase.db";
	 private static final int DATABASE_VERSION = 2;
	 private static final String TABLE_NAME = "gbdata";
	 public static final String COLUMN_TYPE = "type";
	 public static final String COLUMN_TIMESTAMP = "timestamp";
	 public static final String COLUMN_DATA = "data";
	 public static final String COLUMN_EXTRA1 = "extra1";
	 public static final String COLUMN_EXTRA2 = "extra2";
	 public static final String COLUMN_SYNC_STATUS = "syncstatus";
	 public static final String COLUMN_DEVICE_ID = "deviceid";
	 private static GBDatabaseHelper helper;
	 private static String mDeviceId = "";
	 /** Create a helper object for the Events database */
	 public GBDatabaseHelper(Context ctx) {
		 super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	 }
	 
	 public static synchronized GBDatabaseHelper getInstance(Context context) {
		 if(helper == null) {
			 helper = new GBDatabaseHelper(context);
			 mDeviceId = Installation.id(context);
		 }
		 return helper;
	 }

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
		        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
		        + COLUMN_TYPE + " TEXT,"
		        + COLUMN_TIMESTAMP + " TEXT,"
		        + COLUMN_DATA + " TEXT,"
		        + COLUMN_EXTRA1 + " TEXT,"
		        + COLUMN_EXTRA2 + " TEXT,"
		        + COLUMN_DEVICE_ID + " TEXT,"
		        + COLUMN_SYNC_STATUS + " TEXT"
		        + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
	}

	public void insertData(HashMap<String, String> dataValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_TYPE, dataValues.get(COLUMN_TYPE));
		values.put(COLUMN_TIMESTAMP, dataValues.get(COLUMN_TIMESTAMP));
		values.put(COLUMN_DATA, dataValues.get(COLUMN_DATA));

		if (dataValues.get(COLUMN_EXTRA1) == null) {
			values.putNull(COLUMN_EXTRA1);
		} else {
			values.put(COLUMN_EXTRA1, dataValues.get(COLUMN_EXTRA1));
		}

		if (dataValues.get(COLUMN_EXTRA2) == null) {
			values.putNull(COLUMN_EXTRA2);
		} else {
			values.put(COLUMN_EXTRA2, dataValues.get(COLUMN_EXTRA2));
		}

		values.put(COLUMN_DEVICE_ID, mDeviceId);
		values.put(COLUMN_SYNC_STATUS, "no");
		database.insertOrThrow(TABLE_NAME, null, values);
		database.close();
	}

	/**
	 * Get list of data from SQLite DB as Array List
	 * @return
	 */
	public Cursor getAllDBData() {
	    //TODO: try close database
		String selectQuery = "SELECT  * FROM " + TABLE_NAME;
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    return cursor;
	}

	public int getAllDBDataCount() {
	    int count = 0;
	    String selectQuery = "SELECT  * FROM " + TABLE_NAME;
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    count = cursor.getCount();
	    database.close();
	    return count;
	}

	public int getNonSyncDBDataCount() {
	    int count = 0;
        String selectQuery = "SELECT  * FROM " + TABLE_NAME
                +" where "+COLUMN_SYNC_STATUS+" = '"+"no"+"'";
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    count = cursor.getCount();
	    database.close();
	    return count;
	}

	// this method only generate those data non-synchronized to server
	public String genJSONfromDB(){
        ArrayList<HashMap<String, String>> dbList;
        dbList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME
                +" where "+COLUMN_SYNC_STATUS+" = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(BaseColumns._ID, cursor.getString(
                        cursor.getColumnIndexOrThrow(BaseColumns._ID)));
                map.put(COLUMN_TYPE, cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                map.put(COLUMN_TIMESTAMP, cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                map.put(COLUMN_DATA, cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_DATA)));
                map.put(COLUMN_EXTRA1, cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_EXTRA1)));
                map.put(COLUMN_EXTRA2, cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_EXTRA2)));
                map.put(COLUMN_DEVICE_ID, cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_DEVICE_ID)));
                map.put(COLUMN_SYNC_STATUS, cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_SYNC_STATUS)));
                dbList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        return gson.toJson(dbList);
    }

    public void updateSyncStatus(String id, String status){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "Update " + TABLE_NAME
                +" set " + COLUMN_SYNC_STATUS +" = '"+ status +"'"
                +" where " + BaseColumns._ID + "="+"'"+ id +"'";
        database.execSQL(updateQuery);
        database.close();
    }

	public void deleteTable() {
		SQLiteDatabase database = this.getWritableDatabase();
		database.delete(TABLE_NAME, null, null);
		database.close();
	}

}
