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
			// �ı���ʾ
			showText();
			break;
		case R.id.button1:
			// ɾ������
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
		// ��BUG��stateMap.size()
		// �������dataList,�����ѡ����
		for (int i = dataList.size(); i >= 0; i--) {
			// �õ���Booleanֵ
			Boolean state = stateMap.get(i);
			Log.e("", "" + i);
			// ע�⣺���ж�BooleanֵΪnull����������򱨿�ָ��
			if (state == null) {
			} else if (state) {
				// ɾ��������
				dataList.remove(i);
				stateMap.remove(i);
			}
		}
		// ��������
		number = 0;
		mTv_edit.setText("�����༭");
		mTv_allCheck.setText("ȫѡ");
		mAdapter.notifyDataSetChanged();
	}

	private void showText() {

		// ���'ȫѡ'ʱ
		String msg = mTv_allCheck.getText().toString().trim();
		if (msg.equals("ȫѡ")) {
			// ����������������stateΪtrue
			for (int i = 0; i < dataList.size(); i++) {
				stateMap.put(i, true);
				number = dataList.size();
				mTv_edit.setText("��ѡ" + number + "��");
				mTv_allCheck.setText("ȫ��ѡ");
			}
		} else if (msg.equals("ȫ��ѡ")) {
			// ������stateΪfalse
			for (int i = 0; i < dataList.size(); i++) {
				stateMap.put(i, false);
			}
			// ��������
			number = 0;
			mTv_edit.setText("�����༭");
			mTv_allCheck.setText("ȫѡ");
		}
		mAdapter.notifyDataSetChanged();

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// ��ȡ�е�ǰbooleanֵ�����ù�ѡͼƬ
		Boolean state = stateMap.get(position);
		// ����ʼ״̬Ϊnullʱ�����������Ϊѡ��״̬
		if (state == null) {
			// number���ڼ���
			number++;
			stateMap.put(position, true);
			// ����ʼ״̬Ϊtrueʱ�����������Ϊ��ѡ״̬
		} else if (state) {
			number--;
			stateMap.put(position, false);
		} else {
			number++;
			stateMap.put(position, true);
		}
		// ����ȫ��ѡ��ʱ
		if (number == dataList.size()) {
			mTv_edit.setText("��ѡ" + number + "��");
			mTv_allCheck.setText("ȫ��ѡ");
		} else {
			// ��û����ѡ��ʱ
			if (number == 0) {
				mTv_edit.setText("�����༭");
				mTv_allCheck.setText("ȫѡ");
			} else {
				mTv_edit.setText("��ѡ" + number + "��");
				mTv_allCheck.setText("ȫѡ");
			}
		}
		mAdapter.notifyDataSetChanged();
	}

}
