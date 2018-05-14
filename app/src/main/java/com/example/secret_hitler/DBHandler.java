package com.example.secret_hitler;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHandler extends SQLiteOpenHelper {

    private static DBHandler sInstance;

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "player.db";
    private static final String TABLE_NAME = "playerDetails";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ROLE = "role";
    SQLiteDatabase db;

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (id integer PRIMARY KEY NOT NULL , "
            + "role text);";

    public static synchronized DBHandler getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        this.db = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addNewPlayer(Player newPlayer) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        int rowCount = cursor.getCount();

        values.put(COLUMN_ID, rowCount);
        values.put(COLUMN_ROLE, "role");

        db.insert(TABLE_NAME, null, values);
        cursor.close();
        db.close();
    }

    public String GetRole(int id) {
        db = this.getReadableDatabase();
        //String query = "SELECT " + COLUMN_ROLE + " FROM " + TABLE_NAME;
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_ROLE},COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        String role = "No role";

        if (cursor != null && cursor.moveToFirst()) {
            //cursor.moveToPosition(id);
            role = cursor.getString(cursor.getColumnIndex(COLUMN_ROLE));
        }
        cursor.close();
        db.close();
        return role;
    }

    public void SetRole(int id, String role) {
        db = this.getWritableDatabase();
        String idAsString = Integer.toString(id);
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, id);
        contentValues.put(COLUMN_ROLE, role);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{idAsString});
        db.close();
    }

    public int GetPlayerCount() {
        db = this.getWritableDatabase();
        long tempCount = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        int count = (int) tempCount;
        db.close();
        return count;
    }

    public void ClearTable(String tableName) {
        db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + tableName);
        db.close();
    }

}
