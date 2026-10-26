package com.example.gestioncontactos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class ContactDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "contacts.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "contacts";

    public ContactDbHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "country TEXT NOT NULL, name TEXT NOT NULL, phone TEXT NOT NULL, " +
                "note TEXT, image BLOB)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si se cambia el esquema, agrega aquí la migración correspondiente.
    }

    public long insert(Contact c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = values(c);
        return db.insert(TABLE, null, v);
    }

    public int update(Contact c) {
        return getWritableDatabase().update(TABLE, values(c), "id=?", new String[]{String.valueOf(c.id)});
    }

    public int delete(long id) {
        return getWritableDatabase().delete(TABLE, "id=?", new String[]{String.valueOf(id)});
    }

    public Contact get(long id) {
        try (Cursor cur = getReadableDatabase().query(TABLE, null, "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            return cur.moveToFirst() ? fromCursor(cur) : null;
        }
    }

    public List<Contact> all(String query) {
        List<Contact> result = new ArrayList<>();
        String q = query == null ? "" : query.trim();
        try (Cursor cur = getReadableDatabase().query(TABLE, null,
                "name LIKE ? OR phone LIKE ?", new String[]{"%" + q + "%", "%" + q + "%"},
                null, null, "name COLLATE NOCASE ASC")) {
            while (cur.moveToNext()) result.add(fromCursor(cur));
        }
        return result;
    }

    private ContentValues values(Contact c) {
        ContentValues v = new ContentValues();
        v.put("country", c.country);
        v.put("name", c.name);
        v.put("phone", c.phone);
        v.put("note", c.note);
        if (c.image == null) v.putNull("image"); else v.put("image", c.image);
        return v;
    }

    private Contact fromCursor(Cursor c) {
        int imageIndex = c.getColumnIndexOrThrow("image");
        return new Contact(c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("country")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("phone")),
                c.getString(c.getColumnIndexOrThrow("note")),
                c.isNull(imageIndex) ? null : c.getBlob(imageIndex));
    }
}
