package com.demo.gpsibeaconscanner;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class GBCursorAdapter extends ResourceCursorAdapter {

    public GBCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView type = (TextView) view.findViewById(R.id.text_data_type);
        type.setText(cursor.getString(
                cursor.getColumnIndexOrThrow(GBDatabaseHelper.COLUMN_TYPE)));

        TextView data = (TextView) view.findViewById(R.id.text_data);
        data.setText(cursor.getString(
                cursor.getColumnIndexOrThrow(GBDatabaseHelper.COLUMN_DATA)));
        
        TextView time = (TextView) view.findViewById(R.id.text_timestamp);
        time.setText(cursor.getString(
                cursor.getColumnIndexOrThrow(GBDatabaseHelper.COLUMN_TIMESTAMP)));
    }
}