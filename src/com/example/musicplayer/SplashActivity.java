package com.example.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

public class SplashActivity extends Activity {

	private boolean isFirst;
	private ImageView mWelcome;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		mWelcome = (ImageView) findViewById(R.id.imageView1);
		// 获得guide中isFirst
		SharedPreferences sp = getSharedPreferences("guide", MODE_PRIVATE);
		isFirst = sp.getBoolean("isFirst", true);
		
		mWelcome.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (isFirst) {
					Intent intent = new Intent(SplashActivity.this,
							GuideActivity.class);
					startActivity(intent);
					finish();
				} else {
					Intent intent = new Intent(SplashActivity.this,
							MainActivity.class);
					startActivity(intent);
					finish();
				}
			}
		}, 3000);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	
	}

}
