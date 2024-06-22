package com.example.pm2e17765;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;

import DB.Consulta;
import DB.SQLiteConexion;
import DB.Trans;

public class Listado extends AppCompatActivity {

    SQLiteConexion conexion;
    ListView listperson;
    ArrayList<Consulta> lista;
    ArrayList<String> Arreglo;

    Button btnRegresar,btnCompartir,btnVerImg,btnEliminar,btnActualizar;
    int pos=-1;

    SearchView buscar;
    ArrayAdapter<String> adapter;
    ArrayList<String> listaFiltrada;
    private static final int REQUEST_CALL_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listado);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        conexion = new SQLiteConexion(this, Trans.DBname, null, Trans.Version);
        btnRegresar = findViewById(R.id.btn_Regresar);
        listperson = (ListView) findViewById(R.id.listperson);
        btnCompartir = findViewById(R.id.btn_Compartir);
        btnVerImg = findViewById(R.id.btn_VerImg);
        btnEliminar = findViewById(R.id.btn_Eliminar);
        btnActualizar = findViewById(R.id.btn_Actualizar);

        buscar = findViewById(R.id.buscar);
        buscar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarLista(newText);
                return true;
            }
        });
        ObtenerInfo();
        //ArrayAdapter adp = new ArrayAdapter(this, android.R.layout.simple_list_item_1, Arreglo);
        //listperson.setAdapter(adp);

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Registro.class);
                startActivity(intent);
            }
        });

        listperson.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String ElementoSeleccionado = (String) parent.getItemAtPosition(position);
                //Toast.makeText(getApplicationContext(), ElementoSeleccionado, Toast.LENGTH_LONG).show();

                pos = position;
                Consulta persona = lista.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(Listado.this);
                builder.setTitle("Confirmar acción");
                builder.setMessage("¿Quieres llamar a " + persona.getNombre() + "?");
                builder.setPositiveButton("Llamar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkCallPermission()) {
                            makePhoneCall();
                        }
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                builder.show();
            }
        });

        btnCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos != -1) {
                    compartir(pos);
                } else {
                    Toast.makeText(getApplicationContext(), "Seleccione una persona", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnVerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos != -1) {
                    Consulta personaSeleccionada = lista.get(pos);
                    long idSeleccionado = personaSeleccionada.getId();

                    Intent intent = new Intent(Listado .this, MostrarImagen.class);
                    intent.putExtra("itemId", idSeleccionado);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Ninguna persona seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos != -1) {
                    eliminar(pos);
                    lista.remove(pos);
                    Arreglo.remove(pos);

                    adapter = new ArrayAdapter<>(Listado.this, android.R.layout.simple_list_item_1, Arreglo);
                    listperson.setAdapter(adapter);

                    Toast.makeText(getApplicationContext(), "Se ha elminado el contacto", Toast.LENGTH_SHORT).show();
                    pos = -1;
                } else {
                    Toast.makeText(getApplicationContext(), "Seleccione una persona", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pos != -1) {
                    Consulta pers = lista.get(pos);
                    Intent intent = new Intent(Listado.this, Registro.class);
                    intent.putExtra("id", pers.getId().toString());
                    intent.putExtra("pais", pers.getPais());
                    intent.putExtra("nombre", pers.getNombre());
                    intent.putExtra("telefono", pers.getTelefono());
                    intent.putExtra("nota", pers.getNota());
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Seleccione una persona", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void ObtenerInfo() {
        SQLiteDatabase db = conexion.getReadableDatabase();
        Consulta consult = null;
        lista = new ArrayList<Consulta>();
        Arreglo = new ArrayList<>();

        HashMap<String, String> paisCodigoMap = new HashMap<>();
        paisCodigoMap.put("Honduras (+504)", "504");
        paisCodigoMap.put("Costa Rica (+506)", "506");
        paisCodigoMap.put("Guatemala (+502)", "502");
        paisCodigoMap.put("El Salvador (+503)", "503");

        // Cursor para recorrer los datos de la tabla
        Cursor cursor = db.rawQuery(Trans.SelectAllPerson,null);

        while(cursor.moveToNext()) {
            consult = new Consulta();
            consult.setId(cursor.getInt(0));
            consult.setPais(cursor.getString(1));
            consult.setNombre(cursor.getString(2));
            consult.setTelefono(cursor.getString(3));
            consult.setNota(cursor.getString(4));

            String pais = consult.getPais();
            String codigoPais = paisCodigoMap.get(pais);
            String telefonoConCodigo = codigoPais + consult.getTelefono();

            lista.add(consult);
            Arreglo.add(consult.getNombre()+ " | " + telefonoConCodigo);
        }
        cursor.close();
        listaFiltrada = new ArrayList<>(Arreglo); // Inicialización de listaFiltrada con la lista completa
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFiltrada);
        listperson.setAdapter(adapter);
    }

    private void filtrarLista(String texto) {
        listaFiltrada.clear();
        if (texto.isEmpty()) {
            listaFiltrada.addAll(Arreglo);
        } else {
            texto = texto.toLowerCase();
            for (String item : Arreglo) {
                if (item.toLowerCase().contains(texto)) {
                    listaFiltrada.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean checkCallPermission() {
        if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            return false;
        } else {
            return true;
        }
    }

    private void makePhoneCall() {
        Consulta persona = lista.get(pos);
        Intent intent = new Intent(Listado.this, Llamada.class);
        intent.putExtra("nombre", persona.getNombre());
        intent.putExtra("pais", persona.getPais());
        intent.putExtra("telefono", String.valueOf(persona.getTelefono()));
        startActivity(intent);
        //Toast.makeText(getApplicationContext(), "llamando", Toast.LENGTH_LONG).show();
    }

    private void compartir(int posicion) {
        Consulta pos = lista.get(posicion);
        String mensaje = "País: " + pos.getPais() + "\n" +
                "Nombre: " + pos.getNombre() + "\n" +
                "Teléfono: " + pos.getTelefono() + "\n" +
                "Nota: " + pos.getNota();
        Intent intentCompartir = new Intent(Intent.ACTION_SEND);
        intentCompartir.setType("text/plain");
        intentCompartir.putExtra(Intent.EXTRA_SUBJECT, "Datos personales");
        intentCompartir.putExtra(Intent.EXTRA_TEXT, mensaje);
        startActivity(Intent.createChooser(intentCompartir, "Compartir"));
    }

    private void eliminar(int posicion) {
        Consulta pers = lista.get(posicion);
        SQLiteDatabase db = conexion.getWritableDatabase();
        db.delete(Trans.TablePersonas, Trans.id + "=?", new String[]{String.valueOf(pers.getId())});
        db.close();
    }


}