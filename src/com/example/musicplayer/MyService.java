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
	// �Ƿ�Ϊ����״̬
	private boolean isPlay = false;
	// �Ƿ��һ�ε��
	private boolean isFirst = true;
	// ������ɷ�
	private boolean isLoad = false;
	// �Ƿ�Ϊ������Ž���
	private boolean isRecently = false;
	// �Ƿ�ѡ����ϲ��
	private int isFavorite = 0;
	// ����ģʽ
	private int mode = 0;

	// �������ֶ���
	private Music music = null;
	// ����position
	private int index = 0;
	private MediaPlayer mMediaPlayer;
	// String path = "http://192.168.112.3/juejiang.mp3";
	String path = "";
	// ����imageMap����
	private HashMap<Integer, Bitmap> imageMap = new HashMap<Integer, Bitmap>();
	// ����music����
	private ArrayList<Music> musicList = new ArrayList<Music>();

	// ��ȡmusic����
	public ArrayList<Music> getMusicList() {
		return musicList;
	}

	// ����music����
	public void setMusicList(ArrayList<Music> musicList) {
		// ���������֮ǰ���б�
		this.musicList.clear();
		// ���õ�����������ȫ����
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
		// MyService����ʱ�ʹ���MediaPlayer����
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

	// �Ƿ�Ϊ������Ž���
	public boolean isRecently() {
		return isRecently;
	}

	public void setRecently(boolean isRecently) {
		this.isRecently = isRecently;
	}

	// �õ�MediaPlayer�ĵ�ǰ����λ��
	public int getCurrentPosition() {
		return mMediaPlayer.getCurrentPosition() / 1000;
	}

	// ��ȡ�����ܳ���
	public int getDuration() {
		return mMediaPlayer.getDuration() / 1000;
	}

	// MediaPlayer���¶�λ����λ��
	public void mySeekTo(int msec) {
		mMediaPlayer.seekTo(msec * 1000);

	}

	public void playMusic(String path) {
		try {
			// �������path����setPath
			this.setPath(path);
			// ��ִ��playMusicʱ����Ϊδ����״̬
			this.setIsLoad(false);
			// һ��ִ��playMusicʱ��isFirst��Ϊfalse
			this.setIsFirst(false);
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(path);
			// �첽����,���prepare
			mMediaPlayer.prepareAsync();
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					// //��onPrepared׼����ʱ��isLoad��Ϊtrue
					MyService.this.setIsLoad(true);
					mMediaPlayer.start();
					// ���������״̬ʱ,��BUG
					// if(!isRecently){
					// �Ѿ����Ź�ʱ,��������ݿ����ظ���������Ϣ
					deleteSQL();
					// ���������ص�������Ϣ���浽���ݿ�musicdata����
					insertSQL();
					// ���͹㲥,���¶�ȡ���ݿ�
					sendBroadcast(new Intent(
							"android.action.broadcast.MyRecentPlay"));
					// }
				}
			});
			//�������������
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

	// ������ɼ���
	private void completeListenr() {
		mMediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						// �ж�ģʽ
						switch (mode) {
						// ѭ��
						case 0:
							index++;
							index = index % musicList.size();
							music = musicList.get(index);
							MyService.this.playMusic(music.musicUrl);
							break;
						// ����
						case 1:
							playMusic(music.musicUrl);
							break;
						// ���
						case 2:
							Random random = new Random();
							while (true) {
								int len = musicList.size();
								int nextInt = random.nextInt(len);
								// �����������ڡ�����2��ʱ
								if (len > 1) {
									if (index != nextInt) {
										index = nextInt;
										// ����ѭ��
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

	// ��������ݿ����ظ���������Ϣ
	private void deleteSQL() {
		mDb.delete("musicdata", "musicUrl=?", new String[] { music.musicUrl });
	}

	// ���������ص����ֱ��浽���ݿ���
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
		// ��onPrepared׼����ʱ���ŵ���pause()
		if (isLoad()) {
			mMediaPlayer.pause();
		}
	}

	public void playStop() {
		mMediaPlayer.stop();
	}

	public void reStartPlay() {
		// ��onPrepared׼����ʱ
		if (isLoad()) {
			mMediaPlayer.start();
		}
	}

	// ������һ��
	public void playNextMusic() {
		index++;
		//��index�����б���ʱ������
		if(index>musicList.size()-1){
			index = 0;
		}
		music = musicList.get(index);
		MyService.this.playMusic(music.musicUrl);
	}

	// ������һ��
	public void playUpMusic() {
		index--;
		//��indexС��0ʱ���ص��б��β
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

	// ����isLoad��ʶλ
	public boolean isLoad() {
		return isLoad;
	}

	// ����isLoad��ʶλ
	public void setIsLoad(boolean isLoad) {
		this.isLoad = isLoad;
	}

	// ��ȡMusic����
	public Music getMusic() {
		return music;
	}

	// �����������صõ�Music����
	public void setMusic(Music music) {
		this.music = music;
	}

	// �����������صõ�����ͼƬ����
	public void setImageMap(HashMap<Integer, Bitmap> imageMap) {
		// ÿ������ǰ�������֮ǰ��ӵ�ͼƬ
		MyService.this.imageMap.clear();
		// ����ֱ����image��ֵ
		this.imageMap.putAll(imageMap);
	}

	// ��ȡ����ͼƬ����
	public HashMap<Integer, Bitmap> getImageMap() {
		return imageMap;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int position) {
		this.index = position;
	}

	// ��ȡ�Ƿ���ϲ����
	public int getIsFavorite() {
		return isFavorite;
	}

	public void setIsFavorite(int isFavorite) {
		this.isFavorite = isFavorite;
	}

	// ��ȡ����ģʽ
	public int getMode() {
		return mode;
	}

	// ���ò���ģʽ
	public void setMode(int mode) {
		this.mode = mode;
	}

}
