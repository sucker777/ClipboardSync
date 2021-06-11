package com.sucker777.clipboardsync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLQuery {

    private final static String TABLE_NAME = "history";

    private SQLiteDatabase db;

    public SQLQuery(Context context) {
        db = new SQL(context, "database").getWritableDatabase();
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

    public int getHistoryCount() {
        return 0;
    }
}
