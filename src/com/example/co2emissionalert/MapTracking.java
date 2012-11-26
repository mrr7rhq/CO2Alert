package com.example.co2emissionalert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

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
import android.view.Menu;
import android.widget.Toast;

public class MapTracking extends MapActivity implements LocationListener, OnInitListener{

	private MapView mapView;
	private MapController mapController;
	private MyLocationOverlay myLocationLay;
	private LocationManager locationManager;
	private Log log;
	private Location currentLocation;
	private Location lastLocation;
	private boolean isFirstLocation = true;//标记是否是第一个location 
	private Projection pro;
	private List<Overlay> overlays;
	private float SumDistance = 0;
	private final double EARTH_RADIUS = 6378137.0;  
	private ShakeListener shakeListener;
	//private DecimalFormat df;
	//private BigDecimal bd = new BigDecimal();
	private float MM;
	private float CO2M = 0;;
	private float NEWCO2M = 0;
	
	private boolean isInitial = false;//是否初始化mylocation
	
	private TextToSpeech TTS;
    private int MY_DATA_CHECK_CODE = 0;
    
    private Thread t;
    private volatile boolean flag= true;//线程状态标识
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.d("map", "oncreate start");
        setContentView(R.layout.activity_maptracking);
        mapControl();
        log.d("map", "oncreate end");
        
        //get M
        Intent intent = getIntent();
        MM = intent.getFloatExtra("M", (float) 0.0);
        
        //check for TTS data
        Intent TTSIntent = new Intent();
        TTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(TTSIntent, MY_DATA_CHECK_CODE);
        
        shakeListener = new ShakeListener(this);
        shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {  
            public void onShake() {            	
            	//df.format(SumDistance);
            	//甩动触发事件
            	/*Toast.makeText(getApplicationContext(), "your current distance is:" 
            			+ String.valueOf(SumDistance), Toast.LENGTH_LONG).show();
            	
            	speakWords("your current distance is " + String.valueOf(SumDistance));*/
            	
            	Toast.makeText(getApplicationContext(), "your current CO2 emission is:" 
            			+ String.valueOf(NEWCO2M) + "kilogram", Toast.LENGTH_LONG).show();
            	
            	speaking("your current CO2 emission is " + String.valueOf(NEWCO2M) + "killogram");
            }  
        });
        
        //t = new Thread(this);
        //t.start();
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
    {
    	mapView = (MapView)findViewById(R.id.mapViewId);
    	mapView.setBuiltInZoomControls(true);
    	pro = mapView.getProjection();
    	overlays = mapView.getOverlays();
        mapController = mapView.getController();
    	mapController.setZoom(12);
    	
    	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 10, MapTracking.this);
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 10, MapTracking.this);

    }
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
    	shakeListener.pause();
    	//flag = false;
		super.onDestroy();
	}

	private void init()
    {
    	if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
    	{
    		new AlertDialog.Builder(MapTracking.this).setTitle("地圖工具").setMessage("您尚未開啟定位服務，要前往設定頁面啟動定位服務嗎？")
			.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
			{

				public void onClick(DialogInterface dialog, int which)
				{
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					Toast.makeText(MapTracking.this, "未開啟定位服務，無法使用本工具!!", Toast.LENGTH_SHORT).show();
				}
			}).show();
    	}else
    	{
			if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
			{
				currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
			{
				currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
    		initMyLocation();
    		isInitial = true;
    	}
    }
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		log.d("map", "pause start");
		if(isInitial)
		{
			locationManager.removeUpdates(MapTracking.this);
			myLocationLay.disableCompass();
	        myLocationLay.disableMyLocation();
		}
		log.d("map", "pause end");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//t.notify();
		log.d("map", "resume start");
		if (isInitial)
		{
	           locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 10, MapTracking.this);
	           locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 10, MapTracking.this);
	              myLocationLay.enableMyLocation();
	             myLocationLay.enableCompass();
		}else
		{
			init();
		}
		log.d("map", "resume end");
	}

	private void initMyLocation()
    {
    	//定位点
    	List<Overlay> overlays = mapView.getOverlays();
         myLocationLay = new MyLocationOverlay(this, mapView);
         
         myLocationLay.enableCompass();
         myLocationLay.enableMyLocation();
         log.d("map", "run the funonfirstfix function");
         myLocationLay.runOnFirstFix(new Runnable(){
         
			@Override
			public void run() {
				// TODO Auto-generated method stub
				GeoPoint loc = myLocationLay.getMyLocation();
				mapController.animateTo(loc);
				//log.d("map", "get the firstlocation");
				log.d("map", "the firstlocation is:" + loc.toString());
				log.d("map", "funonfirstfix function end");
			}
        	 
         });
         overlays.add(myLocationLay);
    }

 

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		//Log.v("map", location.toString());
		if(isFirstLocation)
		{
			lastLocation = location;
		    currentLocation = location;
		    isFirstLocation = false;
		}else
		{
			lastLocation = currentLocation;
			currentLocation = location;
		}
		
		double lastlatitude = lastLocation.getLatitude();
		double lastlongitude = lastLocation.getLongitude();
		GeoPoint begin = new GeoPoint((int)(lastlatitude*1000000), (int)(lastlongitude*1000000));
		
		double currentlatitude = currentLocation.getLatitude();
		double currentlongitude = currentLocation.getLongitude();
		GeoPoint end = new GeoPoint((int)(currentlatitude*1000000), (int)(currentlongitude*1000000));
		
		overlays.add(new LineOverlay(begin,end));
		mapController.animateTo(end);
		getDistance(begin,end);   //calculate distance
		//gpsDistance(begin,end);
		//log.d("map", "get the currentlocation");
		log.d("location", "the lastlocation is:" + lastLocation.toString());
		log.d("location", "the currentlocation is:" + currentLocation.toString());

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
	
	//calculate distance between the cuurrentpoint and lastpoint
	public void getDistance(GeoPoint begin, GeoPoint end)
	{
		float[] results = new float[3];
		Location.distanceBetween(begin.getLatitudeE6()/1E6, begin.getLongitudeE6()/1E6, end.getLatitudeE6()/1E6, end.getLongitudeE6()/1E6, results);
		SumDistance += results[0];
		//df.format(SumDistance);
		getCO2Emission();
		log.d("distance", "the current getdistance:" + String.valueOf(results[0]));
		log.d("distance", "the sum getdistance:" + String.valueOf(SumDistance));
	}
	
	public void getCO2Emission()
	{
		CO2M = MM * SumDistance;
		CO2M = CO2M/1000;
		//保留4位小数
		NEWCO2M = (float) (Math.round(CO2M*10000)/10000.0); 
		//df.format(CO2M);
		//NEWCO2M = CO2M;
		log.d("CO2M", "the sum NEWCO2M:" + String.valueOf(NEWCO2M));
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
			log.d("distance", "the gpscurrent distance:" + String.valueOf(s));
			log.d("distance", "the sum gpsdistance:" + String.valueOf(SumDistance));
	       //return s;
	}*/
	
	// draw the tracking line
	class LineOverlay extends Overlay{
		private GeoPoint beginPoint;
		private GeoPoint endPoint;
		
		public LineOverlay(GeoPoint begin, GeoPoint end)
		{
			beginPoint = begin;
			endPoint = end;
		}
		
		public void draw(Canvas canvas, MapView mapV, boolean shadow)
		{
			super.draw(canvas, mapV, shadow);
			Paint paint = new Paint();
			paint.setColor(Color.RED);
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
        	
        	speaking("your current CO2 emission has reached " + String.valueOf(NEWCO2M) + "killogram");
        	flag = false;
		}
	}
	}*/
}
