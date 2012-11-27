package com.example.co2emissionalert;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class SettingActivity extends Activity {

	private RadioGroup transportationGroup;
	private RadioButton walk;
	private RadioButton bike;
	private RadioButton tram;
	private RadioButton ferry;
	private RadioButton metro;
	private RadioButton train;
	private RadioButton bus;
	private RadioButton car;
	
	private Button startButton;
	private Button stopButton;
	// M: coefficient for CO2 calculation, default checked for walk/bicycle mode
	private float M = (float) 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_setting);
		
		transportationGroup = (RadioGroup)findViewById(R.id.modegroup);
		walk = (RadioButton)findViewById(R.id.walkbutton);
		bike = (RadioButton)findViewById(R.id.bikebutton);
		tram = (RadioButton)findViewById(R.id.trambutton);
		ferry = (RadioButton)findViewById(R.id.ferrybutton);
		metro = (RadioButton)findViewById(R.id.metrobutton);
		train = (RadioButton)findViewById(R.id.trainbutton);
		bus = (RadioButton)findViewById(R.id.busbutton);
		car = (RadioButton)findViewById(R.id.carbutton);
		
		startButton = (Button)findViewById(R.id.startloggingbutton);
		stopButton = (Button)findViewById(R.id.stoploggingbutton);
		
		startButton.setOnClickListener(new MyButtonListener());
		stopButton.setOnClickListener(new StopButtonListener());
		transportationGroup.setOnCheckedChangeListener(new MyGroupListener());
	}
	
	
      class MyGroupListener implements OnCheckedChangeListener{
		// Check transportationGroup, when mode choice is changed, set M value for newly checked mode. 
		// Coefficient M is differently chosen for modes.
		// Transportation in 8 modes: walk, bicycle, tram, ferry, metro, train, bus, car.
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if(walk.getId() == checkedId){			
				M = (float) 180;
			}else if(bike.getId() == checkedId){			
				M = (float) 75;
			}else if(tram.getId() == checkedId){			
				M = (float) 60;
			}else if(ferry.getId() == checkedId){
				M = (float) 125;
			}else if(metro.getId() == checkedId){
				M = (float) 3.3;
			}else if(train.getId() == checkedId){
				M = (float) 43;
			}else if(bus.getId() == checkedId){
				M = (float) 100;
			}else if(car.getId() == checkedId){
				M = (float) 149;
			}
		}
		
	}
	
	class MyButtonListener implements OnClickListener
	{
			// When "Start" button is clicked, onClick method initiates MapTracking activity.
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.putExtra("M", M);	// assign M value to counterpart in MapTracking
			intent.setClass(SettingActivity.this, MapTracking.class);
			SettingActivity.this.startActivity(intent);	// trigger MapTracking activity
		}
		
	}
	
	class StopButtonListener implements OnClickListener
	{
			// When "Exit" button is clicked, onClick method kills this process and exits.
		@Override
		public void onClick(View arg0) {
			finish();
		}
		
	}
}
