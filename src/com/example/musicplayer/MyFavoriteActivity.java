package com.example.musicplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MyFavoriteActivity extends Activity implements OnItemClickListener {

	private MyAdapter mAdapter;
	private ListView mListView;
	private SQLiteDatabase mDb;
	private ArrayList<Music> dataList = new ArrayList<Music>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_favorite);
		MySqlite mySqlite = new MySqlite(this);
		mDb = mySqlite.getReadableDatabase();
		mAdapter = new MyAdapter();
		mListView = (ListView) findViewById(R.id.listView_favorite);
		mListView.setOnItemClickListener(this);
		mListView.setAdapter(mAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		dataList.clear();
		// 查询ilike表中isFavorite为1
		Cursor query = mDb.query("musicdata", null, "isFavorite=?",
				new String[] { 1 + "" }, null, null, null);
		while (query.moveToNext()) {
				String musicName = query.getString(query
						.getColumnIndex("musicName"));
				String singer = query.getString(query.getColumnIndex("singer"));
				String imageUrl = query.getString(query
						.getColumnIndex("imageUrl"));
				String musicUrl = query.getString(query
						.getColumnIndex("musicUrl"));
				int isFavorite = query.getInt(query
						.getColumnIndex("isFavorite"));
				dataList.add(new Music(musicName, singer, imageUrl, musicUrl,
						isFavorite));
			}
			mAdapter.notifyDataSetChanged();
		}

	class MyAdapter extends BaseAdapter {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.list_favorite,
					null);

			TextView tv_musicName = (TextView) view
					.findViewById(R.id.textMusicname1);
			TextView tv_singer = (TextView) view.findViewById(R.id.textSinger1);
			Music music = dataList.get(position);
			tv_musicName.setText(music.musicName);
			tv_singer.setText(music.singer);

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
			Intent intent = new Intent(MyFavoriteActivity.this,PlayMusicActivity.class);
			//bug : positon与表中的不对应
			Music music = dataList.get(position);
			//传递相应音乐的musicUrl
			intent.putExtra("musicUrl", music.musicUrl);
			startActivity(intent );
	}

}
