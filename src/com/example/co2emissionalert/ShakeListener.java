package com.example.co2emissionalert;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public class ShakeListener extends Activity implements SensorEventListener {

								
	private static final int INTERVAL_SAMPLING_THRESHOLD = 100;	// Set the interval time threshold between two consecutive SensorEvents		
	private static final int DELTA_LOWER_THRESHOLD = 40;	// Set the shaking speed threshold: defined for recognizing shaking behavior
	private static final int DELTA_UPPER_THRESHOLD = 100;
	
	private static final int SHAKE_TIMEOUT = 500;
	private static final int SHAKE_DURATION = 1000;
	private static final int SHAKE_COUNT = 3;
	
	private static boolean SetBlock = false;	// block flag for onShake()
	private static boolean ShakeFlag = false;
	//private static final float alpha = 0.8f;	// filter coefficient: alpha is calculated as t / (t + dT)
    											// with t, the low-pass filter's time-constant
    											// and dT, the event delivery rate
	private SensorManager sensorManager; 
	private Sensor sensor; 
	private Context context; 
	private OnShakeListener onShakeListener; 
	
		// The buffer acceleration of last event	 
	private float lastX = 0f; 
	private float lastY = 0f; 
	private float lastZ = 0f;
		// Unix time in milliseconds
	//private long[] updateTime = {0L, 0L, 0L, 0L, 0L};
	private long lastTime = 0L;
	private int shakeCount = 0;
	private long lastShakeTime = 0L;
	private long lastFineTime = 0L;
	
    public interface OnShakeListener {  
        public void onShake();
    }
  
    	// Constructor for given context
    public ShakeListener(Context context) {  
        this.context = context;  
        
        onResume(); 
    }  
  
    public void setOnShakeListener(OnShakeListener listener) {  
    	onShakeListener = listener;  
    }  
    
    @Override
    protected void onResume() {  
    	//super.onResume();
    	
    	// Retrieve a SensorManager for accessing sensors
     	sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
     	if(sensorManager == null) { 
     		throw new UnsupportedOperationException("Sensors are not supported");
     	} 
    	// Get default sensor of type ACCELEROMETER
    	sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); 
    		// Register ShakeListener for given sensor
    	if(sensor != null) { 
    	   sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME); 
    	}
    }  
    
    	// When activity is paused
    @Override
    protected void onPause() {      	
    	
    		// Unregister the ShakeListener for target sensor
    	if (sensorManager != null) {  
        	sensorManager.unregisterListener(this);  
        	sensorManager = null;  
        }
    	//super.onPause();
    }  
  
    
    	// When the accuracy of a sensor has changed  
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {  
          
    }  
  
    
    	// When sensor values have changed
    @Override
    public final void onSensorChanged(SensorEvent event) {  
    	
    	//if(SetBlock) return;	// block is on for running onShake()
    	// Test if the SensorEvent is for ACCELEROMETER
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {  
            return;  
        }
        
        double dA = 0;
        double avgdA = 0;
    	
        	// timestamp of event in milliseconds
        long currentTime = System.currentTimeMillis(); 
    	
        if ((currentTime - lastFineTime) > SHAKE_TIMEOUT) {
            shakeCount = 0;
        }
        
        if ((currentTime - lastTime) > INTERVAL_SAMPLING_THRESHOLD) {
            long interval = currentTime - lastTime;
            	// get current acceleration for each direction
            float x = event.values[0]; 
            float y = event.values[1]; 
            float z = event.values[2]; 
        		// get delta values of acceleration
            float dX = x - lastX;
            float dY = y - lastY;
            float dZ = z - lastZ;
        		// buffer current acceleration
            lastX = x;
            lastY = y;
            lastZ = z;
        		// delta-acceleration: vector difference
            dA = Math.sqrt(dX*dX + dY*dY + dZ*dZ);
	        avgdA = dA / interval *100;
	        Log.d("sensor","Vector acceleration: ( " + String.valueOf(x) + ", " + String.valueOf(y) + ", " + String.valueOf(z) + ")" );	// DEBUG log message
            Log.d("sensor","Average Delta acceleration: " + String.valueOf(avgdA));	// DEBUG log message
	        
	        if(avgdA > DELTA_LOWER_THRESHOLD){
	        	if ((++shakeCount >= SHAKE_COUNT) && (currentTime - lastShakeTime > SHAKE_DURATION)){
	                lastShakeTime = currentTime;
	                shakeCount = 0;
	                if (onShakeListener != null) { 
	                	ShakeFlag = true; 	// shake detected and shakeflag set
	                }
	            }
	            lastFineTime = currentTime;
	        }
	        lastTime = currentTime;
            
        
        }
        	// Trigger onShake on pattern: delta-acceleration crest over threshold occurs twice within an interval
        if(!SetBlock && ShakeFlag && (avgdA > DELTA_LOWER_THRESHOLD) && (avgdA < DELTA_UPPER_THRESHOLD)) {
        	SetBlock = true; 
        	onShakeListener.onShake();
        	new Handler().postDelayed(new Runnable(){
        		@Override
        		public void run(){
        			SetBlock = false; ShakeFlag = false;
        		}
        	},5000);	// 5s block for speaking
        	
        }
        
        
    	// get current acceleration for each direction
    /*float x = event.values[0]; 
    float y = event.values[1]; 
    float z = event.values[2]; 
		// get delta values of acceleration
    float dX = x - lastX;
    float dY = y - lastY;
    float dZ = z - lastZ;
		// buffer current acceleration
    lastX = x;
    lastY = y;
    lastZ = z;
		// delta-acceleration: vector difference
    dA = Math.sqrt(dX*dX + dY*dY + dZ*dZ);
    
        if(dA > 10)	onShakeListener.onShake();*/
    } 
}
