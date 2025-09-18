package com.example.controldehorarios;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton btnToolbar;
    private Button btnNuevoTurno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar el toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);  // Activa soporte para Toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Oculta el título automatico


        btnToolbar = findViewById(R.id.btn_toolbar);
        btnToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, Ajustes.class);
                startActivity(intent);
            }
        });

        btnNuevoTurno = findViewById(R.id.button);
        btnNuevoTurno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NuevoTurno.class);
                startActivity(intent);
            }
        });

        btnNuevoTurno = findViewById(R.id.button3);
        btnNuevoTurno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VerTurnos.class);
                startActivity(intent);
            }
        });

    }
}

