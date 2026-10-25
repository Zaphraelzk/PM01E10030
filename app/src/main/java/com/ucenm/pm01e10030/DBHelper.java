
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
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pais TEXT NOT NULL, " +
                "nombre TEXT NOT NULL, " +
                "telefono TEXT NOT NULL, " +
                "nota TEXT NOT NULL, " +
                "imagen TEXT" +
                ")";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        // Conserva los datos existentes al actualizar la versión.
        // Si posteriormente cambia la estructura, añade aquí
        // las instrucciones de migración correspondientes.
    }

    // INSERTAR CONTACTO
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

    // LISTAR TODOS LOS CONTACTOS
    public ArrayList<Contactos> listar() {
        ArrayList<Contactos> lista = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM contactos ORDER BY nombre ASC",
                null
        );

        try {
            while (cursor.moveToNext()) {
                Contactos contacto = new Contactos(
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("pais")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("telefono")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("nota")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("imagen"))
                );

                lista.add(contacto);
            }
        } finally {
            cursor.close();
        }

        return lista;
    }

    // BUSCAR CONTACTO POR ID
    public Contactos buscarPorId(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "contactos",
                null,
                "id=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return new Contactos(
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("pais")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("telefono")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("nota")),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("imagen"))
                );
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    // ACTUALIZAR CONTACTO
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

    // ELIMINAR CONTACTO
    public int eliminar(int id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                "contactos",
                "id=?",
                new String[]{String.valueOf(id)}
        );
    }

    // MÉTODOS COMPATIBLES CON LAS ACTIVIDADES JAVA

    public boolean insertarContacto(Contactos c) {
        return insertar(c) != -1;
    }

    public ArrayList<Contactos> obtenerContactos() {
        return listar();
    }

    public Contactos obtenerContactoPorId(int id) {
        return buscarPorId(id);
    }

    public boolean actualizarContacto(Contactos c) {
        return actualizar(c) > 0;
    }

    public boolean eliminarContacto(int id) {
        return eliminar(id) > 0;
    }
}
