package com.example.co2emissionalert;

//import com.example.co2emissionalert.MapTracking.MarkerOverlay;
//import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.androidplot.Plot;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.series.XYSeries;
import com.androidplot.xy.*;
import com.example.co2emissionalert.DBHandler.ArrayPair;

import java.lang.reflect.Array;
import java.text.*;
import java.util.Arrays;
import java.util.Date;

public class SummaryActivity extends Activity{

	private long STime;
	private double SDistance;
	private double SCO2;
	private DBHandler db;
	private XYPlot myXYPlot;
	private TextView tsummary;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_summary);
		myXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
		tsummary = (TextView)findViewById(R.id.tsummaryId);
		ExitActivity.isApplicationTerminated = false;
		
		Bundle extras = getIntent().getExtras(); 
        if(extras != null){
        	STime = extras.getLong("STime");	// millisecondes
            Log.d("sum", "Time got: " + String.valueOf(STime));	// DEBUG log message
            
            SDistance = extras.getDouble("SDistance", 0f);
            Log.d("sum", "Distance got: " + String.valueOf(SDistance));	// DEBUG log message
        
            SCO2 = extras.getDouble("SCO2", 0f);
            Log.d("sum", "CO2 got: " + String.valueOf(SCO2));	// DEBUG log message
        }
		
        db = new DBHandler(this);
        ArrayPair arr =  db.getArrays();
        Number[] timestamps = arr.getArray1();
        Number[] samples = arr.getArray2();
        //Number[] samples = arr[1];
        // create our series from array:
        XYSeries series = new SimpleXYSeries(Arrays.asList(timestamps), Arrays.asList(samples), "delta-CO2");
         
        myXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        myXYPlot.getGraphWidget().getGridLinePaint().setColor(Color.BLACK);
        myXYPlot.getGraphWidget().getGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1,1}, 1));
        myXYPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        myXYPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
 
        myXYPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        myXYPlot.getBorderPaint().setStrokeWidth(1);
        myXYPlot.getBorderPaint().setAntiAlias(false);
        myXYPlot.getBorderPaint().setColor(Color.WHITE);
        
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.WHITE, Color.GREEN, Shader.TileMode.MIRROR));
 
        LineAndPointFormatter formatter  = new LineAndPointFormatter(Color.rgb(0, 0,0), Color.BLUE, Color.RED);
        formatter.setFillPaint(lineFill);
        myXYPlot.getGraphWidget().setPaddingRight(2);
        myXYPlot.addSeries(series, formatter);
 
        myXYPlot.setDomainLabel("Time");
        myXYPlot.setRangeLabel("CO2 Rate g/s");
        myXYPlot.setTicksPerDomainLabel(60000);
        
        myXYPlot.setRangeValueFormat(new DecimalFormat("0.0"));
        
        
		myXYPlot.disableAllMarkup();
		
		
        int sec = (int) (STime / 1000);
		int min = sec / 60; sec %= 60;
		int hour = min / 60; min %= 60;
		double aSpeed = (double)Math.round(SDistance/STime*1000*100)/100;
		double cRate = (double)Math.round(SCO2/STime*1000*100)/100;
		Log.d("sum", "CO2 rate: " + String.valueOf(cRate));	// DEBUG log message
		//String sMode = db.getModes();
        tsummary.setText("Your Trip Conclusion: \n\n" + "\t1." + String.valueOf(MapTracking.nMode) + MapTracking.sMode + "\n"
        		+ "\n\t2.Elapsed time: " + String.valueOf(hour) + "h " 
        		+ String.valueOf(min) + "min " + String.valueOf(sec) + "s\n\n"
        		+ "\t3.Total distance: " + String.valueOf(SDistance) + "m\n\n"
        		+ "\t4.Total CO2 Emission: " + String.valueOf(SCO2) + "g\n\n"
        		+ "\t5.Average speed: " + String.valueOf(aSpeed) + "m/s\n\n"
        		+ "\t6.Average rate of CO2 Emission: " + String.valueOf(cRate) + "g/s\n");
	
	
	
	
	
	
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
		 	/*case R.id.history:
		 		File dbFile = getDatabasePath("mytrack.db");
		 		Log.d("database", dbFile.getAbsolutePath());
	        return true;*/
	        
	        /*case R.id.extract:
	        	exportToKML();
	        return true;*/
		 
			case R.id.about:
				new AlertDialog.Builder(this).setTitle("CO2 Emission Alert").setMessage("This app is developed by Junlong Xiang, Xiang Gao, and Feihu Qu, and is released under the GPL v2 software license.\n 2.12.12 Helsinki")
					.setCancelable(false).setNegativeButton("Got it", new DialogInterface.OnClickListener()
					{	public void onClick(DialogInterface dialog, int which)
						{
							dialog.cancel();
						}
					}).show();
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
