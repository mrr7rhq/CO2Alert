package com.example.co2emissionalert;

//import com.example.co2emissionalert.MapTracking.MarkerOverlay;
//import com.google.android.maps.GeoPoint;

import java.io.File;

import com.example.co2emissionalert.MapTracking.LocEntry;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class SummaryActivity extends Activity{

	private long STime;
	private double SDistance;
	private double SCO2;
	private DBHandler db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_summary);
		final TextView tsummary = (TextView)findViewById(R.id.tsummaryId);
		ExitActivity.isApplicationTerminated = false;
		db = new DBHandler(this);
		
		/*Intent i = getIntent();
		LocEntry finalEntry = (LocEntry) i.getSerializableExtra("finalObj");*/
		
		Bundle extras = getIntent().getExtras(); 
        if(extras != null){
        	STime = extras.getLong("STime");	// millisecondes
            Log.d("sum", "Time got: " + String.valueOf(STime));	// DEBUG log message
            
            SDistance = extras.getDouble("SDistance", 0f);
            Log.d("sum", "Distance got: " + String.valueOf(SDistance));	// DEBUG log message
        
            SCO2 = extras.getDouble("SCO2", 0f);
            Log.d("sum", "CO2 got: " + String.valueOf(SCO2));	// DEBUG log message
        }
        
		
        int sec = (int) (STime / 1000);
		int min = sec / 60; sec %= 60;
		int hour = min / 60; min %= 60;
		double aSpeed = (double)Math.round(SDistance/STime*1000*100)/100;
		double cRate = (double)Math.round(SCO2/STime*1000*100)/100;
		Log.d("sum", "CO2 rate: " + String.valueOf(cRate));	// DEBUG log message
		
        tsummary.setText("Your Trip Conclusion: \n\n" + "\t1.Elapsed time: " + String.valueOf(hour) + "h " 
        		+ String.valueOf(min) + "min " + String.valueOf(sec) + "s\n\n"
        		+ "\t2.Total distance: " + String.valueOf(SDistance) + "m\n\n"
        		+ "\t3.Total CO2 Emission: " + String.valueOf(SCO2) + "g\n\n"
        		+ "\t4.Average speed: " + String.valueOf(aSpeed) + "m/s\n\n"
        		+ "\t5.Average rate of CO2 Emission: " + String.valueOf(cRate) + "g/s\n");
	}
	
	@Override
	protected void onResume(){
		if(ExitActivity.isApplicationTerminated){
			finish();
		}
		super.onResume();
	}
	
	 @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 getMenuInflater().inflate(R.menu.activity_summary, menu);
		 	return true;
	}
	    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	        
		 switch (item.getItemId()) {
		 	case R.id.history:
		 		File dbFile = getDatabasePath("mytrack.db");
		 		Log.d("database", dbFile.getAbsolutePath());
	        return true;
	        
	        case R.id.extract:
	        	
	        return true;
	        
	        case R.id.exit:
	        	Intent intent = new Intent();
	        	//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	intent.setClass(SummaryActivity.this, ExitActivity.class);
				startActivityForResult(intent, 0);	// trigger ExitActivity
	        	
	        return true;
	        
	        default:
	        return super.onOptionsItemSelected(item);
		 }
	}
	
	
}
