package com.example.joggingclubmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SessionDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "jogging_club.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_SESSIONS = "sessions";

    public SessionDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SESSIONS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "session_date TEXT NOT NULL, "
                + "session_time TEXT NOT NULL, "
                + "location TEXT NOT NULL, "
                + "capacity INTEGER NOT NULL, "
                + "registrations INTEGER NOT NULL DEFAULT 0, "
                + "status TEXT NOT NULL DEFAULT 'Open'"
                + ")";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Keep existing session data when upgrading.
    }

    public boolean insertSession(
            String name,
            String date,
            String time,
            String location,
            int capacity) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("session_date", date);
        values.put("session_time", time);
        values.put("location", location);
        values.put("capacity", capacity);
        values.put("registrations", 0);
        values.put("status", "Open");

        long result = db.insert(TABLE_SESSIONS, null, values);

        return result != -1;
    }

    public Cursor getAllSessions() {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_SESSIONS,
                null,
                null,
                null,
                null,
                null,
                "id DESC"
        );
    }

    public boolean updateSessionStatus(int sessionId, String status) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("status", status);

        int rowsUpdated = db.update(
                TABLE_SESSIONS,
                values,
                "id = ?",
                new String[]{String.valueOf(sessionId)}
        );

        return rowsUpdated > 0;
    }
}
