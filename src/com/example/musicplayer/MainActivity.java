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
			
			// �ӷ����л�����ֲ���״̬
			boolean isPlay = mBinder.getisPlay();
			if (isPlay) {
				mImagePlay.setImageResource(R.drawable.ic_pause);
			} else {
				// ��̨û�в���״̬ʱ
				mImagePlay.setImageResource(R.drawable.ic_play);
			}
			// �ӷ����л���Ѿ��е����ֶ���
			Music music = mBinder.getMusic();
			// �����ֶ���Ϊ��ʱ��Ϊ��ʱ��Ĭ��״̬
			if (music != null) {
				//������ʱ����ʾ��Ϣ�����ػ�ӭ����
				mWelcome.setVisibility(View.INVISIBLE);
				mImageUrl.setVisibility(View.VISIBLE);
				mMusicName.setVisibility(View.VISIBLE);
				mSinger.setVisibility(View.VISIBLE);
				//�ӷ����л��ͼƬ����
				HashMap<Integer, Bitmap> imageMap = mBinder.getImageMap();
				//�ӷ����л��ͼƬ����index�±꣬����
				int index = mBinder.getIndex();
				Bitmap bitmap = imageMap.get(index);
				//��Ҫ�ж�Ϊ��ʱ���첽����ͼƬ
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
			// ��������ת��û������ת
			if (mBinder.getMusic() == null) {
				Toast.makeText(MainActivity.this, "�����б�Ϊ��", Toast.LENGTH_SHORT).show();
			} else {
				Intent intent1 = new Intent(MainActivity.this,
						PlayMusicActivity.class);
				startActivity(intent1);
			}
			break;
		case R.id.imagePlay1:
			// �ӷ����л�����ֲ���״̬
			boolean isPlay = mBinder.getisPlay();
			//���ʱ������״̬ȡ��
			isPlay = !isPlay;
			// �ӷ����л���Ѿ��е����ֶ���
			Music music = mBinder.getMusic();
			if (music == null) {
				Toast.makeText(MainActivity.this, "����Ϊ��", Toast.LENGTH_SHORT).show();
			} else {
				//���л�Ϊ����״̬ʱ
				if (isPlay) {
					mImagePlay.setImageResource(R.drawable.ic_pause);
					//�Ѹ�BUG�����û�в���
					//�����֣����Ż��������
					mBinder.reStartPlay();
					//�����µĲ���״̬�����±��浽������
					mBinder.setIsPlay(true);
				} else {
					// ���л�Ϊ��ͣ״̬ʱ
					mImagePlay.setImageResource(R.drawable.ic_play);
					//��ͣ
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
