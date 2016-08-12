package com.example.musicplayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.customimageview.CircleImageView;
import com.example.musicplayer.MainActivity.ImageAsync;
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
	private ImageView mImagePlayUp;
	private ImageView mImagePlayDown;
	private boolean isCollect = false;
	private int isFavorite;
	private SQLiteDatabase mDb;
	// 从'我喜欢'中得到的地址，或从服务中得到的
	private String mPath = "";
	// 播放模式
	private int mode = 0;
	private ImageView mImageMode;
	private Toast mToast;
	private CircleImageView mCustom_image;
	private TextView mMusicName;
	private Music music = null;

	// 每次开始播放时，使用广播来接收
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 更新音乐名称、自定义ImageView图片
			showCustomImage();
		}
	};

	private void showCustomImage() {

		music = mBinder.getMusic();
		if (music != null) {
			// 每次开始播放时，更新音乐名称
			mMusicName.setText(music.musicName);
			HashMap<Integer, Bitmap> mImageMap = mBinder.getImageMap();
			// 从服务中获得图片集合index下标，不可
			int index = mBinder.getIndex();
			Bitmap bm = mImageMap.get(index);
			if (bm == null) {
				ImageAsync imageAsync = new ImageAsync();
				imageAsync.execute(music.imageUrl);
			} else {
				// 更新到自定义ImageView上
				mCustom_image.setImageBitmap(bm);
			}
		}
	}

	class ImageAsync extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			InputStream is = null;
			try {
				URL url = new URL(params[0]);
				is = url.openStream();
				bitmap = BitmapFactory.decodeStream(is);
				return bitmap;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			mCustom_image.setImageBitmap(result);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_music);
		mStartTime = (TextView) findViewById(R.id.mStartTime);
		mTotalTime = (TextView) findViewById(R.id.mTotalTime);
		mMusicName = (TextView) findViewById(R.id.textMusicName);
		mPlay = (ImageView) findViewById(R.id.imagePlay);
		mPlay.setOnClickListener(this);
		mImageIlike = (ImageView) findViewById(R.id.imageIlike);
		mImageIlike.setOnClickListener(this);
		mImageMode = (ImageView) findViewById(R.id.imageMode);
		mImageMode.setOnClickListener(this);
		mImagePlayUp = (ImageView) findViewById(R.id.imagePlayUp);
		mImagePlayUp.setOnClickListener(this);
		mImagePlayDown = (ImageView) findViewById(R.id.imagePlayDown);
		mImagePlayDown.setOnClickListener(this);
		mCustom_image = (CircleImageView) findViewById(R.id.profile_image);
		mCustom_image.setOnClickListener(this);
		mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
		mSeekBar.setOnSeekBarChangeListener(new MySeekBarListener());
		MySqlite mySqlite = new MySqlite(this);
		mDb = mySqlite.getReadableDatabase();
		getData();

	}

	// 获取'我喜欢'传递过来的数据
	private void getData() {
		Intent intent = getIntent();
		// mPath = intent.getStringExtra("musicUrl");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent service = new Intent(PlayMusicActivity.this, MyService.class);
		startService(service);
		bindService(service, conn, Context.BIND_AUTO_CREATE);
		// 注册广播，用于音乐名称、自定义图片更新
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.action.broadcast.BeginingPlay");
		registerReceiver(receiver, filter);
	}

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = ((MyBinder) service).getSystem();
			Log.e("", "showCustomImage");
			// 更新音乐名称、自定义ImageView图片
			showCustomImage();
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
		super.onStop();
		// 退出时，解绑
		unbindService(conn);
		unregisterReceiver(receiver);
	}

	@Override
	protected void onDestroy() {
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
				// 从服务中得到音乐地址
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
		// 设置播放模式
		case R.id.imageMode:
			chanceMode();
			break;
		case R.id.imagePlayUp:
			mBinder.playUpMusic();
			// 将更新的播放状态，重新保存到服务中
			mBinder.setIsPlay(isPlay);
			break;
		case R.id.imagePlayDown:
			mBinder.playUpMusic();
			// 将更新的播放状态，重新保存到服务中
			mBinder.setIsPlay(isPlay);
			break;
		default:
			break;
		}
	}

	private void chanceMode() {
		mode++;
		mode = mode % 3;
		switch (mode) {
		// 循环
		case 0:
			// 更改图片
			mImageMode.setImageResource(R.drawable.player_btn_repeat_normal);
			// 通知显示
			showToast("已切换到自动播放");
			break;
		// 单曲
		case 1:
			// 更改图片
			mImageMode.setImageResource(R.drawable.player_btn_repeatone_normal);
			showToast("已切换到单曲循环");
			break;
		// 随机
		case 2:
			// 更改图片
			mImageMode.setImageResource(R.drawable.player_btn_random_normal);
			showToast("已切换到随机播放");
			Log.e("", "" + mode);
			break;
		default:
			break;
		}
		// 保存到服务中
		mBinder.setMode(mode);
	}

	// 自定义“播放模式”通知
	private void showToast(String msg) {
		if (mToast != null) {
			mToast.cancel();
		}
		mToast = new Toast(PlayMusicActivity.this);
		View view = getLayoutInflater().inflate(R.layout.toast_custom, null);
		ImageView image = (ImageView) view.findViewById(R.id.imageToastCustom);
		TextView tv1 = (TextView) view.findViewById(R.id.textToastCustom);
		tv1.setText(msg);
		mToast.setView(view);
		mToast.setDuration(1000);
		mToast.setGravity(Gravity.TOP, 0, 100);
		mToast.show();
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
