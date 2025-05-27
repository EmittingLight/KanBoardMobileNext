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
    private static final int DATABASE_VERSION = 2; // 🆙 увеличили версию

    public static final String TABLE_TICKETS = "tickets";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_DUE_DATE = "due_date"; // ⏰ новое поле

    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_TICKETS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_STATUS + " TEXT NOT NULL, " +
            COLUMN_CREATED_AT + " TEXT, " +
            COLUMN_DUE_DATE + " TEXT);";

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

    public void insertTicket(String title, String description, String status, String createdAt, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_CREATED_AT, createdAt);
        values.put(COLUMN_DUE_DATE, dueDate);

        long result = db.insert(TABLE_TICKETS, null, values);
        Log.d(TAG, "Добавлена задача: " + title + " | Время: " + createdAt + " | Срок: " + dueDate + " | Результат: " + result);
        db.close();
    }

    public List<Ticket> getAllTickets() {
        List<Ticket> overdue = new ArrayList<>();
        List<Ticket> rest = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TICKETS, null);

        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE));

                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                Ticket ticket = new Ticket(id, title, description, status, createdAt, dueDate);


                // 📅 Проверка на просрочку
                if (dueDate != null && !dueDate.isEmpty() && !"Готово".equals(status)) {
                    try {
                        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
                        java.util.Date due = format.parse(dueDate);
                        java.util.Date now = new java.util.Date();

                        if (due != null && due.before(now)) {
                            overdue.add(ticket);
                            continue;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка парсинга dueDate: " + dueDate, e);
                    }
                }

                rest.add(ticket);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // ⬆️ Сначала просроченные, потом остальные
        List<Ticket> sorted = new ArrayList<>();
        sorted.addAll(overdue);
        sorted.addAll(rest);

        Log.d(TAG, "Загружено задач: " + sorted.size());
        return sorted;
    }


    public void updateTicket(Ticket ticket) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, ticket.getTitle());
        values.put(COLUMN_DESCRIPTION, ticket.getDescription());
        values.put(COLUMN_STATUS, ticket.getStatus());
        values.put(COLUMN_CREATED_AT, ticket.getCreatedAt());
        values.put(COLUMN_DUE_DATE, ticket.getDueDate());

        int updated = db.update(
                TABLE_TICKETS,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(ticket.getId())}
        );

        Log.d(TAG, "Обновлено записей: " + updated);
        db.close();
    }


    public void deleteTicket(Ticket ticket) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TICKETS, COLUMN_ID + " = ?", new String[]{String.valueOf(ticket.getId())});
        db.close();
    }


    public List<Ticket> getAllTicketsRaw() {
        List<Ticket> tickets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TICKETS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE));

                Ticket ticket = new Ticket(id, title, description, status, createdAt, dueDate);
                tickets.add(ticket);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tickets;
    }
    public void updateOverdueStatuses() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TICKETS + " WHERE " + COLUMN_STATUS + " != 'Готово'", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String dueDateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));

                try {
                    if (dueDateStr != null && !dueDateStr.isEmpty()) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
                        java.util.Date dueDate = sdf.parse(dueDateStr);
                        java.util.Date now = new java.util.Date();

                        if (dueDate != null && dueDate.before(now)) {
                            ContentValues values = new ContentValues();
                            values.put(COLUMN_STATUS, "К выполнению"); // или "Просрочено", если хочешь новый статус
                            db.update(TABLE_TICKETS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

}
