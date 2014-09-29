package com.demo.gpsibeaconscanner;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayActivity extends Activity {
    ImageView mImage;
    TextView mText;
    String mID;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_layout);
        mID = getIntent().getStringExtra("idDisplay");

        mImage = (ImageView)findViewById(R.id.imageDisaply);
        mText = (TextView)findViewById(R.id.textDisplay);
        loadDataFromAsset(mID);
    }
 
    public void loadDataFromAsset(String id) {
    	if (TextUtils.isEmpty(mID)) return;
        // load text
        try {
            // get input stream for text
            InputStream is = getAssets().open(id + ".txt");
            // check size
            int size = is.available();
            // create buffer for IO
            byte[] buffer = new byte[size];
            // get data to buffer
            is.read(buffer);
            // close stream
            is.close();
            // set result to TextView
            mText.setText(new String(buffer));
        }
        catch (IOException ex) {
            return;
        }
 
        // load image
        try {
            // get input stream
            InputStream ims = getAssets().open(id + ".jpg");
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            mImage.setImageDrawable(d);
        }
        catch(IOException ex) {
            return;
        }
 
    }
}