package com.demo.gpsibeaconscanner;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

public class ServerResponseHandler extends AsyncHttpResponseHandler{

    private final static String TAG = "GBServerResponseHandler";
    private Context mContext;
    private Message mMessage = null;
    public ServerResponseHandler(Context ctx){
        mContext = ctx;
    }
    public ServerResponseHandler(Context ctx, Message msg){
        mContext = ctx;
        mMessage = msg;
    }
    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        log("onSuccess: " + new String(responseBody));
        try {
            GBDatabaseHelper dbHelper =
                    GBDatabaseHelper.getInstance(mContext);
            JSONArray arr = new JSONArray(new String(responseBody));
            for(int i=0; i<arr.length();i++){
                JSONObject obj = (JSONObject)arr.get(i);
                log("_id : "+obj.get("_id"));
                log("syncstatus : "+obj.get("syncstatus"));
                dbHelper.updateSyncStatus(obj.get("_id").toString(),obj.get("syncstatus").toString());
            }
            Toast.makeText(mContext, "DB Sync completed!", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            log(e.toString());
            Toast.makeText(mContext, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
        }

        if (mMessage != null) {
            mMessage.sendToTarget();
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
            Throwable error) {
        log("onFailure: " + statusCode + error.getCause());
        Toast.makeText(mContext, "Failed to sync", Toast.LENGTH_LONG).show();

        if (mMessage != null) {
            mMessage.sendToTarget();
        }
    }

    private static void log(String s) {
        Log.d(TAG , s);
    }
}
