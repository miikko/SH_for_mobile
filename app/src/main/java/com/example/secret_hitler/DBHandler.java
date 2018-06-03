package com.example.secret_hitler;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collection;

public class DBHandler extends SQLiteOpenHelper {

    private static DBHandler sInstance;

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "player.db";

    private static final String PLAYER_TABLE_NAME = "playerDetails";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_IS_PRESIDENT = "isPresident";
    private static final String COLUMN_IS_CHANCELLOR = "isChancellor";
    private static final String COLUMN_IS_ALIVE = "isAlive";

    private static final String BOARD_TABLE_NAME = "gameBoard";
    //private static final String COLUMN_ID = "id";
    private static final String COLUMN_FASCIST_LAW_COUNT = "fascistLawCount";
    private static final String COLUMN_LIBERAL_LAW_COUNT = "liberalLawCount";
    SQLiteDatabase db;

    private static final String PLAYER_TABLE_CREATE = "CREATE TABLE " + PLAYER_TABLE_NAME +
            " (id integer PRIMARY KEY NOT NULL , role text , name text , isPresident boolean , isChancellor boolean , isAlive boolean);";

    private static final String BOARD_TABLE_CREATE = "CREATE TABLE " + BOARD_TABLE_NAME + " (id integer PRIMARY KEY NOT NULL , fascistLawCount integer, liberalLawCount integer);";

    /**
     * General Database Methods
     */
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
        db.execSQL(PLAYER_TABLE_CREATE);
        db.execSQL(BOARD_TABLE_CREATE);
        this.db = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PLAYER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BOARD_TABLE_NAME);
        onCreate(db);
    }

    public void ClearTable(String tableName) {
        db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + tableName);
        db.close();
    }

    public void TableCount() {
        db = this.getReadableDatabase();
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='gameBoard'";
        Cursor cursor = db.rawQuery(query, null);
        Log.d("TABLE", "" + cursor.getCount());
        db.close();
        close();
    }

    public int RowCount(String tableName) {
        db = this.getWritableDatabase();
        long tempCount = DatabaseUtils.queryNumEntries(db, tableName);
        int count = (int) tempCount;
        db.close();
        return count;
    }

    private String GetStringItem(String tableName, String columnName, int rowNumber) {
        db = this.getReadableDatabase();
        Cursor cursor = db.query(tableName, new String[]{COLUMN_ID, columnName},COLUMN_ID + "=?", new String[]{String.valueOf(rowNumber)}, null, null, null, null);
        String item = "";

        if (cursor != null && cursor.moveToFirst()) {
            item = cursor.getString(cursor.getColumnIndex(columnName));
        }
        cursor.close();
        db.close();
        return item;
    }

    private int GetIntItem(String tableName, String columnName, int rowNumber) {
        db = this.getReadableDatabase();
        Cursor cursor = db.query(tableName, new String[]{COLUMN_ID, columnName},COLUMN_ID + "=?", new String[]{String.valueOf(rowNumber)}, null, null, null, null);
        int item = 0;

        if (cursor != null && cursor.moveToFirst()) {
            item = cursor.getInt(cursor.getColumnIndex(columnName));
        }
        cursor.close();
        db.close();
        return item;
    }
    /**
     * End of General Database Methods
     */
