package com.example.co2emissionalert;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeListener extends Activity implements SensorEventListener {

		// Set the interval time constant between two consecutive SensorEvents
	private static final int INTERVAL_TIME = 70;
		// Set the shaking speed threshold: defined for recognizing shaking behavior
	private static final int DELTA_THRESHOLD = 45;	 
	private SensorManager sensorManager; 
	private Sensor sensor; 
	private Context context; 
	private OnShakeListener onShakeListener; 
	//public static final float alpha = 0.8f;
		// The buffer acceleration of last event	 
	private float lastX; 
	private float lastY; 
	private float lastZ;
		// Unix time in milliseconds for last SensorEvent
	private long lastUpdateTime;

	
    public interface OnShakeListener {  
        public void onShake();
    }
  
    	// Constructor for given context
    public ShakeListener(Context context) {  
        this.context = context;  
        // Retrieve a SensorManager for accessing sensors
     	sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
     	if(sensorManager == null) { 
     		throw new UnsupportedOperationException("Sensors are not supported");
     	} 
        onResume(); 
    }  
  
    public void setOnShakeListener(OnShakeListener listener) {  
    	onShakeListener = listener;  
    }  
    
    @Override
    protected void onResume() {  
    	super.onResume();
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
    	super.onPause();
    		// Unregister the ShakeListener for target sensor
    	if (sensorManager != null) {  
        	sensorManager.unregisterListener(this);  
        	sensorManager = null;  
        }     	
    }  
  
    
    	// When the accuracy of a sensor has changed  
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {  
          
    }  
  
    
    	// When sensor values have changed
    @Override
    public final void onSensorChanged(SensorEvent event) {  
    		// Test if the SensorEvent is for ACCELEROMETER
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {  
            return;  
        } 
    		// Get the current Unix time in milliseconds
        long currentUpdateTime = System.currentTimeMillis(); 
    		// The interval time from last SensorEvent
    	long interval = currentUpdateTime - lastUpdateTime;   
    		// Test if interval time is obvious enough
    	if(interval < INTERVAL_TIME) {
    		return; 
    	}
    		// Buffer the current time for event in lastUpdateTime
    	lastUpdateTime = currentUpdateTime; 
    	
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
        	// delta-acceleration
        double dA = Math.sqrt(dX*dX + dY*dY + dZ*dZ);	
        Log.d("sensor","Current delta acceleration:" + String.valueOf(dA));	// DEBUG log message
        	// Trigger onShake if delta-acceleration is larger than threshold
        if(dA >= DELTA_THRESHOLD) {
        	onShakeListener.onShake(); 
        }
    } 
}
