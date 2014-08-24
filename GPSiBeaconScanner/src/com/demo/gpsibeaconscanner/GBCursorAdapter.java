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
        TextView type = (TextView) view.findViewById(R.id.data_text1);
        type.setText(cursor.getString(cursor.getColumnIndex(GBDatabaseHelper.COLUMN_TYPE)));

        TextView data = (TextView) view.findViewById(R.id.data_text2);
        data.setText(cursor.getString(cursor.getColumnIndex(GBDatabaseHelper.COLUMN_DATA)));
        
        TextView time = (TextView) view.findViewById(R.id.data_text3);
        time.setText(cursor.getString(cursor.getColumnIndex(GBDatabaseHelper.COLUMN_TIMESTAMP)));
    }
}