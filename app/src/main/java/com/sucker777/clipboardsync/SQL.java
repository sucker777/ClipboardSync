package com.sucker777.clipboardsync;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class SQL extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    public SQL(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public SQL(Context context, String name) {
        this(context, name, null, VERSION);
    }

    public SQL(Context context, String name, int version) {
        this(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE_TABLE =
                "create table history("
                        + "`timestamp` timestamp NOT NULL,"
                        + "`data` longtext NOT NULL"
                        + ")";
        db.execSQL(DATABASE_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS newMemorandum");
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // TODO 每次成功打開數據庫後首先被執行
    }

    @Override
    public synchronized void close() {
        super.close();
    }
}
