package com.example.musicplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySqlite extends SQLiteOpenHelper {

	public MySqlite(Context context) {
		super(context, "myMusic.db", null,1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql="create table musicdata (id integer primary key autoincrement,musicName varchar(200),singer varchar(200),imageUrl varchar(200),musicUrl varchar(200),isFavorite integer )";
		db.execSQL(sql);
//		String sql1 = "create table ilike(id integer primary key autoincrement,musicUrl varchar(200),isFavorite integer)";
//		db.execSQL(sql1);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
