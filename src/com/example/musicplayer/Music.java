package com.example.musicplayer;


public class Music  {
	String musicName;
	String singer;
	String imageUrl;
	String musicUrl;
	int isFavorite;

	
	public Music(String musicName, String singer, String imageUrl,
			String musicUrl, int isFavorite) {
		this.imageUrl = imageUrl;
		this.musicName = musicName;
		this.singer = singer;
		this.musicUrl = musicUrl;
		this.isFavorite = isFavorite;
	
	}
	}
