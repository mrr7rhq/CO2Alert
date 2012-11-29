package com.example.co2emissionalert;

//import java.math.BigDecimal;
//import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import com.example.co2emissionalert.SettingActivity.StopButtonListener;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MapTracking extends MapActivity implements LocationListener, OnInitListener{

	private static final int CO2_THRESHOLD = 5000;	// CO2 emission threshold for active alerting
	
	private MapView mapView;
	private MapController mapController;
	private MyLocationOverlay myLocationLay;
	private LocationManager locationManager;
	//private LocationListener locListener;
	private Location currentLocation;
	private Location lastLocation;
	private boolean isFirstLocation = true;	// Flag is true if current point is the first location in tracking
	private Projection pro;
	private List<Overlay> overlays;
	private Toast toast;
	
	//private final double EARTH_RADIUS = 6378137.0;  
	private ShakeListener shakeListener;
	//private DecimalFormat df;
	//private BigDecimal bd = new BigDecimal();
	private float MM;	// Coefficient M from Setting Activity
	private float CO2M = 0;
	private float NEWCO2M = 0;
	private float SumDistance = 0;
	private float NEWSumDistance = 0;
	private int COLOR = Color.RED;
	private boolean isInitial = false;	// Flag is true if to initial mylocation
	private boolean isGPSOn = true;
	private int thresBlock = 0;
	private TextToSpeech TTS;
    private int MY_DATA_CHECK_CODE = 0;
    
    //private Thread t;
    //private volatile boolean flag= true;	// Flag for thread status
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("map", "oncreate start");	// DEBUG log message
        setContentView(R.layout.activity_maptracking);
        mapControl();	
        Log.d("map", "oncreate end");	// DEBUG log message
        
        // Get M from the intent by SettingActivity
        Intent intent = getIntent();
        MM = intent.getFloatExtra("M", (float) 0.0);
        
        toast = Toast.makeText(getApplicationContext(), "Distance: " + String.valueOf(NEWSumDistance) + " m\n"
        		+ "CO2 emission: " + String.valueOf(NEWCO2M) + " g", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 50);
        
        //check for TTS data
        Intent TTSIntent = new Intent();
        TTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(TTSIntent, MY_DATA_CHECK_CODE);
        
        shakeListener = new ShakeListener(this);
        shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {  
            public void onShake() {            	
            	//df.format(SumDistance);
            	// Shaking behavior triggers a text pop-up and speech of distance & CO2 emission 
            	        	
            	toast.setText("Distance: " + String.valueOf(NEWSumDistance) + " m\n" + "CO2 emission: " + String.valueOf(NEWCO2M) + " g");		
        		toast.show();
        		
            	speaking("Your current distance is" + String.valueOf(NEWSumDistance) + "meters" + "And your current CO2 emission is" + String.valueOf(NEWCO2M) + "grams");
            }  
        });
        
        
        //t = new Thread(this);
        //t.start();
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_maptracking, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.walk:
        MM=180; COLOR=Color.RED;
        return true;
        case R.id.bike:
        MM=75; COLOR=Color.BLUE;
        return true;
        case R.id.bus:
        MM=100; COLOR=Color.CYAN;
        return true;
        case R.id.car:
        MM=149; COLOR=Color.GREEN;
        return true;
        case R.id.tram:
        MM=60; COLOR=Color.GRAY;
        return true;
        case R.id.ferry:
        MM=125; COLOR=Color.YELLOW;
        return true;
        case R.id.train:
        MM=43; COLOR=Color.MAGENTA;
        return true;
        case R.id.metro:
        MM=3.3f; COLOR=Color.BLACK;
        return true;
        
        case R.id.satellite:
        	Toast.makeText(getApplicationContext(), "Satellite View", Toast.LENGTH_SHORT).show();
        	if(mapView.isSatellite()==false) {
        		mapView.setSatellite(true);
        	}
        return true;
        
        case R.id.normal:
        	Toast.makeText(getApplicationContext(), "Normal Map View", Toast.LENGTH_SHORT).show();
        	if(mapView.isSatellite()==true) {
        		mapView.setSatellite(false);
        	}
        return true;
        
        case R.id.clear:
        	SumDistance=NEWSumDistance=0;
        	CO2M=NEWCO2M=0;
        	overlays.clear();
    		mapView.invalidate(); 
    		initMyLocation();    		
        return true;
        
        case R.id.commit:
        	
        	
        return true;
        
        
        case R.id.about:
        	new AlertDialog.Builder(MapTracking.this).setTitle("CO2 Emission Alert").setMessage("This app is developed by Junlong Xiang, Xiang Gao, and Feihu Qu, and is released under the GPL v2 software license.\n 26.11.12 Helsinki")
			.setCancelable(false).setNegativeButton("Got it", new DialogInterface.OnClickListener()
			{	public void onClick(DialogInterface dialog, int which)
				{
					dialog.cancel();
				}
			}).show();
        return true;
        
        default:
        return super.onOptionsItemSelected(item);
        }
    }
    
   /***************TTS****************/ 

       //check TTS
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
        	//if TTS has installed
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            TTS = new TextToSpeech(this, this);
            }
            else {
                    //or install TTSDATA
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }
        //initial TTS
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            if(TTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                TTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "TextToSpeech failed", Toast.LENGTH_LONG).show();
        }
    }
    
    public void speaking(String speech) {
        TTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }
    /***************TTS****************/ 
    
    private void mapControl()
    {		// configure mapview 
    	mapView = (MapView)findViewById(R.id.mapViewId);
    	mapView.setBuiltInZoomControls(true);
    	pro = mapView.getProjection();
    	overlays = mapView.getOverlays();
        mapController = mapView.getController();
    	mapController.setZoom(14);
    		// Use both mobile network provider and GPS to update location information
    	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, MapTracking.this);
    	//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, MapTracking.this);

    }
    	
    // clean up current activity after destroyed
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
    	shakeListener.onPause();
    	//flag = false;
		super.onDestroy();
	}

    	// Setup the location service and initial the start point of tracking on map
	private void init()
    {
    	if (!(isGPSOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {	
    			// If GPS service is disabled on mobile phone, give an alert dialog
    		new AlertDialog.Builder(MapTracking.this).setTitle("Map Tools").setMessage("Your localization service is not setup. Try to turn it on?")
			.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
			{	// When choosing to turn on GPS, go to setting page
				public void onClick(DialogInterface dialog, int which)
				{
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{	// When choose not to open GPS, give a notice of disability
				public void onClick(DialogInterface dialog, int which)
				{
					Toast.makeText(MapTracking.this, "This application is blocked without GPS service.", Toast.LENGTH_SHORT).show();
				}
			}).show();
    		
    	}else {	// get current location as the last known point in history
				
    		if ( locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
			{		// If GPS can provide the last known location data, get it using GPS
				currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			}else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
			{		// otherwise, test mobile network provider if the last known location is available
				currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
    		initMyLocation();	// initMyLocation()
    		isInitial = true;	// Flag: initialization is done
    	}
    }
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		Log.d("map", "pause start");	// DEBUG log message
		if(isInitial)
		{
			locationManager.removeUpdates(MapTracking.this);
			myLocationLay.disableCompass();
	        myLocationLay.disableMyLocation();
		}
		Log.d("map", "pause end");	// DEBUG log message
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//t.notify();
		Log.d("map", "resume start");	// DEBUG log message
		if (isInitial)
		{
	    	if(isGPSOn) locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 6, MapTracking.this);
	    	else	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 6, MapTracking.this);
	        myLocationLay.enableMyLocation();
	        myLocationLay.enableCompass();
		}else
		{
			init();
		}
		Log.d("map", "resume end");	// DEBUG log message
	}

	private void initMyLocation()
    {    // Initialize my current location
		List<Overlay> overlays = mapView.getOverlays();
        myLocationLay = new MyLocationOverlay(this, mapView);
         
        myLocationLay.enableCompass();
        myLocationLay.enableMyLocation();
        Log.d("map", "run the funonfirstfix function");	// DEBUG log message
        
        myLocationLay.runOnFirstFix(new Runnable(){
        	// center the first point on myLocationLay
        	@Override
        	public void run() {
        		// TODO Auto-generated method stub
        		GeoPoint loc = myLocationLay.getMyLocation();
        		mapController.animateTo(loc);
        		//Log.d("map", "get the firstlocation");	// DEBUG log message
        		Log.d("map", "the firstlocation is:" + loc.toString());	// DEBUG log message
        		Log.d("map", "funonfirstfix function end");	// DEBUG log message
        	}
        	 
        });
        
        overlays.add(myLocationLay);	// add myLocationLay to mapview
        mapView.invalidate();
    }

 

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		//Log.v("map", location.toString());	// DEBUG log message
		if(isFirstLocation) {		
				// if input location is the first point in tracking
			lastLocation = location;
		    currentLocation = location;
		    isFirstLocation = false;
		    
		}else {		// otherwise, store lastLocation and update currentLocation
			lastLocation = currentLocation;
			currentLocation = location;
		}
			// use lastLocation as start point of line
		double lastlatitude = lastLocation.getLatitude();
		double lastlongitude = lastLocation.getLongitude();
		GeoPoint begin = new GeoPoint((int)(lastlatitude*1000000), (int)(lastlongitude*1000000));
			// use currentLocation as end point of line
		double currentlatitude = currentLocation.getLatitude();
		double currentlongitude = currentLocation.getLongitude();
		GeoPoint end = new GeoPoint((int)(currentlatitude*1000000), (int)(currentlongitude*1000000));
			// draw the line and add an Overlay to mapview
		overlays.add(new LineOverlay(begin,end,COLOR));
		mapView.invalidate();
		mapController.animateTo(end);
		getDistance(begin,end,MM);   //calculate distance and CO2 emission
		
			// show the toast updated on location changed
		toast.setText("Distance: " + String.valueOf(NEWSumDistance) + " m\n" + "CO2 emission: " + String.valueOf(NEWCO2M) + " g");		
		toast.show();
		
		if((thresBlock < 1) && (NEWCO2M > CO2_THRESHOLD)){	// actively check if the CO2 emission has exceeded the threshold
			thresBlock += 1;       	
        	speaking("Alas! Your current CO2 emission has exceeded the threshold.");
		}
		//gpsDistance(begin,end);
		//Log.d("map", "get the currentlocation");	// DEBUG log message
		Log.d("location", "the lastlocation is:" + lastLocation.toString());	// DEBUG log message
		Log.d("location", "the currentlocation is:" + currentLocation.toString());	// DEBUG log message

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
		//calculate distance between the currentpoint and lastpoint
	public void getDistance(GeoPoint begin, GeoPoint end, float x)
	{
		float[] results = new float[3];
			// Compute the approximate distance in meters between two locations
		Location.distanceBetween(begin.getLatitudeE6()/1E6, begin.getLongitudeE6()/1E6, end.getLatitudeE6()/1E6, end.getLongitudeE6()/1E6, results);
		SumDistance += results[0];	// SumDistance accumulates calculation result
		NEWSumDistance = (float) (Math.round(SumDistance*10)/10); 
		//df.format(SumDistance);
		getCO2Emission(x);	// calculate CO2 emission
		Log.d("distance", "the current getdistance:" + String.valueOf(results[0]));	// DEBUG log message
		Log.d("distance", "the sum getdistance:" + String.valueOf(SumDistance));	// DEBUG log message
	}
	
		// calculate CO2 emission with distance value
	public void getCO2Emission(float mvalue)
	{	
		CO2M = mvalue * SumDistance/1000;
		// keep 1 digits after decimal point
		NEWCO2M = (float) (Math.round(CO2M*10)/10); 
		//df.format(CO2M);
		//NEWCO2M = CO2M;
		Log.d("CO2M", "the sum NEWCO2M:" + String.valueOf(NEWCO2M));	// DEBUG log message
	}
	
	/*public void gpsDistance(GeoPoint begin, GeoPoint end)
	{
		double lat_a = begin.getLatitudeE6()/1E6;
		double lng_a = begin.getLongitudeE6()/1E6;
		double lat_b = end.getLatitudeE6()/1E6;
		double lng_b = end.getLongitudeE6()/1E6;
		double radLat1 = (lat_a * Math.PI / 180.0);	 
	       double radLat2 = (lat_b * Math.PI / 180.0);
	       double a = radLat1 - radLat2;
	       double b = (lng_a - lng_b) * Math.PI / 180.0;
	       double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
	              + Math.cos(radLat1) * Math.cos(radLat2)
	              * Math.pow(Math.sin(b / 2), 2)));
	       s = s * EARTH_RADIUS;
	       s = Math.round(s * 10000) / 10000;
	       SumDistance += s;
			Log.d("distance", "the gpscurrent distance:" + String.valueOf(s));
			Log.d("distance", "the sum gpsdistance:" + String.valueOf(SumDistance));
	       //return s;
	}*/
	
		// draw a tracking line with Overlay on the mapview, given two ends
	class LineOverlay extends Overlay{
		private GeoPoint beginPoint;
		private GeoPoint endPoint;
		private int lineColor;
		
		public LineOverlay(GeoPoint begin, GeoPoint end, int linecolor)
		{
			beginPoint = begin;
			endPoint = end;
			lineColor = linecolor;
		}
		
		public void draw(Canvas canvas, MapView mapV, boolean shadow)
		{
			super.draw(canvas, mapV, shadow);
			Paint paint = new Paint();
			paint.setColor(lineColor);
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			paint.setStrokeWidth(4);
			Point pixBeginPoint = new Point();
			Point pixEndPoint = new Point();
			Path path = new Path();
			pro.toPixels(beginPoint, pixBeginPoint);
			pro.toPixels(endPoint, pixEndPoint);
			path.moveTo(pixBeginPoint.x, pixBeginPoint.y);
			path.lineTo(pixEndPoint.x, pixEndPoint.y);
			canvas.drawPath(path,paint);
		}
	}

	
	/*public void run() {
		// TODO Auto-generated method stub
		while(flag){
		if(NEWCO2M >= 0.001)
		{
			Toast.makeText(getApplicationContext(), "your current CO2 emission has reached:" 
        			+ String.valueOf(NEWCO2M) + "kilogram", Toast.LENGTH_LONG).show();
        	
        	speaking("your current CO2 emission has reached " + String.valueOf(NEWCO2M) + "kilogram");
        	flag = false;
		}
	}
	}*/
}
