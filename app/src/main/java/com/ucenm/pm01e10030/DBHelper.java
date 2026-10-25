package com.ucenm.pm01e10030;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "contactos.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE contactos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "pais TEXT NOT NULL," +
                "nombre TEXT NOT NULL," +
                "telefono TEXT NOT NULL," +
                "nota TEXT NOT NULL," +
                "imagen TEXT)";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS contactos");
        onCreate(db);
    }

    public long insertar(Contactos c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put("pais", c.getPais());
        valores.put("nombre", c.getNombre());
        valores.put("telefono", c.getTelefono());
        valores.put("nota", c.getNota());
        valores.put("imagen", c.getImagen());

        return db.insert("contactos", null, valores);
    }

    public ArrayList<Contactos> listar() {
        ArrayList<Contactos> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM contactos ORDER BY nombre ASC",
                null
        );

        try {
            while (cursor.moveToNext()) {
                lista.add(new Contactos(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("pais")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nota")),
                        cursor.getString(cursor.getColumnIndexOrThrow("imagen"))
                ));
            }
        } finally {
            cursor.close();
        }

        return lista;
    }

    public Contactos buscarPorId(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "contactos",
                null,
                "id=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        try {
            if (cursor.moveToFirst()) {
                return new Contactos(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("pais")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nota")),
                        cursor.getString(cursor.getColumnIndexOrThrow("imagen"))
                );
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    public int actualizar(Contactos c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put("pais", c.getPais());
        valores.put("nombre", c.getNombre());
        valores.put("telefono", c.getTelefono());
        valores.put("nota", c.getNota());
        valores.put("imagen", c.getImagen());

        return db.update(
                "contactos",
                valores,
                "id=?",
                new String[]{String.valueOf(c.getId())}
        );
    }

    public int eliminar(int id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                "contactos",
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }
}