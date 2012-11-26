package com.example.co2emissionalert;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class SettingActivity extends Activity {

	private RadioGroup transportationGroup;
	private RadioButton tramcar;
	private RadioButton ferry;
	private RadioButton metro;
	private RadioButton bus;
	private RadioButton car;
	
	private Button startButton;
	private Button stopButton;
	
	private float M = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_setting);
		
		transportationGroup = (RadioGroup)findViewById(R.id.modegroup);
		tramcar = (RadioButton)findViewById(R.id.tramcarbutton);
		ferry = (RadioButton)findViewById(R.id.ferrybutton);
		metro = (RadioButton)findViewById(R.id.metrobutton);
		bus = (RadioButton)findViewById(R.id.busbutton);
		car = (RadioButton)findViewById(R.id.carbutton);
		
		startButton = (Button)findViewById(R.id.startloggingbutton);
		stopButton = (Button)findViewById(R.id.stoploggingbutton);
		
		startButton.setOnClickListener(new MyButtonListener());
		stopButton.setOnClickListener(new StopButtonListener());
		transportationGroup.setOnCheckedChangeListener(new MyGroupListener());
	}
  
	class MyGroupListener implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if(tramcar.getId() == checkedId)
			{
				M = (float) 0.115;
				//Toast.makeText(SettingActivity.this, "tramcar", Toast.LENGTH_SHORT).show();
			}else if(ferry.getId() == checkedId)
			{
				M = (float) 0.53;
			}else if(metro.getId() == checkedId)
			{
				M = (float) 0.42;
			}else if(bus.getId() == checkedId)
			{
				M = (float) 0.069;
			}else if(car.getId() == checkedId)
			{
				M = (float) 0.11;
			}
		}
		
	}
	
	class MyButtonListener implements OnClickListener
	{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.putExtra("M", M);
			intent.setClass(SettingActivity.this, MapTracking.class);
			SettingActivity.this.startActivity(intent);
			//Toast.makeText(SettingActivity.this, "click", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	class StopButtonListener implements OnClickListener
	{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			try {
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                System.exit(0);
        }
		}
		
	}
}
