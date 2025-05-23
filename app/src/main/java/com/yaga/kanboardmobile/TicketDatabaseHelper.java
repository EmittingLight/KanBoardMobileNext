package com.yaga.kanboardmobile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TicketDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "TicketDB";
    private static final String DATABASE_NAME = "tickets.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TICKETS = "tickets";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_STATUS = "status";

    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_TICKETS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_STATUS + " TEXT NOT NULL);";

    public TicketDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "Конструктор вызван — создаётся или открывается база.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate вызван: создаётся таблица!");
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade вызван: обновление базы!");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TICKETS);
        onCreate(db);
    }

    public void insertTicket(String title, String description, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_STATUS, status);
        long result = db.insert(TABLE_TICKETS, null, values);
        Log.d(TAG, "Добавлена задача: " + title + " | Результат вставки: " + result);
        db.close();
    }

    public List<Ticket> getAllTickets() {
        List<Ticket> ticketList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TICKETS, null);

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                Ticket ticket = new Ticket(title, description, status);
                ticketList.add(ticket);
            } while (cursor.moveToNext());
        }

        Log.d(TAG, "Загружено задач: " + ticketList.size());

        cursor.close();
        db.close();

        return ticketList;
    }
}
