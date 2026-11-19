package com.example.joggingclubmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MemberDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "jogging_members.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_MEMBERS = "members";

    public MemberDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MEMBERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "full_name TEXT NOT NULL, "
                + "email TEXT NOT NULL, "
                + "phone TEXT NOT NULL, "
                + "emergency_name TEXT NOT NULL, "
                + "emergency_phone TEXT NOT NULL, "
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Database upgrades can be added here later.
    }

    public boolean insertMember(
            String fullName,
            String email,
            String phone,
            String emergencyName,
            String emergencyPhone) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("email", email);
        values.put("phone", phone);
        values.put("emergency_name", emergencyName);
        values.put("emergency_phone", emergencyPhone);

        long result = db.insert(TABLE_MEMBERS, null, values);

        return result != -1;
    }

    public Cursor getAllMembers() {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_MEMBERS,
                null,
                null,
                null,
                null,
                null,
                "id DESC"
        );
    }
}