//------------------------------------------------------------------------------------------------//
    /**
     * Player_Table methods
     */
    public void addNewPlayer(Player newPlayer) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "SELECT * FROM " + PLAYER_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        int rowCount = cursor.getCount();

        values.put(COLUMN_ID, rowCount);
        values.put(COLUMN_ROLE, newPlayer.role);
        values.put(COLUMN_NAME, newPlayer.name);
        values.put(COLUMN_IS_PRESIDENT, newPlayer.isPresident);
        values.put(COLUMN_IS_CHANCELLOR, newPlayer.isChancellor);
        values.put(COLUMN_IS_ALIVE, newPlayer.isAlive);

        db.insert(PLAYER_TABLE_NAME, null, values);
        cursor.close();
        db.close();
    }

    public String GetName(int id) {
        String name = GetStringItem(PLAYER_TABLE_NAME, COLUMN_NAME, id);
        return name;
    }

    public String GetRole(int id) {
        String role = GetStringItem(PLAYER_TABLE_NAME, COLUMN_ROLE, id);
        return role;
    }

    public void SetRole(int id, String role) {
        db = this.getWritableDatabase();
        String idAsString = Integer.toString(id);
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, id);
        contentValues.put(COLUMN_ROLE, role);
        db.update(PLAYER_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{idAsString});
        db.close();
    }

    public int GetPlayerCount() {
        int count = RowCount(PLAYER_TABLE_NAME);
        return count;
    }

    public int GetPresidentID() {
        int presidentID = 0;
        db = this.getReadableDatabase();
        Cursor cursor = db.query(PLAYER_TABLE_NAME, new String[]{COLUMN_IS_PRESIDENT}, null, null, null, null, null);
        ArrayList<Integer> presidencyStatuses = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                presidencyStatuses.add(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PRESIDENT)));
            } while (cursor != null && cursor.moveToNext());
        }
        cursor.close();
        db.close();

        if (!presidencyStatuses.isEmpty()) {
            for (int i = 0; i < presidencyStatuses.size(); i++) {
                if (presidencyStatuses.get(i) == 1) {
                    presidentID = i;
                    break;
                }
            }
        }
        return presidentID;
    }

    public String GetPresidentName() {
        String presidentName = "";
        db = this.getReadableDatabase();
        Cursor cursor = db.query(PLAYER_TABLE_NAME, new String[]{COLUMN_NAME, COLUMN_IS_PRESIDENT}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int presidencyStatus = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PRESIDENT));
                if (presidencyStatus == 1) {
                    presidentName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    cursor.close();
                    db.close();
                    return presidentName;
                }
            } while (cursor != null && cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return presidentName;
    }

    public void SetAsPresident(int playerID) {
        db = this.getWritableDatabase();
        String idAsString = Integer.toString(playerID);
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_IS_PRESIDENT, true);
        db.update(PLAYER_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{idAsString});
        db.close();
    }

    public boolean PresidentExists() {
        db = this.getReadableDatabase();
        Cursor cursor = db.query(PLAYER_TABLE_NAME, new String[]{COLUMN_IS_PRESIDENT}, null, null, null, null, null);
        int presidencyStatus;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                presidencyStatus = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PRESIDENT));
                if (presidencyStatus == 1) {
                    cursor.close();
                    db.close();
                    return true;
                }
            } while (cursor != null && cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return false;
    }
    /**
     * End of Player_Table Methods
     */
//------------------------------------------------------------------------------------------------//
    /**
     * Board_Table Methods
     */
    public void InitializeBoard() {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "SELECT * FROM " + BOARD_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        int rowCount = cursor.getCount();

        values.put(COLUMN_ID, rowCount);
        values.put(COLUMN_FASCIST_LAW_COUNT, "0");
        values.put(COLUMN_LIBERAL_LAW_COUNT, "0");

        db.insert(BOARD_TABLE_NAME, null, values);
        cursor.close();
        db.close();
    }

    public void AddLaw(String lawType) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, "0");
        int fascistLawCount = GetLawCount("Fascist");
        int liberalLawCount = GetLawCount("Liberal");
        if (lawType.equals("Fascist")) {
            fascistLawCount++;
        } else {
            liberalLawCount++;
        }
        contentValues.put(COLUMN_FASCIST_LAW_COUNT, Integer.toString(fascistLawCount));
        contentValues.put(COLUMN_LIBERAL_LAW_COUNT, Integer.toString(liberalLawCount));
        db = this.getWritableDatabase();
        db.update(BOARD_TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{"0"});
        db.close();
    }

    public int GetLawCount(String lawType) {
        String columnName = COLUMN_LIBERAL_LAW_COUNT;
        if (lawType.equals("Fascist")) {
            columnName = COLUMN_FASCIST_LAW_COUNT;
        }
        int lawCount = GetIntItem(BOARD_TABLE_NAME, columnName, 0);
        return lawCount;
    }
    /**
     * End of Board_Table Methods
     */
}
