package com.demo.gpsibeaconscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GBMainActivity extends Activity {
    private final static String TAG = "GBMain";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gbmain);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new GBMainFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gbmain, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
        	Intent intent = new Intent();
        	intent.setClass(this, GBPreferences.class);
        	startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class GBMainFragment extends Fragment {
		private ListView mListView= null;
		private Button mBtnDeleteDB, mBtnUpdateResult;
		private ToggleButton mBtnScan;
		private GBCursorAdapter mListAdapter;
		private Context mContext;
		private final static int REQ_ENABLE_BT = 1001;
		private final static int REQ_ENABLE_GPS = 1002;
		private boolean gpsEnable = false;
		private LocationManager mLocationManager;
		public GBMainFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_gbmain,
					container, false);
			return rootView;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			setupViews();
			if (checkBTEnabled()) {
			    // BT on, check GPS
				checkGPSEnabled();
			}
		}

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            log("onActivityResult() " + requestCode);
            switch(requestCode) {
                case REQ_ENABLE_BT:
                    if(RESULT_OK == resultCode) {
                        log("REQ_ENABLE_BT - RESULT_OK");
                        checkGPSEnabled();
                    }
                    break;
                case REQ_ENABLE_GPS:
        	        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        	        gpsEnable = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        	        log("Gps setting: " + gpsEnable);
                    break;
            }
        }

		private boolean checkBTEnabled() {
		    BluetoothAdapter mBLEAdapter= BluetoothAdapter.getDefaultAdapter();
		    if(!mBLEAdapter.isEnabled()) {
		        Intent intent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		        startActivityForResult(intent, REQ_ENABLE_BT);
		        return false;
		    }
		    return true;
        }
		
		private void checkGPSEnabled() {
	        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	        boolean gpsEnable = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	        
	        if(!gpsEnable) {
	            new AlertDialog.Builder(mContext)
	                .setIcon(android.R.drawable.ic_menu_mapmode)
	                .setTitle("GPS Setting")
	                .setMessage("GPS is off state. Please turn on GPS.")
	                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which1) {
	                    	startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQ_ENABLE_GPS);
	                    }
	                })
	                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                        Toast.makeText(mContext, "Can not use gps function", Toast.LENGTH_SHORT).show();
	                    }
	                }).show();
	        }
        }

        private void setupViews() {
		    mContext = this.getActivity();
            // show db data list if exists
            GBDatabaseHelper dbHelper =
                    GBDatabaseHelper.getInstance(this.getActivity().getBaseContext());
            mListView = (ListView)(this.getView().findViewById(R.id.data_list));
            mListAdapter = new GBCursorAdapter(
                    this.getActivity(), R.layout.data_list_row, dbHelper.getAllDBData(),0);
            mListView.setAdapter(mListAdapter);

            // buttons
            mBtnScan = (ToggleButton)(this.getView().findViewById(R.id.btn_scan));
            mBtnScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Intent i = new Intent(mContext, GBScanService.class);
                        mContext.startService(i);

                        // this intent should be broadcasted by alarm manager
                        /*Intent intentStartScan = new Intent("scanning.start");
                        mContext.sendBroadcast(intentStartScan);*/
                    } else {
                        Intent i = new Intent(mContext, GBScanService.class);
                        mContext.stopService(i);

                        // this intent should be broadcasted by alarm manager
                        /*
                        Intent intentStartScan = new Intent("scanning.stop");
                        mContext.sendBroadcast(intentStartScan);
                        */
                    }
                }
            });

            mBtnDeleteDB = (Button)(this.getView().findViewById(R.id.btn_delete_db));
            mBtnDeleteDB.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    GBDatabaseHelper.getInstance(getActivity()).deleteTable();
                }
            });

            mBtnUpdateResult = (Button)(this.getView().findViewById(R.id.btn_update_result));
            mBtnUpdateResult.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    mListAdapter.changeCursor(
                            GBDatabaseHelper.getInstance(getActivity()).getAllDBData());
                    mListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
		public void onStart() {
			super.onStart();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
		}

		@Override
		public void onPause() {
			super.onPause();
		}

		@Override
		public void onResume() {
			super.onResume();
		}
		
	}

	private static void log(String s) {
	    Log.d(TAG, s);
	}
}
