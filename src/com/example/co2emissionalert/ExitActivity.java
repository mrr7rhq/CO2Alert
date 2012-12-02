package com.example.co2emissionalert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class ExitActivity extends Activity {
	private final int SPLASH_DELAY_TIME = 3000;
	public static boolean isApplicationTerminated = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated constructor stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);
        //Intent intent = getIntent();
        //Log.d("exit", "set");
        new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				isApplicationTerminated = true;
				ExitActivity.this.finish();

			}
		}, SPLASH_DELAY_TIME);
	}
	
	
}
