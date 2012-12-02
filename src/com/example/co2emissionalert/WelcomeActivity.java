package com.example.co2emissionalert;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;

public class WelcomeActivity extends Activity {
	private final int SPLASH_DELAY_TIME = 4000 ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub				
				startActivity(new Intent(WelcomeActivity.this , SettingActivity.class));
				WelcomeActivity.this.finish();
			}
		}, SPLASH_DELAY_TIME);
    }

}
