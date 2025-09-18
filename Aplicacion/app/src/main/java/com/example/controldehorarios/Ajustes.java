package com.example.controldehorarios;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Ajustes extends AppCompatActivity {

    private EditText tarifa;
    private EditText irpf;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ajustes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tarifa = findViewById(R.id.editTarifa);
        irpf = findViewById(R.id.editIRPF);

        prefs = getSharedPreferences("config", MODE_PRIVATE);

        // Cargar la tarifa guardada
        double tarifaGuardada = prefs.getFloat("tarifa", 10f); // valor por defecto 10
        tarifa.setText(String.valueOf(tarifaGuardada));

        // Cargar IRPF
        float irpfGuardado = prefs.getFloat("irpf", 15f);
        irpf.setText(String.valueOf(irpfGuardado));

        // Guardar tarifa automáticamente
        tarifa.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    float valor = Float.parseFloat(s.toString());
                    prefs.edit().putFloat("tarifa", valor).apply();
                }
            }
        });

        // Guardar IRPF automáticamente
        irpf.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    float valor = Float.parseFloat(s.toString());
                    prefs.edit().putFloat("irpf", valor).apply();
                }
            }
        });
    }
}
