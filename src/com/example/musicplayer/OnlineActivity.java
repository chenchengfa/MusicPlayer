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

	// 下载Json文档异步任务
	class FileAsync extends AsyncTask<String, Void, String> {
		//开始异步时，显示进度条
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
					//网络连接超时，客户端发送信息给服务端
					conn.setConnectTimeout(5000);
					//读取超时，服务端发送信息给客户端
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
					//当结果不为空时，返回结果；否则返回NUll
					if(result !=null){
						return result;
					}
				} catch (IOException e) {
					e.printStackTrace();
					//网络超时异常时，显示文体提示
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
				//当存在输入流时，关流操作
				if(is!=null){
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//当结果为空时，返回NUll
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
				//更新完成时，显示listView
				mBar.setVisibility(View.INVISIBLE);
				mTimeOut.setVisibility(View.INVISIBLE);
				listView.setVisibility(View.VISIBLE);
				//刷新适配器
				mAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	// 更新图片异步任务
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
				//当结果不为空时，返回结果；否则返回NUll
				if(bitmap!=null){
					return bitmap;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				//当存在输入流时，关流操作
				if(is !=null){
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//当结果为空时，返回NUll
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			mImage.setImageBitmap(result);
			// 将下载完的图片保存HashMap中
			imageMap.put(mPosition, result);
			// 将下载完的图片保存到服务中，用于其它界面使用
			if (mBinder != null) {
			mBinder.setImageMap(imageMap);
			}
			// 下载完图片，及时更新适配器
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
			// 获取歌曲图片
			if (myImage == null) {
				// 当已经创建音乐下载异步任务时，获取异步任务
				imgAsync = asyncMap.get(position);
				if (imgAsync == null) {
					// 创建音乐下载异步
					imgAsync = new imageAsync(position, image);
					imgAsync.execute(music.imageUrl);
					asyncMap.put(position, imgAsync);
				}
				// 当网速慢，没有歌曲图片，但异步任务已经创建时，将图片不断更新
				imgAsync.setImageChange(image);
			} else {
				image.setImageBitmap(myImage);
			}
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
		//将存放音乐列表的容器传到服务中
		mBinder.setMusicList(musicList);
		// 获取行点击对应的Music
		Music music = musicList.get(position);
		Log.e("", ""+music.musicName);
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
				Log.e("", "playPause");
			} else {
				// 当再次点击同一首音乐，且不是播放状态时，执行reStartPlay
				mBinder.reStartPlay();
				mBinder.setIsPlay(true);
				Log.e("", "reStartPlay");
			}
		}
		//保存为已经播放过
		mBinder.setRecently(true);
		// 点击时需要改变listview的显示，所以需要更新适配器
		mAdapter.notifyDataSetChanged();

	}

}
