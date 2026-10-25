package com.ucenm.pm01e10030;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ListaContactosActivity extends AppCompatActivity {

    private EditText etBuscar;
    private ListView lvContactos;

    private Button btnEditar, btnEliminar, btnCompartir;
    private Button btnVerImagen, btnLlamar, btnNuevo;

    private DBHelper db;
    private ArrayList<Contactos> todosLosContactos = new ArrayList<>();
    private ArrayList<Contactos> contactosFiltrados = new ArrayList<>();
    private ArrayAdapter<String> adaptador;
    private int posicionSeleccionada = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);

        db = new DBHelper(this);

        etBuscar = findViewById(R.id.etBuscar);
        lvContactos = findViewById(R.id.lvContactos);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnCompartir = findViewById(R.id.btnCompartir);
        btnVerImagen = findViewById(R.id.btnVerImagen);
        btnLlamar = findViewById(R.id.btnLlamar);
        btnNuevo = findViewById(R.id.btnNuevo);

        adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_single_choice,
                new ArrayList<>()
        );

        lvContactos.setAdapter(adaptador);
        lvContactos.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        lvContactos.setOnItemClickListener((parent, view, position, id) -> {
            posicionSeleccionada = position;
        });

        etBuscar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                filtrarContactos(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });

        btnEditar.setOnClickListener(v -> editarContacto());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
        btnCompartir.setOnClickListener(v -> compartirContacto());
        btnVerImagen.setOnClickListener(v -> mostrarImagen());
        btnLlamar.setOnClickListener(v -> abrirLlamada());

        btnNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ListaContactosActivity.this,
                    MainActivity.class
            );
            startActivity(intent);
        });

        cargarContactos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarContactos();
    }

    private void cargarContactos() {
        List<Contactos> lista = db.obtenerContactos();

        todosLosContactos.clear();
        if (lista != null) {
            todosLosContactos.addAll(lista);
        }

        filtrarContactos(etBuscar.getText().toString());
    }

    private void filtrarContactos(String texto) {
        contactosFiltrados.clear();

        for (Contactos c : todosLosContactos) {
            if (c.getNombre() != null
                    && c.getNombre().toLowerCase()
                    .contains(texto.toLowerCase())) {
                contactosFiltrados.add(c);
            }
        }

        ArrayList<String> nombres = new ArrayList<>();

        for (Contactos c : contactosFiltrados) {
            String linea = c.getNombre() + " - " + c.getTelefono();
            nombres.add(linea);
        }

        adaptador.clear();
        adaptador.addAll(nombres);
        adaptador.notifyDataSetChanged();
        posicionSeleccionada = -1;
    }

    private Contactos obtenerSeleccionado() {
        if (posicionSeleccionada < 0
                || posicionSeleccionada >= contactosFiltrados.size()) {
            Toast.makeText(this,
                    "Selecciona un contacto primero",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        return contactosFiltrados.get(posicionSeleccionada);
    }

    private void editarContacto() {
        Contactos contacto = obtenerSeleccionado();
        if (contacto == null) return;

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("id", contacto.getId());
        startActivity(intent);
    }

    private void confirmarEliminar() {
        Contactos contacto = obtenerSeleccionado();
        if (contacto == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Eliminar contacto")
                .setMessage("¿Deseas eliminar a "
                        + contacto.getNombre() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    boolean eliminado =
                            db.eliminarContacto(contacto.getId());

                    if (eliminado) {
                        Toast.makeText(this,
                                "Contacto eliminado",
                                Toast.LENGTH_SHORT).show();
                        cargarContactos();
                    } else {
                        Toast.makeText(this,
                                "No se pudo eliminar el contacto",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void compartirContacto() {
        Contactos contacto = obtenerSeleccionado();
        if (contacto == null) return;

        String mensaje = "Contacto: " + contacto.getNombre()
                + "\nTeléfono: " + contacto.getTelefono()
                + "\nPaís: " + contacto.getPais()
                + "\nNota: " + contacto.getNota();

        Intent compartir = new Intent(Intent.ACTION_SEND);
        compartir.setType("text/plain");
        compartir.putExtra(Intent.EXTRA_TEXT, mensaje);

        startActivity(Intent.createChooser(
                compartir, "Compartir contacto"
        ));
    }

    private void mostrarImagen() {
        Contactos contacto = obtenerSeleccionado();
        if (contacto == null) return;

        if (contacto.getImagen() == null
                || contacto.getImagen().isEmpty()) {
            Toast.makeText(this,
                    "Este contacto no tiene fotografía",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            byte[] bytes = Base64.decode(
                    contacto.getImagen(), Base64.DEFAULT
            );

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.length
            );

            if (bitmap == null) {
                Toast.makeText(this,
                        "No se pudo mostrar la fotografía",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            ImageView imagen = new ImageView(this);
            imagen.setImageBitmap(bitmap);
            imagen.setAdjustViewBounds(true);
            imagen.setPadding(16, 16, 16, 16);

            new AlertDialog.Builder(this)
                    .setTitle(contacto.getNombre())
                    .setView(imagen)
                    .setPositiveButton("Cerrar", null)
                    .show();

        } catch (Exception e) {
            Toast.makeText(this,
                    "Error al cargar la fotografía",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirLlamada() {
        Contactos contacto = obtenerSeleccionado();
        if (contacto == null) return;

        Intent intent = new Intent(this, LlamarActivity.class);
        intent.putExtra("id", contacto.getId());
        startActivity(intent);
    }
}
