package com.example.musicplayer;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.musicplayer.MyService.MyBinder;

public class RecentPlayActivity extends Activity implements OnItemClickListener, OnClickListener {

	private ListView mListView;
	private MyAdapter mAdapter;
	private SQLiteDatabase mDb;
	private ArrayList<Music> dataList = new ArrayList<Music>();
	private MyService mBinder;
	private ImageView mJumpEdit;
	 
	
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			 initData();
			Log.e("", "RecentPlayActivity -----onReceive");
		}
	};
	// ��������
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = ((MyBinder) service).getSystem();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recent_play);
		mJumpEdit = (ImageView) findViewById(R.id.imageEdit);
		mJumpEdit.setOnClickListener(this);
		MySqlite mySqlite = new MySqlite(this);
		mDb = mySqlite.getReadableDatabase();
		mAdapter = new MyAdapter();
		mListView = (ListView) findViewById(R.id.listView1);
		mListView.setOnItemClickListener(this);
		initData();
		mListView.setAdapter(mAdapter);
	}

	// �򿪽���ʱ����ȡ���ݿ�
	private void initData() {
		// �������
		dataList.clear();
		// Sqlite��ѯ
		Cursor query = mDb.query("musicdata", null, null, null, null, null,
				null, null);
		while (query.moveToNext()) {
			String musicName = query.getString(query
					.getColumnIndex("musicName"));
			String singer = query.getString(query.getColumnIndex("singer"));
			String imageUrl = query.getString(query.getColumnIndex("imageUrl"));
			String musicUrl = query.getString(query.getColumnIndex("musicUrl"));
			int isFavorite = query.getInt(query
					.getColumnIndex("isFavorite"));
			dataList.add(new Music(musicName, singer, imageUrl, musicUrl,isFavorite));
		}
		// ����
		Collections.reverse(dataList);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(
				"android.action.broadcast.MyRecentPlay");
		registerReceiver(receiver, filter);
		Intent service = new Intent(RecentPlayActivity.this, MyService.class);
		startService(service);
		bindService(service, conn, Context.MODE_APPEND);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(receiver);
		unbindService(conn);
	}

	class MyAdapter extends BaseAdapter {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.list_recentplay,
					null);
			TextView tv_indicator = (TextView) view
					.findViewById(R.id.indicator1);
			TextView tv_musicName = (TextView) view
					.findViewById(R.id.musicRecent);
			TextView tv_singer = (TextView) view
					.findViewById(R.id.signerRecent);
			Music music = dataList.get(position);
			tv_musicName.setText(music.musicName);
			tv_singer.setText(music.singer);
			// ȷ��mBinder����Ϊ��
			if (mBinder != null) {
				if (music.musicUrl.equals(mBinder.getPath())) {
					tv_indicator.setVisibility(View.VISIBLE);
				} else {
					tv_indicator.setVisibility(View.INVISIBLE);
				}
			}
			return view;
		}

		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		//ȥ��listViewͷ��
		position = position -mListView.getHeaderViewsCount();
		if(position>0){
			// ������е�music���������
			Music music = dataList.get(position);
			mBinder.setMusic(music);
			//������е�isRecentlyֵ������
			mBinder.setRecently(true);
			// �е��ʱ�������±굽������,��BUG����online�ǲ�ͬ��listView
			mBinder.setIndex(position);
			// �ӷ����л���Ѿ��е�����·��
			String path = mBinder.getPath();
			// ͨ������path��musicList�Ƿ���ȣ����ж��Ƿ�Ϊͬһ�׸�
			if (!path.equals(music.musicUrl)) {
				// ͨ��playMusic���Σ�������·�����뵽������
				mBinder.playMusic(music.musicUrl);
				mBinder.setIsPlay(true);
			} else {
				// ���ٴε��ͬһ�����֣�ͬʱ�ǲ���״̬ʱ��ִ��playPause
				if (mBinder.getisPlay()) {
					mBinder.playPause();
					mBinder.setIsPlay(false);
				} else {
					// ���ٴε��ͬһ�����֣��Ҳ��ǲ���״̬ʱ��ִ��reStartPlay
					mBinder.reStartPlay();
					mBinder.setIsPlay(true);
				}
			}
			mAdapter.notifyDataSetChanged();
		}
		}

	@Override
	public void onClick(View v) {
			switch (v.getId()) {
			case R.id.imageEdit:
				Intent intent = new Intent(RecentPlayActivity.this, RecentEditActivity.class);
				startActivity(intent );
				break;
			default:
				break;
			}
		
	}
}
