package com.example.musicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
public class RecentEditActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private TextView mTv_allCheck, mTv_finish;
	private TextView mTv_edit;
	private ListView mListView;
	private ArrayList<Music> dataList = new ArrayList<Music>();
	private HashMap<Integer, Boolean> stateMap = new HashMap<Integer, Boolean>();
	private MyAdapter mAdapter;
	private int number = 0;
	private SQLiteDatabase mDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recent_edit);
		MySqlite mySqlite = new MySqlite(this);
		mDb = mySqlite.getReadableDatabase();
		mTv_allCheck = (TextView) findViewById(R.id.textView1);
		mTv_edit = (TextView) findViewById(R.id.textView2);
		mTv_finish = (TextView) findViewById(R.id.textView3);
		mTv_allCheck.setOnClickListener(this);
		mTv_finish.setOnClickListener(this);
		findViewById(R.id.button1).setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.listView1);
		mListView.setOnItemClickListener(this);
		mAdapter = new MyAdapter();
		mListView.setAdapter(mAdapter);
		getData();
	}

	private void getData() {
		// 清空容器
		dataList.clear();
		// Sqlite查询
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
		// 倒序
		Collections.reverse(dataList);
		mAdapter.notifyDataSetChanged();
	}

	class MyAdapter extends BaseAdapter {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.list_recent_edit,
					null);
			ImageView image = (ImageView) view.findViewById(R.id.imageView1);
			TextView tv_musicName = (TextView) view
					.findViewById(R.id.textView1_list);

			Music music = dataList.get(position);
			tv_musicName.setText(music.musicName);

			Boolean state = stateMap.get(position);
			if (state == null) {
				image.setImageResource(R.drawable.order_item_default);
			} else if (state) {
				image.setImageResource(R.drawable.order_item_mark);
			} else {
				image.setImageResource(R.drawable.order_item_default);
			}

			return view;
		}

		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.textView1:
			// 文本显示
			showText();
			break;
		case R.id.button1:
			// 删除操作
			deleteEdit();
			break;
		case R.id.textView3:
			finish();
			break;
		default:
			break;
		}
	}

	private void deleteEdit() {
		// 改BUG：stateMap.size()
		// 反序遍历dataList,获得已选的行
		for (int i = dataList.size(); i >= 0; i--) {
			// 得到的Boolean值
			Boolean state = stateMap.get(i);
			Log.e("", "" + i);
			// 注意：先判断Boolean值为null的情况，否则报空指针
			if (state == null) {
			} else if (state) {
				// 删除行内容
				dataList.remove(i);
				stateMap.remove(i);
			}
		}
		// 计数清零
		number = 0;
		mTv_edit.setText("批量编辑");
		mTv_allCheck.setText("全选");
		mAdapter.notifyDataSetChanged();
	}

	private void showText() {

		// 点击'全选'时
		String msg = mTv_allCheck.getText().toString().trim();
		if (msg.equals("全选")) {
			// 遍历，设置所有行state为true
			for (int i = 0; i < dataList.size(); i++) {
				stateMap.put(i, true);
				number = dataList.size();
				mTv_edit.setText("已选" + number + "首");
				mTv_allCheck.setText("全不选");
			}
		} else if (msg.equals("全不选")) {
			// 所有行state为false
			for (int i = 0; i < dataList.size(); i++) {
				stateMap.put(i, false);
			}
			// 计数清零
			number = 0;
			mTv_edit.setText("批量编辑");
			mTv_allCheck.setText("全选");
		}
		mAdapter.notifyDataSetChanged();

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// 获取行当前boolean值，设置勾选图片
		Boolean state = stateMap.get(position);
		// 当初始状态为null时，点击则设置为选中状态
		if (state == null) {
			// number用于计数
			number++;
			stateMap.put(position, true);
			// 当初始状态为true时，点击则设置为不选状态
		} else if (state) {
			number--;
			stateMap.put(position, false);
		} else {
			number++;
			stateMap.put(position, true);
		}
		// 当行全部选择时
		if (number == dataList.size()) {
			mTv_edit.setText("已选" + number + "首");
			mTv_allCheck.setText("全不选");
		} else {
			// 当没有行选择时
			if (number == 0) {
				mTv_edit.setText("批量编辑");
				mTv_allCheck.setText("全选");
			} else {
				mTv_edit.setText("已选" + number + "首");
				mTv_allCheck.setText("全选");
			}
		}
		mAdapter.notifyDataSetChanged();
	}

}
