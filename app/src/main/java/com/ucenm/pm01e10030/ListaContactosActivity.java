package com.ucenm.pm01e10030;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ListaContactosActivity extends AppCompatActivity {

    private DBHelper db;
    private ArrayList<Contactos> contactos;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> nombres;
    private ListView lista;
    private EditText buscar;
    private int seleccionado = -1;

    private final int VERDE = 0xFF166534;
    private final int MORADO = 0xFF7C3AED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DBHelper(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        layout.setBackgroundColor(0xFFF7F7F7);

        TextView titulo = new TextView(this);
        titulo.setText("Lista de contactos");
        titulo.setTextSize(25);
        titulo.setTextColor(VERDE);
        titulo.setGravity(17);
        layout.addView(titulo);

        buscar = new EditText(this);
        buscar.setHint("Buscar contacto");
        buscar.setSingleLine(true);
        layout.addView(buscar);

        lista = new ListView(this);
        layout.addView(lista, new LinearLayout.LayoutParams(
                -1, 0, 1
        ));

        Button editar = boton("Actualizar", MORADO);
        Button eliminar = boton("Eliminar", 0xFFB91C1C);
        Button compartir = boton("Compartir", VERDE);
        Button verFoto = boton("Ver imagen", MORADO);
        Button llamar = boton("Llamar", VERDE);
        Button nuevo = boton("Nuevo contacto", MORADO);

        layout.addView(editar);
        layout.addView(eliminar);
        layout.addView(compartir);
        layout.addView(verFoto);
        layout.addView(llamar);
        layout.addView(nuevo);

        setContentView(layout);

        lista.setOnItemClickListener((parent, view, position, id) -> {
            seleccionado = position;
            Toast.makeText(
                    this,
                    "Contacto seleccionado",
                    Toast.LENGTH_SHORT
            ).show();
        });

        buscar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                cargarLista(s.toString());
            }

            @Override
            public void afterTextChanged(
                    android.text.Editable s) {
            }
        });

        editar.setOnClickListener(v -> {
            Contactos c = obtenerSeleccionado();
            if (c == null) return;

            Intent i = new Intent(
                    this, MainActivity.class
            );
            i.putExtra("id", c.getId());
            startActivity(i);
        });

        eliminar.setOnClickListener(v -> eliminarContacto());

        compartir.setOnClickListener(v -> {
            Contactos c = obtenerSeleccionado();
            if (c == null) return;

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(
                    Intent.EXTRA_TEXT,
                    "Nombre: " + c.getNombre()
                            + "\nTeléfono: " + c.getPais()
                            + " " + c.getTelefono()
                            + "\nNota: " + c.getNota()
            );

            startActivity(Intent.createChooser(
                    i, "Compartir contacto"
            ));
        });

        verFoto.setOnClickListener(v -> mostrarImagen());

        llamar.setOnClickListener(v -> {
            Contactos c = obtenerSeleccionado();
            if (c == null) return;

            Intent i = new Intent(
                    this, LlamarActivity.class
            );
            i.putExtra("id", c.getId());
            startActivity(i);
        });

        nuevo.setOnClickListener(v -> {
            Intent i = new Intent(
                    this, MainActivity.class
            );
            startActivity(i);
        });
    }

    private Button boton(String texto, int color) {
        Button b = new Button(this);
        b.setText(texto);
        b.setTextColor(0xFFFFFFFF);
        b.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(color)
        );
        return b;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista(buscar == null
                ? "" : buscar.getText().toString());
    }

    private void cargarLista(String filtro) {
        contactos = db.listar();
        nombres = new ArrayList<>();
        seleccionado = -1;

        for (Contactos c : contactos) {
            if (c.getNombre().toLowerCase()
                    .contains(filtro.toLowerCase())) {
                nombres.add(
                        c.getNombre() + "\n"
                                + c.getPais() + " "
                                + c.getTelefono()
                );
            }
        }

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                nombres
        );

        lista.setAdapter(adapter);
    }

    private Contactos obtenerSeleccionado() {
        if (seleccionado < 0
                || seleccionado >= lista.getCount()) {
            Toast.makeText(
                    this,
                    "Seleccione un contacto",
                    Toast.LENGTH_SHORT
            ).show();
            return null;
        }

        String texto = nombres.get(seleccionado);
        String nombreElegido = texto.split("\n")[0];

        for (Contactos c : contactos) {
            if (c.getNombre().equals(nombreElegido)) {
                return c;
            }
        }

        return null;
    }

    private void eliminarContacto() {
        Contactos c = obtenerSeleccionado();
        if (c == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Eliminar contacto")
                .setMessage(
                        "¿Desea eliminar a " + c.getNombre() + "?"
                )
                .setPositiveButton("Sí", (dialog, which) -> {
                    db.eliminar(c.getId());
                    seleccionado = -1;
                    cargarLista(buscar.getText().toString());

                    Toast.makeText(
                            this,
                            "Contacto eliminado",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void mostrarImagen() {
        Contactos c = obtenerSeleccionado();
        if (c == null) return;

        if (c.getImagen() == null || c.getImagen().isEmpty()) {
            Toast.makeText(
                    this,
                    "El contacto no tiene fotografía",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        try {
            byte[] bytes = Base64.decode(
                    c.getImagen(), Base64.DEFAULT
            );

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.length
            );

            ImageView imagen = new ImageView(this);
            imagen.setImageBitmap(bitmap);
            imagen.setAdjustViewBounds(true);

            new AlertDialog.Builder(this)
                    .setTitle(c.getNombre())
                    .setView(imagen)
                    .setPositiveButton("Cerrar", null)
                    .show();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "No se pudo abrir la fotografía",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}