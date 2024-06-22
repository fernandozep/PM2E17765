package com.example.pm2e17765;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;

public class Llamada extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 1;

    TextView txtNombre,txtTelefono;
    Button btnRegreso,btnLlamada;
    String nom,tel,pais,codigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_llamada);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtNombre = findViewById(R.id.txt_Pers);
        txtTelefono = findViewById(R.id.txt_Num);
        btnLlamada = findViewById(R.id.btn_Llamar);
        btnRegreso = findViewById(R.id.btn_Regresar);
        nom = getIntent().getStringExtra("nombre");
        tel = getIntent().getStringExtra("telefono");
        pais = getIntent().getStringExtra("pais");
        txtNombre.setText(nom);
        txtTelefono.setText(tel);

        HashMap<String, String> codigoMap = new HashMap<>();
        codigoMap.put("Honduras (+504)", "+504");
        codigoMap.put("Costa Rica (+506)", "+506");
        codigoMap.put("Guatemala (+502)", "+502");
        codigoMap.put("El Salvador (+503)", "+503");

        codigo = codigoMap.get(pais);

        String telefonoCompleto = codigo + tel;
        txtTelefono.setText(telefonoCompleto);

        btnLlamada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Llamada.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Llamada.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
                } else {
                    String telefono = txtTelefono.getText().toString();
                    Intent intentLlamada = new Intent(Intent.ACTION_CALL);
                    intentLlamada.setData(Uri.parse("tel:" + telefonoCompleto));
                    startActivity(intentLlamada);
                }
            }
        });
        btnRegreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Listado.class);
                startActivity(intent);
            }
        });
    }
}