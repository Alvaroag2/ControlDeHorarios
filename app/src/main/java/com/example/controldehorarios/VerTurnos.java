package com.example.controldehorarios;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class VerTurnos extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TurnoAdapter adapter;
    private ArrayList<Turno> listaTurnos;
    private RadioGroup rgPeriodos;
    private RadioButton rbSemana, rbMes, rbAnio, rbTodo;
    private int selectedYear, selectedMonth, selectedDay;
    private TextView tvTotal, tvTotalConIRPF, tvTotalHoras;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ver_turnos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fecha inicial
        Calendar c = Calendar.getInstance();
        selectedYear = c.get(Calendar.YEAR);
        selectedMonth = c.get(Calendar.MONTH);
        selectedDay = c.get(Calendar.DAY_OF_MONTH);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerViewTurnos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaTurnos = new ArrayList<>();

        //Preguntar sobre la accion
        adapter = new TurnoAdapter(listaTurnos, turno -> {
            new AlertDialog.Builder(VerTurnos.this)
                    .setTitle("Acción sobre el turno")
                    .setMessage("¿Qué quieres hacer con este turno?")
                    .setPositiveButton("Editar", (dialog, which) -> {
                        // Abrir la actividad de edición y pasar los datos del turno
                        Intent intent = new Intent(VerTurnos.this, EditarTurno.class);
                        intent.putExtra("idTurno", turno.getId());
                        Log.d("VerTurnos", "Enviando idTurno: " + turno.getId());
                        startActivity(intent);
                    })
                    .setNegativeButton("Eliminar", (dialog, which) -> {
                        // Confirmar eliminación
                        new AlertDialog.Builder(VerTurnos.this)
                                .setTitle("Eliminar turno")
                                .setMessage("¿Seguro que quieres eliminar este turno?")
                                .setPositiveButton("Sí", (d, w) -> eliminarTurno(turno))
                                .setNegativeButton("No", null)
                                .show();
                    })
                    .show();
        });


        recyclerView.setAdapter(adapter);

        // RadioGroup
        rgPeriodos = findViewById(R.id.rgPeriodos);
        rbSemana = findViewById(R.id.rbSemana);
        rbMes = findViewById(R.id.rbMes);
        rbAnio = findViewById(R.id.rbAnio);
        rbTodo = findViewById(R.id.rbTodo);

        // Botón de fecha
        Button btnElegirFecha = findViewById(R.id.btnElegirFecha);
        btnElegirFecha.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    VerTurnos.this,
                    (view, year, month, dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = dayOfMonth;

                        // Mostrar un mensaje con el año seleccionado
                        if (rbAnio.isChecked()) {
                            Toast.makeText(VerTurnos.this,
                                    "Has seleccionado el año: " + selectedYear,
                                    Toast.LENGTH_SHORT).show();
                        }

                        filtrarTurnos();
                    },
                    selectedYear, selectedMonth, selectedDay
            );
            datePickerDialog.show();
        });
        // Total ganado
        tvTotal = findViewById(R.id.tvTotal);
        tvTotalConIRPF = findViewById(R.id.tvTotalConIRPF);
        tvTotalHoras = findViewById(R.id.tvTotalHoras);


        // Listener del RadioGroup
        rgPeriodos.setOnCheckedChangeListener((group, checkedId) -> filtrarTurnos());

        // Carga inicial
        filtrarTurnos();
    }


    private double getIRPF() {
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        return prefs.getFloat("irpf", 15f) / 100.0; // de % a decimal
    }

    private void eliminarTurno(Turno turno) {
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete("turnos", "id = ?", new String[]{String.valueOf(turno.getId())});

        db.close();

        Toast.makeText(this, "Turno eliminado", Toast.LENGTH_SHORT).show();

        filtrarTurnos(); // refresca la lista
    }



    private void filtrarTurnos() {
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        String filtro = "";
        String fechaStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);

        if (rbSemana.isChecked()) {
            filtro = "WHERE date(fechaInicio) BETWEEN date('" + fechaStr + "', '-6 days') AND date('" + fechaStr + "')";
        } else if (rbMes.isChecked()) {
            String mesStr = String.format("%02d", selectedMonth + 1);
            filtro = "WHERE substr(fechaInicio,1,7) = '" + selectedYear + "-" + mesStr + "'";
        } else if (rbAnio.isChecked()) {
            filtro = "WHERE substr(fechaInicio,1,4) = '" + selectedYear + "'";
        }
        // si rbTodo está marcado no añadimos filtro

        // Cargar los turnos
        String sql = "SELECT id, fechaInicio, fechaFinal, horas, minutos, notas, totalPagado FROM turnos " + filtro;
        Cursor cursor = db.rawQuery(sql, null);

        listaTurnos.clear();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String fechaInicio = cursor.getString(1);
                String fechaFinal = cursor.getString(2);
                int horas = cursor.getInt(3);
                int minutos = cursor.getInt(4);
                String notas = cursor.getString(5);
                double totalPagado = cursor.getDouble(6);

                listaTurnos.add(new Turno(id, fechaInicio, fechaFinal, horas, minutos, notas, totalPagado));
            } while (cursor.moveToNext());
        }

        cursor.close();

        // Calcular total de euros en ese periodo
        String sqlTotal = "SELECT SUM(totalPagado) FROM turnos " + filtro;
        Cursor cursorTotal = db.rawQuery(sqlTotal, null);
        double total = 0;
        if (cursorTotal.moveToFirst()) {
            total = cursorTotal.getDouble(0);
        }
        cursorTotal.close();


// Calcular total con IRPF aplicado
        double irpf = getIRPF();
        double totalConIRPF = total - (total * irpf);

        // Calcular total de horas y minutos trabajados en ese periodo
        String sqlHoras = "SELECT SUM(horas), SUM(minutos) FROM turnos " + filtro;
        Cursor cursorHoras = db.rawQuery(sqlHoras, null);

        int totalHoras = 0;
        int totalMinutos = 0;

        if (cursorHoras.moveToFirst()) {
            totalHoras = cursorHoras.getInt(0);
            totalMinutos = cursorHoras.getInt(1);
        }
        cursorHoras.close();


// Convertir los minutos acumulados en horas
        totalHoras += totalMinutos / 60;
        totalMinutos = totalMinutos % 60;

// Mostrar totales en los TextView
        tvTotal.setText("Total bruto: " + String.format("%.2f €", total));
        tvTotalConIRPF.setText("Total con IRPF: " + String.format("%.2f €", totalConIRPF));
        tvTotalHoras.setText("Total horas: " + totalHoras + "h " + totalMinutos + "m");


        db.close();
        adapter.notifyDataSetChanged();
    }





}
