package com.example.musicplayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.musicplayer.MyService.MyBinder;

public class OnlineActivity extends Activity implements OnItemClickListener {

	private ArrayList<Music> musicList = new ArrayList<Music>();
	private HashMap<Integer, imageAsync> asyncMap = new HashMap<Integer, OnlineActivity.imageAsync>();
	private HashMap<Integer, Bitmap> imageMap = new HashMap<Integer, Bitmap>();
	//String path ="http://192.168.112.3/musics.txt";
	 String path = "http://ob7ysmkjg.bkt.clouddn.com/qiniu_musics.txt";
	private ListView listView;
	private MyAdapter mAdapter;
	private MyService mBinder=null;
	 
	
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = ((MyBinder) service).getSystem();
		}
	};
	private TextView mTimeOut;
	private ProgressBar mBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_online);
		Intent service = new Intent(OnlineActivity.this, MyService.class);
		startService(service);
		bindService(service, conn, Context.BIND_AUTO_CREATE);
		
		mTimeOut = (TextView) findViewById(R.id.timeOut);
		mBar = (ProgressBar) findViewById(R.id.progressBar1);
		listView = (ListView) findViewById(R.id.listView1);
		mAdapter = new MyAdapter();
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);
		
		FileAsync fileAsync = new FileAsync();
		fileAsync.execute(path);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindService(conn);
	}

	// ����Json�ĵ��첽����
	class FileAsync extends AsyncTask<String, Void, String> {
		//��ʼ�첽ʱ����ʾ������
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			listView.setVisibility(View.INVISIBLE);
			mTimeOut.setVisibility(View.INVISIBLE);
			mBar.setVisibility(View.VISIBLE);
		}
		@Override
		protected String doInBackground(String... params) {
			String result = "";
			InputStream  is=null;
			try {
				URL url = new URL(params[0]);
				try {
					URLConnection conn = url.openConnection();
					//�������ӳ�ʱ���ͻ��˷�����Ϣ�������
					conn.setConnectTimeout(5000);
					//��ȡ��ʱ������˷�����Ϣ���ͻ���
					conn.setReadTimeout(5000);
					is = conn.getInputStream();
					ByteArrayBuffer byteArray = new ByteArrayBuffer(1000);
					byte[] buffer = new byte[1024];
					int len = is.read(buffer);
					while (-1 != len) {
						byteArray.append(buffer, 0, len);
						Log.e("", "bbb" + buffer);
						len = is.read(buffer);
					}
					result = new String(byteArray.toByteArray());
					//�������Ϊ��ʱ�����ؽ�������򷵻�NUll
					if(result !=null){
						return result;
					}
				} catch (IOException e) {
					e.printStackTrace();
					//���糬ʱ�쳣ʱ����ʾ������ʾ
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							listView.setVisibility(View.INVISIBLE);
							mBar.setVisibility(View.INVISIBLE);
							mTimeOut.setVisibility(View.VISIBLE);
						}
					});
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}finally{
				//������������ʱ����������
				if(is!=null){
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//�����Ϊ��ʱ������NUll
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				JSONArray jsonArray = new JSONArray(result);
				for (int i = 0; i < jsonArray.length(); i++) {
					Log.e("", "ccc---" + i);
					JSONObject json = jsonArray.getJSONObject(i);
					String name = json.getString("name");
					String imageURL = json.getString("imageURL");
					String signer = json.getString("signer");
					String songURL = json.getString("songURL");
					Log.e("", "eee---" + songURL);
					musicList.add(new Music(name, signer, imageURL, songURL,0));
				}
				//�������ʱ����ʾlistView
				mBar.setVisibility(View.INVISIBLE);
				mTimeOut.setVisibility(View.INVISIBLE);
				listView.setVisibility(View.VISIBLE);
				//ˢ��������
				mAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	// ����ͼƬ�첽����
	class imageAsync extends AsyncTask<String, Void, Bitmap> {

		private ImageView mImage;
		private int mPosition;

		public imageAsync(int position, ImageView image) {
			mPosition = position;
			mImage = image;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			InputStream is=null;
			try {
				URL url = new URL(params[0]);
				URLConnection conn = url.openConnection();
				is = conn.getInputStream();
				bitmap = BitmapFactory.decodeStream(is);
				//�������Ϊ��ʱ�����ؽ�������򷵻�NUll
				if(bitmap!=null){
					return bitmap;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				//������������ʱ����������
				if(is !=null){
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//�����Ϊ��ʱ������NUll
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			mImage.setImageBitmap(result);
			// ���������ͼƬ����HashMap��
			imageMap.put(mPosition, result);
			// ���������ͼƬ���浽�����У�������������ʹ��
			if (mBinder != null) {
			mBinder.setImageMap(imageMap);
			}
			// ������ͼƬ����ʱ����������
			mAdapter.notifyDataSetChanged();
		}

		public void setImageChange(ImageView image) {
			mImage = image;
		}
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
			imageAsync imgAsync = null;
			View view = getLayoutInflater().inflate(R.layout.list_online, null);
			ImageView image = (ImageView) view.findViewById(R.id.imageView1);
			TextView tv_indicator = (TextView) view
					.findViewById(R.id.indicator);
			TextView tv_musicName = (TextView) view
					.findViewById(R.id.textView1);
			TextView tv_singer = (TextView) view.findViewById(R.id.textView2);
			image.setImageResource(R.drawable.iconfonttupian);
			Music music = musicList.get(position);
			tv_musicName.setText(music.musicName);
			tv_singer.setText(music.singer);

			Bitmap myImage = imageMap.get(position);
			// ��ȡ����ͼƬ
			if (myImage == null) {
				// ���Ѿ��������������첽����ʱ����ȡ�첽����
				imgAsync = asyncMap.get(position);
				if (imgAsync == null) {
					// �������������첽
					imgAsync = new imageAsync(position, image);
					imgAsync.execute(music.imageUrl);
					asyncMap.put(position, imgAsync);
				}
				// ����������û�и���ͼƬ�����첽�����Ѿ�����ʱ����ͼƬ���ϸ���
				imgAsync.setImageChange(image);
			} else {
				image.setImageBitmap(myImage);
			}
			// ȷ��mBinder����Ϊ��,BUG1���ܼ�ʱ����
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
		//����������б����������������
		mBinder.setMusicList(musicList);
		// ��ȡ�е����Ӧ��Music
		Music music = musicList.get(position);
		Log.e("", ""+music.musicName);
		// ���õ���Music���뵽������
		mBinder.setMusic(music);
		// �е��ʱ�������±굽������
		mBinder.setIndex(position);
		// �ӷ����л���Ѿ��е�����·��
		String path = mBinder.getPath();
		// ͨ������path��musicList�Ƿ���ȣ����ж��Ƿ�Ϊͬһ�׸�
		if (!path.equals(music.musicUrl)) {
			// ͨ��playMusic���Σ�������·�����뵽������
			mBinder.playMusic(music.musicUrl);
			mBinder.setIsPlay(true);
			Log.e("", "playMusic");
		} else {
			// ���ٴε��ͬһ�����֣�ͬʱ�ǲ���״̬ʱ��ִ��playPause
			if (mBinder.getisPlay()) {
				mBinder.playPause();
				mBinder.setIsPlay(false);
				Log.e("", "playPause");
			} else {
				// ���ٴε��ͬһ�����֣��Ҳ��ǲ���״̬ʱ��ִ��reStartPlay
				mBinder.reStartPlay();
				mBinder.setIsPlay(true);
				Log.e("", "reStartPlay");
			}
		}
		//����Ϊ�Ѿ����Ź�
		mBinder.setRecently(true);
		// ���ʱ��Ҫ�ı�listview����ʾ��������Ҫ����������
		mAdapter.notifyDataSetChanged();

	}

}
