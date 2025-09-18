package com.example.controldehorarios;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class EditarTurno extends AppCompatActivity {

    private EditText etFechaInicio, etHoraInicio, etFechaFinal, etHoraFinal;
    private EditText editHoras, editMinutos, editNotas;
    private EditText editEspeciales, editPrecioEspecial, editDescansoPersonalizado;
    private Spinner spinnerDescanso;
    private ImageButton btnAceptar;
    private TextView totalPagado;
    private int idTurno;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_turno);

        etFechaInicio = findViewById(R.id.etFechaInicio3);
        etHoraInicio = findViewById(R.id.etHoraInicio3);
        etFechaFinal = findViewById(R.id.etFechaFinal3);
        etHoraFinal = findViewById(R.id.etHoraFinal3);
        editHoras = findViewById(R.id.editHoras3);
        editMinutos = findViewById(R.id.editMinutos3);
        editNotas = findViewById(R.id.editTextTextMultiLine3);
        editEspeciales = findViewById(R.id.editTextText5);
        editPrecioEspecial = findViewById(R.id.editTextText6);
        spinnerDescanso = findViewById(R.id.spinner2);
        editDescansoPersonalizado = findViewById(R.id.editTextNumerico3);
        btnAceptar = findViewById(R.id.btnAceptar3);
        totalPagado = findViewById(R.id.totalPagado3);



        // Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.array_descansos, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDescanso.setAdapter(adapter);

        // Recibir idTurno
        idTurno = getIntent().getIntExtra("idTurno", -1);
        if (idTurno == -1) {
            Toast.makeText(this, "Error al cargar turno", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarDatosTurno(idTurno);

        // Listeners DatePicker y TimePicker
        etFechaInicio.setOnClickListener(v -> mostrarDatePicker(etFechaInicio));
        etFechaFinal.setOnClickListener(v -> mostrarDatePicker(etFechaFinal));
        etHoraInicio.setOnClickListener(v -> mostrarTimePicker(etHoraInicio));
        etHoraFinal.setOnClickListener(v -> mostrarTimePicker(etHoraFinal));

        // Recalcular al cambiar horas, minutos, precio o descanso
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { calcularDuracionYTotal(); }
        };
        editHoras.addTextChangedListener(watcher);
        editMinutos.addTextChangedListener(watcher);
        editEspeciales.addTextChangedListener(watcher);
        editPrecioEspecial.addTextChangedListener(watcher);
        editDescansoPersonalizado.addTextChangedListener(watcher);

        spinnerDescanso.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                editDescansoPersonalizado.setVisibility(position == parent.getCount() - 1 ? View.VISIBLE : View.GONE);
                calcularDuracionYTotal();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnAceptar.setOnClickListener(v -> {
            actualizarTurno();
            Intent intent = new Intent(EditarTurno.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private TextWatcher simpleTextWatcher() {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { calcularDuracionYTotal(); }
        };
    }

    private void cargarDatosTurno(int id) {
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT fechaInicio, horaInicio, fechaFinal, horaFinal, horas, minutos, notas, descanso, descansoPersonalizado FROM turnos WHERE id=?",
                new String[]{String.valueOf(id)}
        );


        if (cursor.moveToFirst()) {
            etFechaInicio.setText(cursor.getString(0));
            etFechaInicio.setTag(cursor.getString(0));
            etHoraInicio.setText(cursor.getString(1));
            etFechaFinal.setText(cursor.getString(2));
            etFechaFinal.setTag(cursor.getString(2));
            etHoraFinal.setText(cursor.getString(3));

            editHoras.setText(cursor.isNull(4) ? "" : String.valueOf(cursor.getInt(4)));
            editMinutos.setText(cursor.isNull(5) ? "" : String.valueOf(cursor.getInt(5)));
            editNotas.setText(cursor.getString(6));

            String descanso = cursor.getString(7);
            if (descanso != null) {
                int pos = ((ArrayAdapter) spinnerDescanso.getAdapter()).getPosition(descanso);
                spinnerDescanso.setSelection(pos >= 0 ? pos : 0);
            }

            editDescansoPersonalizado.setText(cursor.isNull(8) ? "" : String.valueOf(cursor.getInt(8)));

        }

        cursor.close();
        db.close();

        calcularDuracionYTotal();
    }

    private void actualizarTurno() {
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("fechaInicio", etFechaInicio.getTag() != null ? etFechaInicio.getTag().toString() : "");
        cv.put("horaInicio", etHoraInicio.getText().toString());
        cv.put("fechaFinal", etFechaFinal.getTag() != null ? etFechaFinal.getTag().toString() : "");
        cv.put("horaFinal", etHoraFinal.getText().toString());
        cv.put("horas", editHoras.getText().toString().isEmpty() ? 0 : Integer.parseInt(editHoras.getText().toString()));
        cv.put("minutos", editMinutos.getText().toString().isEmpty() ? 0 : Integer.parseInt(editMinutos.getText().toString()));
        cv.put("notas", editNotas.getText().toString());
        cv.put("descanso", spinnerDescanso.getSelectedItem() != null ? spinnerDescanso.getSelectedItem().toString() : "");
        String txtDescanso = editDescansoPersonalizado.getText().toString();
        if (txtDescanso.isEmpty()) cv.putNull("descansoPersonalizado");
        else cv.put("descansoPersonalizado", Integer.parseInt(txtDescanso));

        double total = calcularDuracionYTotal();
        cv.put("totalPagado", total);

        int filas = db.update("turnos", cv, "id=?", new String[]{String.valueOf(idTurno)});
        db.close();

        if (filas > 0) Toast.makeText(this, "Turno actualizado", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
    }

    private void mostrarDatePicker(EditText campo) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR), month = c.get(Calendar.MONTH), day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dp = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    campo.setText(d + "/" + (m + 1) + "/" + y);
                    campo.setTag(String.format("%04d-%02d-%02d", y, m + 1, d));
                    calcularDuracionYTotal();
                }, year, month, day);
        dp.show();
    }

    private void mostrarTimePicker(EditText campo) {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY), m = c.get(Calendar.MINUTE);
        TimePickerDialog tp = new TimePickerDialog(this,
                (view, h1, m1) -> {
                    campo.setText(String.format("%02d:%02d", h1, m1));
                    calcularDuracionYTotal();
                }, h, m, true);
        tp.show();
    }

    private double calcularDuracionYTotal() {
        try {
            String horaInicioStr = etHoraInicio.getText().toString();
            String horaFinalStr = etHoraFinal.getText().toString();
            if (horaInicioStr.isEmpty() || horaFinalStr.isEmpty()) return 0;

            String[] inicio = horaInicioStr.split(":");
            String[] fin = horaFinalStr.split(":");

            int hIni = Integer.parseInt(inicio[0]);
            int mIni = Integer.parseInt(inicio[1]);
            int hFin = Integer.parseInt(fin[0]);
            int mFin = Integer.parseInt(fin[1]);

            int minIni = hIni * 60 + mIni;
            int minFin = hFin * 60 + mFin;
            if (minFin < minIni) minFin += 24 * 60;

            int duracionBruta = minFin - minIni;

            int descansoMin = 0;
            String opcion = spinnerDescanso.getSelectedItem().toString();
            switch (opcion) {
                case "15 min": descansoMin = 15; break;
                case "30 min": descansoMin = 30; break;
                case "45 min": descansoMin = 45; break;
                case "60 min": descansoMin = 60; break;
                case "Sin descanso": default: descansoMin = 0; break;
            }
            if (editDescansoPersonalizado.getVisibility() == View.VISIBLE && !editDescansoPersonalizado.getText().toString().isEmpty())
                descansoMin = Integer.parseInt(editDescansoPersonalizado.getText().toString());

            int duracionTotalMin = Math.max(0, duracionBruta - descansoMin);
            double horasTotalesNormales = duracionTotalMin / 60.0;

            int hEsp = editEspeciales.getText().toString().isEmpty() ? 0 : Integer.parseInt(editEspeciales.getText().toString());
            double precioEsp = editPrecioEspecial.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(editPrecioEspecial.getText().toString());

            SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
            double tarifaNormal = prefs.getFloat("tarifa", 10f);

            double total = (horasTotalesNormales * tarifaNormal) + (hEsp * precioEsp);

            totalPagado.setText(String.format("Total: %.2f h | %.2f € (descanso %.2f h)", horasTotalesNormales, total, descansoMin / 60.0));

            return total;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }



}
