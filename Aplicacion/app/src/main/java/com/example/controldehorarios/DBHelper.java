package com.example.controldehorarios;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "turnos.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE turnos (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "fechaInicio TEXT," +
                        "horaInicio TEXT," +
                        "fechaFinal TEXT," +
                        "horaFinal TEXT," +
                        "descanso TEXT," +
                        "descansoPersonalizado INTEGER," +
                        "horas INTEGER," +
                        "minutos INTEGER," +
                        "totalPagado REAL," +
                        "notas TEXT," +
                        "horasEspeciales INTEGER," +
                        "precioEspecial REAL" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS turnos");
        onCreate(db);
    }

}
