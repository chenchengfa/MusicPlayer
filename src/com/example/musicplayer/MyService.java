package com.example.musicplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;

public class MyService extends Service {
	// 是否为播放状态
	private boolean isPlay = false;
	// 是否第一次点击
	private boolean isFirst = true;
	// 下载完成否
	private boolean isLoad = false;
	// 是否为最近播放界面
	private boolean isRecently = false;
	// 是否选中我喜欢
	private int isFavorite = 0;
	// 播放模式
	private int mode = 0;

	// 设置音乐对象
	private Music music = null;
	// 定义position
	private int index = 0;
	private MediaPlayer mMediaPlayer;
	// String path = "http://192.168.112.3/juejiang.mp3";
	String path = "";
	// 创建imageMap容器
	private HashMap<Integer, Bitmap> imageMap = new HashMap<Integer, Bitmap>();
	// 创建music容器
	private ArrayList<Music> musicList = new ArrayList<Music>();

	// 获取music容器
	public ArrayList<Music> getMusicList() {
		return musicList;
	}

	// 设置music容器
	public void setMusicList(ArrayList<Music> musicList) {
		// 清空容器中之前的列表
		this.musicList.clear();
		// 将得到的容器内容全部加
		this.musicList.addAll(musicList);
	}

	private SQLiteDatabase mDb;

	public MyService() {
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// MyService启动时就创建MediaPlayer对象
		mMediaPlayer = new MediaPlayer();
		MySqlite musicSQL = new MySqlite(this);
		mDb = musicSQL.getReadableDatabase();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new MyBinder();
	}

	class MyBinder extends Binder {
		public MyService getSystem() {
			return MyService.this;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMediaPlayer.release();
	}

	public boolean getisPlay() {
		return isPlay;
	}

	public void setIsPlay(boolean isPlay) {
		MyService.this.isPlay = isPlay;
	}

	// 是否为最近播放界面
	public boolean isRecently() {
		return isRecently;
	}

	public void setRecently(boolean isRecently) {
		this.isRecently = isRecently;
	}

	// 得到MediaPlayer的当前播放位置
	public int getCurrentPosition() {
		return mMediaPlayer.getCurrentPosition() / 1000;
	}

	// 获取音乐总长度
	public int getDuration() {
		return mMediaPlayer.getDuration() / 1000;
	}

	// MediaPlayer重新定位播放位置
	public void mySeekTo(int msec) {
		mMediaPlayer.seekTo(msec * 1000);

	}

	public void playMusic(String path) {
		try {
			// 将传入的path赋给setPath
			this.setPath(path);
			// 刚执行playMusic时，设为未下载状态
			this.setIsLoad(false);
			// 一旦执行playMusic时，isFirst设为false
			this.setIsFirst(false);
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(path);
			// 异步任务,完成prepare
			mMediaPlayer.prepareAsync();
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					// //当onPrepared准备好时，isLoad设为true
					MyService.this.setIsLoad(true);
					mMediaPlayer.start();
					// 在最近播放状态时,有BUG
					// if(!isRecently){
					// 已经播放过时,先清除数据库中重复的音乐信息
					deleteSQL();
					// 将在线下载的音乐信息保存到数据库musicdata表中
					insertSQL();
					// 发送广播,重新读取数据库
					sendBroadcast(new Intent(
							"android.action.broadcast.MyRecentPlay"));
					// }
				}
			});
			//播放完监听方法
			completeListenr();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 播放完成监听
	private void completeListenr() {
		mMediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						// 判断模式
						switch (mode) {
						// 循环
						case 0:
							index++;
							index = index % musicList.size();
							music = musicList.get(index);
							MyService.this.playMusic(music.musicUrl);
							break;
						// 单曲
						case 1:
							playMusic(music.musicUrl);
							break;
						// 随机
						case 2:
							Random random = new Random();
							while (true) {
								int len = musicList.size();
								int nextInt = random.nextInt(len);
								// 当歌曲数大于、等于2首时
								if (len > 1) {
									if (index != nextInt) {
										index = nextInt;
										// 跳出循环
										break;
									}
									}else {
										index = nextInt;
										break;
									}

							}
							music = musicList.get(index);
							MyService.this.playMusic(music.musicUrl);
							break;
						default:
							break;
						}
					}
				});
	}

	// 先清除数据库中重复的音乐信息
	private void deleteSQL() {
		mDb.delete("musicdata", "musicUrl=?", new String[] { music.musicUrl });
	}

	// 将在线下载的音乐保存到数据库中
	private void insertSQL() {
		String table = "musicdata";
		ContentValues values = new ContentValues();
		values.put("musicName", music.musicName);
		values.put("singer", music.singer);
		values.put("imageUrl", music.imageUrl);
		values.put("musicUrl", music.musicUrl);
		values.put("isFavorite", music.isFavorite);
		mDb.insert(table, null, values);
	}

	public void playPause() {
		// 当onPrepared准备好时，才调用pause()
		if (isLoad()) {
			mMediaPlayer.pause();
		}
	}

	public void playStop() {
		mMediaPlayer.stop();
	}

	public void reStartPlay() {
		// 当onPrepared准备好时
		if (isLoad()) {
			mMediaPlayer.start();
		}
	}

	// 播放下一首
	public void playNextMusic() {
		index++;
		//当index大于列表长度时，置零
		if(index>musicList.size()-1){
			index = 0;
		}
		music = musicList.get(index);
		MyService.this.playMusic(music.musicUrl);
	}

	// 播放上一首
	public void playUpMusic() {
		index--;
		//当index小于0时，回到列表结尾
		if(index<0){
			index = musicList.size()-1;
		}
		music = musicList.get(index);
		MyService.this.playMusic(music.musicUrl);
	}

	public boolean getIsFirst() {
		return isFirst;
	}

	public void setIsFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	// 设置isLoad标识位
	public boolean isLoad() {
		return isLoad;
	}

	// 设置isLoad标识位
	public void setIsLoad(boolean isLoad) {
		this.isLoad = isLoad;
	}

	// 获取Music对象
	public Music getMusic() {
		return music;
	}

	// 设置上线下载得到Music对象
	public void setMusic(Music music) {
		this.music = music;
	}

	// 设置上线下载得到歌曲图片对象
	public void setImageMap(HashMap<Integer, Bitmap> imageMap) {
		// 每次设置前，先清除之前添加的图片
		MyService.this.imageMap.clear();
		// 不能直接用image赋值
		this.imageMap.putAll(imageMap);
	}

	// 获取歌曲图片对象
	public HashMap<Integer, Bitmap> getImageMap() {
		return imageMap;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int position) {
		this.index = position;
	}

	// 获取是否’我喜欢‘
	public int getIsFavorite() {
		return isFavorite;
	}

	public void setIsFavorite(int isFavorite) {
		this.isFavorite = isFavorite;
	}

	// 获取播放模式
	public int getMode() {
		return mode;
	}

	// 设置播放模式
	public void setMode(int mode) {
		this.mode = mode;
	}

}
