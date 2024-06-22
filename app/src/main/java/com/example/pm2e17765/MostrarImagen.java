package com.example.pm2e17765;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import DB.SQLiteConexion;
import DB.Trans;

public class MostrarImagen extends AppCompatActivity {
    ImageView imageView;
    SQLiteConexion conexion;
    long itemId;
    Button btnRegresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mostrar_imagen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);
        btnRegresar = findViewById(R.id.btn_Atras);
        conexion = new SQLiteConexion(this, Trans.DBname, null, Trans.Version);
        itemId = getIntent().getLongExtra("itemId", -1);

        if (itemId != -1) {
            String uriString = obtenerUriImagenDesdeBaseDeDatos(itemId);

            if (!uriString.isEmpty()) {
                Uri uri = Uri.parse(uriString);
                imageView.setImageURI(uri);
            } else {
                Toast.makeText(getApplicationContext(), "No se pudo encontrar la imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No se proporcionó una identificación de registro válida", Toast.LENGTH_SHORT).show();
        }

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Listado.class);
                startActivity(intent);
            }
        });
    }

    private String obtenerUriImagenDesdeBaseDeDatos(long itemId) {
        String uriString = "";

        try {
            SQLiteDatabase db = conexion.getReadableDatabase();
            Cursor cursor = db.rawQuery(Trans.SelectUriImagen + " WHERE " + Trans.id + "=?", new String[]{String.valueOf(itemId)});

            if (cursor.moveToFirst()) {
                uriString = cursor.getString(cursor.getColumnIndexOrThrow(Trans.foto));
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uriString;
    }
}