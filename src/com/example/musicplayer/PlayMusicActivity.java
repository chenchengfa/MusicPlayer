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
	// ��'��ϲ��'�еõ��ĵ�ַ����ӷ����еõ���
	private String mPath = "";
	// ����ģʽ
	private int mode = 0;
	private ImageView mImageMode;
	private Toast mToast;
	private CircleImageView mCustom_image;
	private TextView mMusicName;
	private Music music = null;

	// ÿ�ο�ʼ����ʱ��ʹ�ù㲥������
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// �����������ơ��Զ���ImageViewͼƬ
			showCustomImage();
		}
	};

	private void showCustomImage() {

		music = mBinder.getMusic();
		if (music != null) {
			// ÿ�ο�ʼ����ʱ��������������
			mMusicName.setText(music.musicName);
			HashMap<Integer, Bitmap> mImageMap = mBinder.getImageMap();
			// �ӷ����л��ͼƬ����index�±꣬����
			int index = mBinder.getIndex();
			Bitmap bm = mImageMap.get(index);
			if (bm == null) {
				ImageAsync imageAsync = new ImageAsync();
				imageAsync.execute(music.imageUrl);
			} else {
				// ���µ��Զ���ImageView��
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

	// ��ȡ'��ϲ��'���ݹ���������
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
		// ע��㲥�������������ơ��Զ���ͼƬ����
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
			// �����������ơ��Զ���ImageViewͼƬ
			showCustomImage();
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
		super.onStop();
		// �˳�ʱ�����
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
			// ����һ�β���ʱ,ִ��playMusic��refresh���½�����
			if (isFirst) {
				mPlay.setImageResource(R.drawable.ic_pause);
				// �ӷ����еõ����ֵ�ַ
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
		// ���ò���ģʽ
		case R.id.imageMode:
			chanceMode();
			break;
		case R.id.imagePlayUp:
			mBinder.playUpMusic();
			// �����µĲ���״̬�����±��浽������
			mBinder.setIsPlay(isPlay);
			break;
		case R.id.imagePlayDown:
			mBinder.playUpMusic();
			// �����µĲ���״̬�����±��浽������
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
		// ѭ��
		case 0:
			// ����ͼƬ
			mImageMode.setImageResource(R.drawable.player_btn_repeat_normal);
			// ֪ͨ��ʾ
			showToast("���л����Զ�����");
			break;
		// ����
		case 1:
			// ����ͼƬ
			mImageMode.setImageResource(R.drawable.player_btn_repeatone_normal);
			showToast("���л�������ѭ��");
			break;
		// ���
		case 2:
			// ����ͼƬ
			mImageMode.setImageResource(R.drawable.player_btn_random_normal);
			showToast("���л����������");
			Log.e("", "" + mode);
			break;
		default:
			break;
		}
		// ���浽������
		mBinder.setMode(mode);
	}

	// �Զ��塰����ģʽ��֪ͨ
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
