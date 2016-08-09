package com.example.musicplayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.MyService.MyBinder;

public class MainActivity extends Activity implements OnClickListener {

	private Button mOnline;
	private Button mRecentPlay;
	private RelativeLayout mDownRelative;
	private MyService mBinder;
	private TextView mSinger;
	private ImageView mImagePlay;
	private TextView mMusicName;
	private TextView mWelcome;
	private ImageView mImageUrl;
	private Button myFavoriteBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDownRelative = (RelativeLayout) findViewById(R.id.downRelativeLayout);
		mDownRelative.setOnClickListener(this);
		mOnline = (Button) findViewById(R.id.onlineBtn);
		mOnline.setOnClickListener(this);
		mRecentPlay = (Button) findViewById(R.id.recentPlayBtn);
		mRecentPlay.setOnClickListener(this);
		myFavoriteBtn = (Button) findViewById(R.id.myFavoriteBtn);
		myFavoriteBtn.setOnClickListener(this);
		mImagePlay = (ImageView) findViewById(R.id.imagePlay1);
		mImagePlay.setOnClickListener(this);
		mImageUrl = (ImageView) findViewById(R.id.imageUrl);
		mMusicName = (TextView) findViewById(R.id.musicName);
		mSinger = (TextView) findViewById(R.id.singer);
		mWelcome= (TextView) findViewById(R.id.welcomeText);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent service = new Intent(MainActivity.this, MyService.class);
		startService(service);
		bindService(service, conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindService(conn);
	}

	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = ((MyBinder) service).getSystem();
			
			// 从服务中获得音乐播放状态
			boolean isPlay = mBinder.getisPlay();
			if (isPlay) {
				mImagePlay.setImageResource(R.drawable.ic_pause);
			} else {
				// 后台没有播放状态时
				mImagePlay.setImageResource(R.drawable.ic_play);
			}
			// 从服务中获得已经有的音乐对象
			Music music = mBinder.getMusic();
			// 当音乐对象不为空时，为空时则默认状态
			if (music != null) {
				//有音乐时，显示信息，隐藏欢迎文字
				mWelcome.setVisibility(View.INVISIBLE);
				mImageUrl.setVisibility(View.VISIBLE);
				mMusicName.setVisibility(View.VISIBLE);
				mSinger.setVisibility(View.VISIBLE);
				//从服务中获得图片集合
				HashMap<Integer, Bitmap> imageMap = mBinder.getImageMap();
				//从服务中获得图片集合index下标，不可
				int index = mBinder.getIndex();
				Bitmap bitmap = imageMap.get(index);
				//需要判断为空时，异步下载图片
				if(bitmap == null){
					ImageAsync imageAsync = new ImageAsync();
					imageAsync.execute(music.imageUrl);
				}else{
				mImageUrl.setImageBitmap(bitmap);
				}
				mMusicName.setText(music.musicName);
				mSinger.setText(music.singer);
			}
		}
	};
	class ImageAsync extends AsyncTask<String, Void, Bitmap>{

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			try {
				URL url = new URL(params[0]);
				InputStream is = url.openStream();
				bitmap = BitmapFactory.decodeStream(is);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bitmap;
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			mImageUrl.setImageBitmap(result);
		}
	}

	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.onlineBtn:
			Intent intent = new Intent(MainActivity.this, OnlineActivity.class);
			startActivity(intent);
			break;
		case R.id.recentPlayBtn:
			Intent intent2 = new Intent(MainActivity.this, RecentPlayActivity.class);
			startActivity(intent2);
			break;
		case R.id.myFavoriteBtn:
			Intent intent3 = new Intent(MainActivity.this, MyFavoriteActivity.class);
			startActivity(intent3);
			break;
		case R.id.downRelativeLayout:
			// 有音乐跳转，没有则不跳转
			if (mBinder.getMusic() == null) {
				Toast.makeText(MainActivity.this, "歌曲列表为空", Toast.LENGTH_SHORT).show();
			} else {
				Intent intent1 = new Intent(MainActivity.this,
						PlayMusicActivity.class);
				startActivity(intent1);
			}
			break;
		case R.id.imagePlay1:
			// 从服务中获得音乐播放状态
			boolean isPlay = mBinder.getisPlay();
			//点击时，播放状态取反
			isPlay = !isPlay;
			// 从服务中获得已经有的音乐对象
			Music music = mBinder.getMusic();
			if (music == null) {
				Toast.makeText(MainActivity.this, "音乐为空", Toast.LENGTH_SHORT).show();
			} else {
				//当切换为播放状态时
				if (isPlay) {
					mImagePlay.setImageResource(R.drawable.ic_pause);
					//已改BUG：点击没有播放
					//有音乐，播放或继续播放
					mBinder.reStartPlay();
					//将更新的播放状态，重新保存到服务中
					mBinder.setIsPlay(true);
				} else {
					// 当切换为暂停状态时
					mImagePlay.setImageResource(R.drawable.ic_play);
					//暂停
					mBinder.playPause();
					mBinder.setIsPlay(false);
				}
			}
			break;
		default:
			break;
		}
	}

}
