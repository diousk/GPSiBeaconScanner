package com.demo.gpsibeaconscanner;

import java.util.HashMap;

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
	 private static final int DATABASE_VERSION = 1;
	 private static final String TABLE_NAME = "gbdata";
	 public static final String COLUMN_TYPE = "type";
	 public static final String COLUMN_TIMESTAMP = "timestamp";
	 public static final String COLUMN_DATA = "data";
	 public static final String COLUMN_EXTRA1 = "extra1";
	 public static final String COLUMN_EXTRA2 = "extra2";
	 private static GBDatabaseHelper helper;
	 /** Create a helper object for the Events database */
	 public GBDatabaseHelper(Context ctx) {
		 super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	 }
	 
	 public static synchronized GBDatabaseHelper getInstance(Context context) {
		 if(helper == null) {
			 helper = new GBDatabaseHelper(context);
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
		        + COLUMN_EXTRA2 + " TEXT"
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
		database.insertOrThrow(TABLE_NAME, null, values);
		database.close();
	}

	/**
	 * Get list of data from SQLite DB as Array List
	 * @return
	 */
	public Cursor getAllDBData() {
		String selectQuery = "SELECT  * FROM " + TABLE_NAME;
	    SQLiteDatabase database = this.getWritableDatabase();
	    Cursor cursor = database.rawQuery(selectQuery, null);
	    return cursor;
	}

	public void deleteTable() {
		SQLiteDatabase database = this.getWritableDatabase();
		database.delete(TABLE_NAME, null, null);
	}

}
