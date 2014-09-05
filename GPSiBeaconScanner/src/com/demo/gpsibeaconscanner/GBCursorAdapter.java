package com.demo.gpsibeaconscanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class GBCursorAdapter extends ResourceCursorAdapter {
    private static final String TAG = "GBCursorAdapter";

    private static void log(String s){
        Log.d(TAG, s);
    }

    public GBCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        boolean hasSynced = "yes".equals(cursor.getString(
                cursor.getColumnIndexOrThrow(GBDatabaseHelper.COLUMN_SYNC_STATUS)));

        TextView type = (TextView) view.findViewById(R.id.text_data_type);
        type.setText(cursor.getString(
                cursor.getColumnIndexOrThrow(GBDatabaseHelper.COLUMN_TYPE)));
        if (hasSynced) {
            type.setTextColor(Color.GRAY);
        } else {
            type.setTextColor(Color.BLACK);
        }

        TextView data = (TextView) view.findViewById(R.id.text_data);
        data.setText(cursor.getString(
                cursor.getColumnIndexOrThrow(GBDatabaseHelper.COLUMN_DATA)));

        TextView extra1 = (TextView) view.findViewById(R.id.text_extra1);
        extra1.setText(cursor.getString(
                cursor.getColumnIndexOrThrow(GBDatabaseHelper.COLUMN_EXTRA1)));
        TextView extra1Title = (TextView) view.findViewById(R.id.text_title_extra1);
        extra1Title.setVisibility(View.VISIBLE);
        extra1.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(extra1.getText())) {
            extra1Title.setVisibility(View.GONE);
            extra1.setVisibility(View.GONE);
        }

        TextView time = (TextView) view.findViewById(R.id.text_timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        long timestamp = Long.valueOf(cursor.getString(
                cursor.getColumnIndexOrThrow(GBDatabaseHelper.COLUMN_TIMESTAMP)));
        Date resultdate = new Date(timestamp);
        String readableDate = sdf.format(resultdate);
        time.setText(readableDate);
    }
}