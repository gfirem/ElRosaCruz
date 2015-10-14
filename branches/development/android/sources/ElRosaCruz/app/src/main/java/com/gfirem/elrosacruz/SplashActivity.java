package com.gfirem.elrosacruz;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ProgressBar;

import com.gfirem.elrosacruz.utils.BaseUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends FragmentActivity {

	ProgressBar fProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);

		BaseUtils.checkPlayServices(this);

		 TimerTask task = new TimerTask() {
	            @Override
	            public void run() {
	            	StartMainActivity();
	            }
	        };
	 
	        // Simulate a long loading process on application startup.
	        Timer timer = new Timer();
	        timer.schedule(task, 2000);
	}

	private void StartMainActivity() {
		Intent mainIntent = new Intent().setClass(SplashActivity.this, MainActivity.class);
		overridePendingTransition(0, 0);
		startActivity(mainIntent);
		finish();
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
	}
}
