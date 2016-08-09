package com.example.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayer.MyService.MyBinder;

public class PlayMusicActivity extends Activity implements OnClickListener {

	private MyService mBinder;
	private ImageView mPlay;
	private boolean isPlay = false;
	private SeekBar mSeekBar;
	private boolean isTouch = false;
	private boolean isFirst = true;
	private TextView mTotalTime;
	private TextView mStartTime;
	private Runnable mAction;
	private ImageView mImageIlike;
	private boolean isCollect = false;
	private int isFavorite;
	private SQLiteDatabase mDb;
	//从'我喜欢'中得到的地址，或从服务中得到的
	private String mPath="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_music);
		mStartTime = (TextView) findViewById(R.id.mStartTime);
		mTotalTime = (TextView) findViewById(R.id.mTotalTime);
		mPlay = (ImageView) findViewById(R.id.imagePlay);
		mPlay.setOnClickListener(this);
		mImageIlike = (ImageView) findViewById(R.id.imageIlike);
		mImageIlike.setOnClickListener(this);
		mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
		mSeekBar.setOnSeekBarChangeListener(new MySeekBarListener());
		MySqlite mySqlite = new MySqlite(this);
		mDb = mySqlite.getReadableDatabase();
		 getData();
	}

	// 获取'我喜欢'传递过来的数据
	private void getData() {
		Intent intent = getIntent();
		mPath = intent.getStringExtra("musicUrl");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent service = new Intent(PlayMusicActivity.this, MyService.class);
		startService(service);
		bindService(service, conn, Context.BIND_AUTO_CREATE);
	}

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = ((MyBinder) service).getSystem();
			isFirst = mBinder.getIsFirst();
			isPlay = mBinder.getisPlay();
			// 当从后台回来时，界面状态和之前一样
			isFavorite = mBinder.getIsFavorite();
			if (isFavorite == 1) {
				isCollect = true;
				mImageIlike.setImageResource(R.drawable.record_card_like_s);
			} else if (isFavorite == 0) {
				isCollect = false;
				mImageIlike.setImageResource(R.drawable.record_card_like_h);
			}

			// 按Pause键后，界面重新从后台回来，由于没有执行refresh，单独更新
			if (mBinder.isLoad()) {
				int duration = mBinder.getDuration();
				mTotalTime.setText(timeFormat(duration));
				// 设置进度条长度和音乐长度相等
				mSeekBar.setMax(duration);
			}
			// 后台处于播放状态时
			if (isPlay) {
				mPlay.setImageResource(R.drawable.ic_pause);
				refresh();
				Log.e("", "onServiceConnected==Play");
			} else {
				// 后台没有播放状态时
				mPlay.setImageResource(R.drawable.ic_play);
				Log.e("", "onServiceConnected==Pause");
				// 按Pause键的情况，进入后台，单独更新进度条
				if (!isFirst) {
					int i = mBinder.getCurrentPosition();
					mSeekBar.setProgress(i);
					mStartTime.setText(timeFormat(i));
				}
			}
		}
	};


	@Override
	protected void onPause() {
		super.onPause();
		// 从服务中获取music对象
		Music music = mBinder.getMusic();
		// 保存isFavorite到数据库中
		ContentValues values = new ContentValues();
		values.put("isFavorite", isFavorite);
		// 根据musicUrl音乐地址进行更新
		mDb.update("musicdata", values, "musicUrl=?",
				new String[] { music.musicUrl + "" });
		Log.e("", "===" + isFavorite);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//退出时，解绑
		unbindService(conn);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imagePlay:
			isPlay = !isPlay;
			// 当第一次播放时,执行playMusic，refresh更新进度条
			if (isFirst) {
				mPlay.setImageResource(R.drawable.ic_pause);
				//从服务中得到音乐地址
				mPath = mBinder.getPath();
				mBinder.playMusic(mPath);
				refresh();
				Log.e("", "isFirst---playMusic");
				// 重置isFirst标识位
				isFirst = false;
			} else {
				// 当不是第一次播放时,改为reStartPlay继续播放
				if (isPlay) {
					mPlay.setImageResource(R.drawable.ic_pause);
					refresh();
					mBinder.reStartPlay();
					Log.e("", "isPlay---reStartPlay");
				} else {
					// 暂停播放时,执行playPause，不更新进度条
					mPlay.setImageResource(R.drawable.ic_play);
					mBinder.playPause();
					Log.e("", " ---playPause");
				}
			}
			// 将更新的播放状态，重新保存到服务中
			mBinder.setIsPlay(isPlay);
			break;
		case R.id.imageIlike:
			// 判断是否为收藏
			isCollect = !isCollect;
			if (isCollect) {
				// 设置'我喜欢'标识位
				isFavorite = 1;
				// 将isFavorite值保存到服务中
				mBinder.setIsFavorite(isFavorite);
				mImageIlike.setImageResource(R.drawable.record_card_like_s);
			} else {
				isFavorite = 0;
				mBinder.setIsFavorite(isFavorite);
				mImageIlike.setImageResource(R.drawable.record_card_like_h);
			}
			break;
		default:
			break;
		}
	}

	// 更新进度条，时间显示
	private void refresh() {
		if (mAction == null) {
			mAction = new Runnable() {
				@Override
				public void run() {
					if (isPlay == false) {
						mAction = null;
						return;
					}

					// //isLoad为True，更新mTotalTime值
					if (mBinder.isLoad()) {
						int duration = mBinder.getDuration();
						mTotalTime.setText(timeFormat(duration));
						// 设置进度条长度和音乐长度相等
						mSeekBar.setMax(duration);
					}
					// 当拖动结束时,给进度条定位更新
					if (!isTouch) {
						int i = mBinder.getCurrentPosition();
						mSeekBar.setProgress(i);
						mStartTime.setText(timeFormat(i));
					}
					// 进度条1S前进一次
					mPlay.postDelayed(this, 1000);
				}
			};
			mPlay.postDelayed(mAction, 0);
		}
	}

	public String timeFormat(int progress) {
		int min = progress / 60;
		int sec = progress % 60;
		return String.format("%02d:%02d", min, sec);

	}

	class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// //当拖动过程中，显示mStartTime时间变化
			mStartTime.setText(timeFormat(progress));
			mSeekBar.setProgress(progress);

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			isTouch = true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// 拖动是否结束标示，当拖动结束时，才更新
			isTouch = false;
			// 当拖动结束时，MediaPlay重新定位播放位置
			mBinder.mySeekTo(mSeekBar.getProgress());
		}

	}

}
