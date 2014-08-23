package com.demo.gpsibeaconscanner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

// ref: http://kirill-poletaev.blogspot.tw/2013/01/android-beginner-tutorial-part-66.html
// TODO: complete this class
public class GBDatabaseHelper extends SQLiteOpenHelper implements BaseColumns{
	 private static final String DATABASE_NAME = "gbdatabase.db";
	 private static final int DATABASE_VERSION = 1;
	 
	 /** Create a helper object for the Events database */
	 public GBDatabaseHelper(Context ctx) {
		 super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	 }

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
}
