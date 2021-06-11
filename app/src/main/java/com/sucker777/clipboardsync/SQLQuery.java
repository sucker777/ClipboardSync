package com.sucker777.clipboardsync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLQuery {

    private final static String TABLE_NAME = "history";

    private SQL sql;
    private SQLiteDatabase db;

    public SQLQuery(Context context) {
        sql = new SQL(context, "database");
        db = sql.getWritableDatabase();
    }

    public void insert(ContentValues values) {
        //Delete old same content
        String selection = "data = ?";
        String[] selectionArgs = { values.getAsString("data") };
        db.delete(TABLE_NAME, selection, selectionArgs);
        //Insert content
        db.insert(TABLE_NAME, null, values);
    }

    public Cursor getHistory() {
        String[] projection = {
                "timestamp",
                "data"
        };

        String sortOrder = "timestamp" + " DESC";

        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder,
                null
        );

        return cursor;
    }

    public void deleteDB() {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        sql.onCreate(db);
    }

    public int getHistoryCount() {
        return 0;
    }
}
