package com.example.musicplayer;

import java.util.ArrayList;

import com.example.musicplayer.MyService.MyBinder;
import com.example.musicplayer.OnlineActivity.imageAsync;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LocalSongActivity extends Activity implements OnItemClickListener,
		OnClickListener {
	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
				Log.e("", "ACTION_MEDIA_SCANNER_STARTED");

				mBar.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.INVISIBLE);
				mText.setVisibility(View.INVISIBLE);
			} else if (intent.getAction().equals(
					Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				Log.e("", "ACTION_MEDIA_SCANNER_FINISHED");
				mBar.setVisibility(View.INVISIBLE);
				mListView.setVisibility(View.VISIBLE);
				mText.setVisibility(View.INVISIBLE);
				qureyLocalSong();
			}

		}
	};

	@SuppressLint("NewApi")
	private void qureyLocalSong() {
		ContentResolver resolver = getContentResolver();
		Cursor query = resolver.query(Media.EXTERNAL_CONTENT_URI, null, null,
				null, null, null);
		while (query.moveToNext()) {
			String musicName = query.getString(query
					.getColumnIndex(Media.TITLE));
			String singer = query.getString(query.getColumnIndex(Media.ARTIST));
			String musicUrl = query.getString(query.getColumnIndex(Media.DATA));
			String imageUrl = "";
			int isFavorite = 0;
			musicList.add(new Music(musicName, singer, imageUrl, musicUrl,
					isFavorite));
		}
		mAdapter.notifyDataSetChanged();

	}

	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = ((MyBinder) service).getSystem();
		}
	};

	private ListView mListView;
	private MyAdapter mAdapter;
	private MyService mBinder;
	private ArrayList<Music> musicList = new ArrayList<Music>();

	private TextView mText;
	private ProgressBar mBar;
	private ImageView mScanner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_song);
		mText = (TextView) findViewById(R.id.textLocalSong);
		mBar = (ProgressBar) findViewById(R.id.progressBarLocal);
		mScanner = (ImageView) findViewById(R.id.imageScanner);
		mScanner.setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.listLocalSong);
		mListView.setOnItemClickListener(this);
		mAdapter = new MyAdapter();
		mListView.setAdapter(mAdapter);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addDataScheme("file");
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent service = new Intent(LocalSongActivity.this, MyService.class);
		startService(service);
		bindService(service, conn, Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(receiver);
		unbindService(conn);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		// APi版本
		intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
		String data = "file://"
				+ Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music";
		intent.setData(Uri.parse(data));
		Log.e("", data + "");
		sendBroadcast(intent);
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return musicList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.list_localsong,
					null);
			ImageView image = (ImageView) view
					.findViewById(R.id.imageLocalSong);
			TextView tv_indicator = (TextView) view
					.findViewById(R.id.indicator);
			TextView tv_musicName = (TextView) view
					.findViewById(R.id.textLocalSong1);
			TextView tv_singer = (TextView) view
					.findViewById(R.id.textLocalSong2);
			image.setImageResource(R.drawable.iconfonttupian);
			Music music = musicList.get(position);
			tv_musicName.setText(music.musicName);
			tv_singer.setText(music.singer);

			// 确保mBinder对象不为空,BUG1不能及时更新
			if (mBinder != null) {
				if (music.musicUrl.equals(mBinder.getPath())) {
					tv_indicator.setVisibility(View.VISIBLE);
				} else {
					tv_indicator.setVisibility(View.INVISIBLE);
				}
			}
			return view;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// 将存放音乐列表的容器传到服务中
		mBinder.setMusicList(musicList);
		// 获取行点击对应的Music
		Music music = musicList.get(position);
		Log.e("", "" + music.musicName);
		// 将得到的Music传入到服务中
		mBinder.setMusic(music);
		// 行点击时，设置下标到服务中
		mBinder.setIndex(position);
		// 从服务中获得已经有的音乐路径
		String path = mBinder.getPath();
		// 通过音乐path和musicList是否相等，来判断是否为同一首歌
		if (!path.equals(music.musicUrl)) {
			// 通过playMusic传参，将音乐路径传入到服务中
			mBinder.playMusic(music.musicUrl);
			mBinder.setIsPlay(true);
			Log.e("", "playMusic");
		} else {
			// 当再次点击同一首音乐，同时是播放状态时，执行playPause
			if (mBinder.getisPlay()) {
				mBinder.playPause();
				mBinder.setIsPlay(false);
			} else {
				// 当再次点击同一首音乐，且不是播放状态时，执行reStartPlay
				mBinder.reStartPlay();
				mBinder.setIsPlay(true);
			}
		}
		// 保存为已经播放过
		mBinder.setRecently(true);
		// 点击时需要改变listview的显示，所以需要更新适配器
		mAdapter.notifyDataSetChanged();

	}

}
