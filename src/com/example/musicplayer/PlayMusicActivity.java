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
	//��'��ϲ��'�еõ��ĵ�ַ����ӷ����еõ���
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

	// ��ȡ'��ϲ��'���ݹ���������
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
			// ���Ӻ�̨����ʱ������״̬��֮ǰһ��
			isFavorite = mBinder.getIsFavorite();
			if (isFavorite == 1) {
				isCollect = true;
				mImageIlike.setImageResource(R.drawable.record_card_like_s);
			} else if (isFavorite == 0) {
				isCollect = false;
				mImageIlike.setImageResource(R.drawable.record_card_like_h);
			}

			// ��Pause���󣬽������´Ӻ�̨����������û��ִ��refresh����������
			if (mBinder.isLoad()) {
				int duration = mBinder.getDuration();
				mTotalTime.setText(timeFormat(duration));
				// ���ý��������Ⱥ����ֳ������
				mSeekBar.setMax(duration);
			}
			// ��̨���ڲ���״̬ʱ
			if (isPlay) {
				mPlay.setImageResource(R.drawable.ic_pause);
				refresh();
				Log.e("", "onServiceConnected==Play");
			} else {
				// ��̨û�в���״̬ʱ
				mPlay.setImageResource(R.drawable.ic_play);
				Log.e("", "onServiceConnected==Pause");
				// ��Pause��������������̨���������½�����
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
		// �ӷ����л�ȡmusic����
		Music music = mBinder.getMusic();
		// ����isFavorite�����ݿ���
		ContentValues values = new ContentValues();
		values.put("isFavorite", isFavorite);
		// ����musicUrl���ֵ�ַ���и���
		mDb.update("musicdata", values, "musicUrl=?",
				new String[] { music.musicUrl + "" });
		Log.e("", "===" + isFavorite);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//�˳�ʱ�����
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
			// ����һ�β���ʱ,ִ��playMusic��refresh���½�����
			if (isFirst) {
				mPlay.setImageResource(R.drawable.ic_pause);
				//�ӷ����еõ����ֵ�ַ
				mPath = mBinder.getPath();
				mBinder.playMusic(mPath);
				refresh();
				Log.e("", "isFirst---playMusic");
				// ����isFirst��ʶλ
				isFirst = false;
			} else {
				// �����ǵ�һ�β���ʱ,��ΪreStartPlay��������
				if (isPlay) {
					mPlay.setImageResource(R.drawable.ic_pause);
					refresh();
					mBinder.reStartPlay();
					Log.e("", "isPlay---reStartPlay");
				} else {
					// ��ͣ����ʱ,ִ��playPause�������½�����
					mPlay.setImageResource(R.drawable.ic_play);
					mBinder.playPause();
					Log.e("", " ---playPause");
				}
			}
			// �����µĲ���״̬�����±��浽������
			mBinder.setIsPlay(isPlay);
			break;
		case R.id.imageIlike:
			// �ж��Ƿ�Ϊ�ղ�
			isCollect = !isCollect;
			if (isCollect) {
				// ����'��ϲ��'��ʶλ
				isFavorite = 1;
				// ��isFavoriteֵ���浽������
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

	// ���½�������ʱ����ʾ
	private void refresh() {
		if (mAction == null) {
			mAction = new Runnable() {
				@Override
				public void run() {
					if (isPlay == false) {
						mAction = null;
						return;
					}

					// //isLoadΪTrue������mTotalTimeֵ
					if (mBinder.isLoad()) {
						int duration = mBinder.getDuration();
						mTotalTime.setText(timeFormat(duration));
						// ���ý��������Ⱥ����ֳ������
						mSeekBar.setMax(duration);
					}
					// ���϶�����ʱ,����������λ����
					if (!isTouch) {
						int i = mBinder.getCurrentPosition();
						mSeekBar.setProgress(i);
						mStartTime.setText(timeFormat(i));
					}
					// ������1Sǰ��һ��
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
			// //���϶������У���ʾmStartTimeʱ��仯
			mStartTime.setText(timeFormat(progress));
			mSeekBar.setProgress(progress);

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			isTouch = true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// �϶��Ƿ������ʾ�����϶�����ʱ���Ÿ���
			isTouch = false;
			// ���϶�����ʱ��MediaPlay���¶�λ����λ��
			mBinder.mySeekTo(mSeekBar.getProgress());
		}

	}

}
