package com.example.co2emissionalert;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeListener implements SensorEventListener {

	  //两次检测的时间间隔
	private static final int INTERVAL_TIME = 70;
	//速度阈值，当摇晃速度达到这值后产生作用
	private static final int SPEED_SHRESHOLD = 4500;	 
	private SensorManager sensorManager; 
	private Sensor sensor; 
  private Context context; 
	private OnShakeListener onShakeListener; 
  //手机上一个位置时重力感应坐标	 
	private float lastX; 
	private float lastY; 
  private float lastZ;

	private long lastUpdateTime;
  
    public interface OnShakeListener {  
        public void onShake();  
    }  
  
    public ShakeListener(Context context) {  
        this.context = context;  
        resume();  
    }  
  
    public void setOnShakeListener(OnShakeListener listener) {  
    	onShakeListener = listener;  
    }  
  
    public void resume() {  
    	sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE); 
    	if(sensorManager == null) { 
    		throw new UnsupportedOperationException("Sensors are not supported");
    	  } 
    	//获得重力传感器 
    	sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); 
    	//注册 
       if(sensor != null) { 
    	sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME); 
    	 }  
    }  
  
    public void pause() {  
        if (sensorManager != null) {  
        	sensorManager.unregisterListener(this);  
        	sensorManager = null;  
        } 
    }  
  
      
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  
          
    }  
  
     
    public void onSensorChanged(SensorEvent event) {  
  
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {  
            return;  
        } 
    	long currentUpdateTime = System.currentTimeMillis(); 
    	//两次检测的时间间隔	 
    	long interval = currentUpdateTime - lastUpdateTime;   
    	//判断是否达到了检测时间间隔 
    	if(interval < INTERVAL_TIME) 
    	return; 
    	lastUpdateTime = currentUpdateTime; 
    	//获得x,y,z坐标 
    	float x = event.values[0]; 
    	float y = event.values[1]; 
    	float z = event.values[2]; 
         //获得x,y,z的变化值
          float changeX = x - lastX;
          float changeY = y - lastY;
          float changeZ = z - lastZ;

          lastX = x;
          lastY = y;
          lastZ = z;
         double speed = Math.sqrt(changeX*changeX + changeY*changeY + changeZ*changeZ)/interval * 10000;
         //达到速度阀值，发出提示
         if(speed >= SPEED_SHRESHOLD)
         onShakeListener.onShake(); 
    } 
}
