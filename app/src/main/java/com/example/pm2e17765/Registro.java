package com.example.pm2e17765;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import DB.SQLiteConexion;
import DB.Trans;

public class Registro extends AppCompatActivity {

    //Para formulario
    EditText nombre, telefono, nota, foto;
    Button bt_agregar, bt_contactos;

    //Para foto
    static final int peticion_acceso_camara = 101;
    static final int peticion_captura_imagen = 102;
    static final int peticion_acceso_lectura_externa = 103;
    static final int peticion_acceso_escritura_externa = 104;
    ImageView ObjectoImagen;
    Button btncaptura;
    String id = "0";
    Spinner pais_sp;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nombre = (EditText) findViewById(R.id.edt_Nombre);
        telefono = (EditText) findViewById(R.id.edt_Telefono);
        nota = (EditText) findViewById(R.id.edt_Nota);
        bt_agregar = (Button) findViewById(R.id.btn_Salvar);
        bt_contactos = (Button) findViewById(R.id.btn_Contactos);
        pais_sp = (Spinner) findViewById(R.id.sp_Pais);
        ObjectoImagen = (ImageView) findViewById(R.id.iv_Foto);
        btncaptura = (Button) findViewById(R.id.btn_Foto);

        String[] paises = {"Honduras (+504)", "Costa Rica (+506)", "Guatemala (+502)", "El Salvador (+503)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pais_sp.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent.hasExtra("id")) {
            //Datos persona
            id = intent.getStringExtra("id");
            nombre.setText(intent.getStringExtra("nombre"));
            telefono.setText(intent.getStringExtra("telefono"));
            nota.setText(intent.getStringExtra("nota"));
            //Spinner
            String paisSeleccionado = intent.getStringExtra("pais");
            if (pais_sp.getAdapter() instanceof ArrayAdapter) {
                adapter = (ArrayAdapter<String>) pais_sp.getAdapter();
            } else {
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paises);
                pais_sp.setAdapter(adapter);
            }
            int position = adapter.getPosition(paisSeleccionado);
            pais_sp.setSelection(position);

