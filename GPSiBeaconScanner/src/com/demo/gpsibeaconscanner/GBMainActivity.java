package com.demo.gpsibeaconscanner;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

public class GBMainActivity extends Activity {

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
		private ListView mLVBLE= null;
		private Button mBtnDeleteDB;
		
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
			// show db data list if exists
			GBDatabaseHelper sqLiteOpenHelper = new GBDatabaseHelper(this.getActivity());
			
			mLVBLE = (ListView)(this.getView().findViewById(R.id.data_list));
			GBCursorAdapter adapter = new GBCursorAdapter(
			        this.getActivity(), R.layout.data_list_row, sqLiteOpenHelper.getAllDBData(),0);
			mLVBLE.setAdapter(adapter);

			mBtnDeleteDB = (Button)(this.getView().findViewById(R.id.btn_delete_db));
			mBtnDeleteDB.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					GBDatabaseHelper.getInstance(getActivity()).deleteTable();
				}
			});
		}

		@Override
		public void onStart() {
			super.onStart();
			Intent i = new Intent(this.getActivity(), GBScanService.class);
			this.getActivity().startService(i);
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

}
