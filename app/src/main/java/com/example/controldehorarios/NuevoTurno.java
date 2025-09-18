package com.example.controldehorarios;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class NuevoTurno extends AppCompatActivity {

    private EditText etFechaInicio, etHoraInicio, etFechaFinal, etHoraFinal;
    private Spinner spinner;
    private EditText editTextNumerico;
    private EditText editHoras, editMinutos;
    private TextView totalPagado;
    private boolean editandoManual = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nuevo_turno);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        etFechaInicio = findViewById(R.id.etFechaInicio);
        etHoraInicio = findViewById(R.id.etHoraInicio);
        etFechaFinal = findViewById(R.id.etFechaFinal);
        etHoraFinal = findViewById(R.id.etHoraFinal);

        // Listeners
        etFechaInicio.setOnClickListener(v -> mostrarDatePicker(etFechaInicio));
        etHoraInicio.setOnClickListener(v -> mostrarTimePicker(etHoraInicio));
        etFechaFinal.setOnClickListener(v -> mostrarDatePicker(etFechaFinal));
        etHoraFinal.setOnClickListener(v -> mostrarTimePicker(etHoraFinal));


        editTextNumerico = findViewById(R.id.editTextNumerico);
        editHoras = findViewById(R.id.editHoras);
        editMinutos = findViewById(R.id.editMinutos);
        totalPagado = findViewById(R.id.totalPagado);

        spinner = findViewById(R.id.spinner1);


        ArrayAdapter<CharSequence> adaptador = ArrayAdapter.createFromResource(this, R.array.array_descansos, android.R.layout.simple_spinner_item);

        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adaptador);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i == adapterView.getCount() - 1) {
                    // Última opción seleccionada
                    editTextNumerico.setVisibility(View.VISIBLE);
                } else {
                    // Cualquier otra opción
                    editTextNumerico.setVisibility(View.GONE);
                }
                calcularDuracionYTotal(); // recalcular siempre que cambie el descanso

                Toast.makeText(adapterView.getContext(), (String) adapterView.getItemAtPosition(i), Toast.LENGTH_SHORT).show();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        // Si escribes un descanso personalizado recalcular
        editTextNumerico.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                calcularDuracionYTotal();
            }
        });


        ImageButton btnAceptar = findViewById(R.id.btnAceptar);

        btnAceptar.setOnClickListener(v -> {
            DBHelper helper = new DBHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();

            // Datos horas normales
            int horasNormales = editHoras.getText().toString().isEmpty() ? 0 : Integer.parseInt(editHoras.getText().toString());
            int minutosNormales = editMinutos.getText().toString().isEmpty() ? 0 : Integer.parseInt(editMinutos.getText().toString());
            double horasTotalesNormales = horasNormales + (minutosNormales / 60.0);

            // Datos horas especiales
            int horasEspeciales = ((EditText) findViewById(R.id.editTextText)).getText().toString().isEmpty()
                    ? 0
                    : Integer.parseInt(((EditText) findViewById(R.id.editTextText)).getText().toString());
            double precioEspecial = ((EditText) findViewById(R.id.editTextText2)).getText().toString().isEmpty()
                    ? 0.0
                    : Double.parseDouble(((EditText) findViewById(R.id.editTextText2)).getText().toString());


            //Tarifa normal
            SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
            double tarifaNormal = prefs.getFloat("tarifa", 10f); // si no hay nada, 10 por defecto

            // Calculo de total pagado
            double totalPagadoValor = (horasTotalesNormales * tarifaNormal) + (horasEspeciales * precioEspecial);

            String sql = "INSERT INTO turnos (fechaInicio, horaInicio, fechaFinal, horaFinal, descanso, descansoPersonalizado, horas, minutos, totalPagado, notas, horasEspeciales, precioEspecial) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            db.execSQL(sql, new Object[]{
                    etFechaInicio.getTag().toString(),
                    etHoraInicio.getText().toString(),
                    etFechaFinal.getTag().toString(),
                    etHoraFinal.getText().toString(),
                    spinner.getSelectedItem().toString(),
                    editTextNumerico.getVisibility() == View.VISIBLE && !editTextNumerico.getText().toString().isEmpty()
                            ? Integer.parseInt(editTextNumerico.getText().toString())
                            : null,
                    horasNormales,
                    minutosNormales,
                    totalPagadoValor,
                    ((EditText) findViewById(R.id.editTextTextMultiLine)).getText().toString(),
                    horasEspeciales,
                    precioEspecial
            });

            db.close();
            Toast.makeText(this, "Turno guardado", Toast.LENGTH_SHORT).show();
            finish();
        });


        TextWatcher actualizador = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                calcularTotalHoras();
            }
        };

        editHoras.addTextChangedListener(actualizador);
        editMinutos.addTextChangedListener(actualizador);

    }

    private void calcularTotalHoras() {
        String horasStr = editHoras.getText().toString();
        String minutosStr = editMinutos.getText().toString();

        int horas = horasStr.isEmpty() ? 0 : Integer.parseInt(horasStr);
        int minutos = minutosStr.isEmpty() ? 0 : Integer.parseInt(minutosStr);

        double total = horas + (minutos / 60.0);
        totalPagado.setText(String.format("Total pagado: %.2f horas", total));
    }




    private void mostrarDatePicker(EditText campo) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    // Formato DD/MM/YYYY para mostrar
                    String fechaMostrar = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    campo.setText(fechaMostrar);

                    // Guardar también en formato YYYY-MM-DD
                    campo.setTag(String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth));
                }, year, month, day);
        datePicker.show();
    }


    private void mostrarTimePicker(EditText campo) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    String hora = String.format("%02d:%02d", hourOfDay, minute1);
                    campo.setText(hora);

                    // Calcular cuando ya tengamos inicio y fin
                    calcularDuracionYTotal();
                }, hour, minute, true);
        timePicker.show();
    }



    private void calcularDuracionYTotal() {
        try {
            String horaInicioStr = etHoraInicio.getText().toString();
            String horaFinalStr = etHoraFinal.getText().toString();

            if (horaInicioStr.isEmpty() || horaFinalStr.isEmpty()) return;


            String[] inicioParts = horaInicioStr.split(":");
            String[] finParts = horaFinalStr.split(":");

            int hInicio = Integer.parseInt(inicioParts[0]);
            int mInicio = Integer.parseInt(inicioParts[1]);
            int hFin = Integer.parseInt(finParts[0]);
            int mFin = Integer.parseInt(finParts[1]);


            int minutosInicio = hInicio * 60 + mInicio;
            int minutosFin = hFin * 60 + mFin;


            if (minutosFin < minutosInicio) {
                minutosFin += 24 * 60;
            }

            int duracionBrutaMin = minutosFin - minutosInicio; // antes del descanso
            int duracionTotalMin = duracionBrutaMin;

            // Restar descanso
            int descansoMin = 0;
            String opcion = spinner.getSelectedItem().toString();

            switch (opcion) {
                case "15 min":
                    descansoMin = 15;
                    break;
                case "30 min":
                    descansoMin = 30;
                    break;
                case "45 min":
                    descansoMin = 45;
                    break;
                case "60 min":
                    descansoMin = 60;
                    break;
                case "Sin descanso":
                default:
                    descansoMin = 0;
                    break;
            }

// Si está visible el campo numérico y no esta vacio, usar ese valor
            if (editTextNumerico.getVisibility() == View.VISIBLE
                    && !editTextNumerico.getText().toString().isEmpty()) {
                descansoMin = Integer.parseInt(editTextNumerico.getText().toString());
            }

            duracionTotalMin -= descansoMin;
            if (duracionTotalMin < 0) duracionTotalMin = 0; // seguridad

            int horas = duracionTotalMin / 60;
            int minutos = duracionTotalMin % 60;

// Ahora en los EditText siempre se ven las horas ya restadas
            editHoras.setText(String.valueOf(horas));
            editMinutos.setText(String.valueOf(minutos));

            // Recuperar tarifas
            SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
            double tarifaNormal = prefs.getFloat("tarifa", 10f); // default 10

            int horasEspeciales = ((EditText) findViewById(R.id.editTextText)).getText().toString().isEmpty()
                    ? 0
                    : Integer.parseInt(((EditText) findViewById(R.id.editTextText)).getText().toString());
            double precioEspecial = ((EditText) findViewById(R.id.editTextText2)).getText().toString().isEmpty()
                    ? 0.0
                    : Double.parseDouble(((EditText) findViewById(R.id.editTextText2)).getText().toString());

            // Calcular total pagado
            double horasTotalesNormales = horas + (minutos / 60.0);
            double totalPagadoValor = (horasTotalesNormales * tarifaNormal) + (horasEspeciales * precioEspecial);

            // Mostrar texto con descanso restado
            double horasNetas = duracionTotalMin / 60.0;
            double horasDescanso = descansoMin / 60.0;

            totalPagado.setText(
                    String.format("Total: %.2f h (incluye -%.2f h descanso) | %.2f €",
                            horasNetas, horasDescanso, totalPagadoValor)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}