            //Traer Foto
            long idLong = Long.parseLong(id);
            String uriString = obtenerUriImagenDesdeBaseDeDatos(idLong);
            if (!uriString.isEmpty()) {
                Uri uri = Uri.parse(uriString);
                ObjectoImagen.setImageURI(uri);
                imageUri=uri;
            } else {
                Toast.makeText(getApplicationContext(), "No se pudo encontrar la imagen", Toast.LENGTH_SHORT).show();
            }
        }

        bt_agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(intent.hasExtra("id")){
                    Actualizar();
                }else{
                    Agregar();
                }
            }
        });

        bt_contactos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Listado.class);
                startActivity(intent);
            }
        });

        btncaptura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Permisos();
            }
        });
    }

    //Para agregar un registro a la tabla
    private void Agregar() {
        String v_nom = nombre.getText().toString().trim();
        String v_tel = telefono.getText().toString().trim();
        String v_nota = nota.getText().toString().trim();
        String v_pais = pais_sp.getSelectedItem().toString();

        if (v_nom.isEmpty()) {
            showAlert("Ingrese un nombre.");
            return;
        } else if (v_tel.isEmpty()) {
            showAlert("Ingrese un número de teléfono");
            return;
        } else if (v_nota.isEmpty()) {
            showAlert("Ingrese una nota");
            return;
        } else if (ObjectoImagen.getDrawable() == null) {
            showAlert("Debe de tomar una foto");
            return;
        } else {
            try {
                SQLiteConexion conexion = new SQLiteConexion(this, Trans.DBname, null, Trans.Version);
                SQLiteDatabase db = conexion.getWritableDatabase();

                ContentValues valores  = new ContentValues();
                valores.put(Trans.pais, v_pais);
                valores.put(Trans.nombre, v_nom);
                valores.put(Trans.telefono, v_tel);
                valores.put(Trans.nota, v_nota);
                valores.put(Trans.foto, imageUri.toString());

                Long resultado = db.insert(Trans.TablePersonas, Trans.id, valores);

                Toast.makeText(getApplicationContext(),"Registro ingresado con exito " + resultado.toString(),
                        Toast.LENGTH_LONG).show();
                db.close();
                Intent intent = new Intent(getApplicationContext(), Listado.class);
                startActivity(intent);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Comprueba los permisos necesarios
    private void Permisos() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
            new String [] {Manifest.permission.CAMERA},
            peticion_acceso_camara);
        } else {
            TomarFoto();
        }
    }

    private void TomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!= null) {
            startActivityForResult(intent,  peticion_captura_imagen);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == peticion_acceso_camara) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                TomarFoto();
            }
            else {
                Toast.makeText(getApplicationContext(), "Acceso Denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void guardarFotoEnGaleria(Bitmap bitmap) {
        String nomFoto = "pm1_Ex_" + System.currentTimeMillis() + ".jpg";
        String dirFoto = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, nomFoto, null);

        if (dirFoto != null) {
            Intent intentScan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uriImagen = Uri.parse(dirFoto);
            intentScan.setData(uriImagen);
            sendBroadcast(intentScan);

            Toast.makeText(getApplicationContext(), "Imagen guardada en su galeria.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Error: no se pudo guardar la imagen.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == peticion_captura_imagen && resultCode == RESULT_OK) {
            if( data != null){
                Bundle extras = data.getExtras();
                if(extras != null){
                    Bitmap imagen = (Bitmap) extras.get("data");
                    ObjectoImagen.setImageBitmap(imagen);
                    guardarFotoEnGaleria(imagen);
                    imageUri = getImageUri(imagen);
                }
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void Actualizar() {
        String v_nom = nombre.getText().toString().trim();
        String v_tel = telefono.getText().toString().trim();
        String v_nota = nota.getText().toString().trim();
        String v_pais = pais_sp.getSelectedItem().toString();

        if (v_nom.isEmpty()) {
            showAlert("Ingrese un nombre.");
            return;
        } else if (v_tel.isEmpty()) {
            showAlert("Ingrese un número de teléfono");
            return;
        } else if (v_nota.isEmpty()) {
            showAlert("Ingrese una nota");
            return;
        } else if (ObjectoImagen.getDrawable() == null) {
            showAlert("Debe de tomar una foto");
            return;
        } else {
            try {
                SQLiteConexion conexion = new SQLiteConexion(this, Trans.DBname, null, Trans.Version);
                SQLiteDatabase db = conexion.getWritableDatabase();

                ContentValues valores = new ContentValues();
                valores.put(Trans.pais, v_pais);
                valores.put(Trans.nombre, v_nom);
                valores.put(Trans.telefono, v_tel);
                valores.put(Trans.nota, v_nota);
                valores.put(Trans.foto, imageUri.toString());

                int filasAfectadas = db.update(Trans.TablePersonas, valores, Trans.id + "=?", new String[]{id});
                db.close();

                if (filasAfectadas > 0) {
                    Toast.makeText(getApplicationContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), Listado.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String obtenerUriImagenDesdeBaseDeDatos(long itemId) {
        String uriString = "";
        SQLiteConexion conexion = new SQLiteConexion(this, Trans.DBname, null, Trans.Version);

        try {
            SQLiteDatabase db = conexion.getReadableDatabase();
            Cursor cursor = db.rawQuery(Trans.SelectUriImagen + " WHERE " + Trans.id + "=?", new String[]{String.valueOf(itemId)});

            if (cursor.moveToFirst()) {
                uriString = cursor.getString(cursor.getColumnIndexOrThrow(Trans.foto));
                Log.d("Obtener URI", "URI recuperada: " + uriString);
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uriString;
    }
}