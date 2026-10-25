
package com.ucenm.pm01e10030;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private EditText nombre, telefono, nota;
    private Spinner pais;
    private ImageView foto;
    private DBHelper db;

    private String imagenBase64 = "";
    private int idEditar = -1;

    private final String[] paises = {
            "Honduras (+504)",
            "Guatemala (+502)",
            "El Salvador (+503)",
            "Nicaragua (+505)",
            "Costa Rica (+506)",
            "Estados Unidos (+1)",
            "México (+52)"
    };

    private final int VERDE = 0xFF166534;
    private final int MORADO = 0xFF7C3AED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DBHelper(this);

        construirPantalla();

        idEditar = getIntent().getIntExtra("id", -1);

        if (idEditar != -1) {
            cargarContacto();
        }
    }

    private void construirPantalla() {

        ScrollView scroll = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 24, 28, 24);
        layout.setBackgroundColor(0xFFF7F7F7);

        scroll.addView(layout);
        setContentView(scroll);

        TextView titulo = new TextView(this);
        titulo.setText("Gestión de contactos");
        titulo.setTextSize(26);
        titulo.setTextColor(VERDE);
        titulo.setGravity(Gravity.CENTER);
        layout.addView(titulo);

        TextView subtitulo = new TextView(this);
        subtitulo.setText("Nuevo contacto");
        subtitulo.setTextSize(19);
        subtitulo.setGravity(Gravity.CENTER);
        subtitulo.setTextColor(MORADO);
        layout.addView(subtitulo);

        foto = new ImageView(this);

        LinearLayout.LayoutParams fp =
                new LinearLayout.LayoutParams(180, 180);

        fp.gravity = Gravity.CENTER;
        fp.setMargins(0, 20, 0, 8);

        foto.setLayoutParams(fp);
        foto.setImageResource(
                android.R.drawable.ic_menu_camera
        );
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);

        layout.addView(foto);

        Button btnFoto = boton(
                "Seleccionar fotografía", MORADO
        );

        layout.addView(btnFoto);

        btnFoto.setOnClickListener(v -> seleccionarFoto());

        etiqueta(layout, "País");

        pais = new Spinner(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        paises
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        pais.setAdapter(adapter);
        layout.addView(pais);

        etiqueta(layout, "Nombre");

        nombre = campo(
                "Nombre completo",
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_WORDS
        );

        nombre.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {

                    for (int i = start; i < end; i++) {

                        char c = source.charAt(i);

                        if (!Character.isLetter(c)
                                && c != ' '
                                && c != '\''
                                && c != '-') {
                            return "";
                        }
                    }

                    return null;
                }
        });

        layout.addView(nombre);

        etiqueta(layout, "Teléfono");

        telefono = campo(
                "Solo números",
                InputType.TYPE_CLASS_NUMBER
        );

        telefono.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(15)
        });

        layout.addView(telefono);

        etiqueta(layout, "Nota");

        nota = campo(
                "Nota del contacto",
                InputType.TYPE_CLASS_TEXT
        );

        nota.setMinLines(2);
        nota.setGravity(Gravity.TOP);
        nota.setSingleLine(false);

        layout.addView(nota);

        Button guardar = boton(
                "Guardar contacto", VERDE
        );

        layout.addView(guardar);

        guardar.setOnClickListener(v -> guardarContacto());

        Button ver = boton(
                "Ver contactos", MORADO
        );

        layout.addView(ver);

        ver.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ListaContactosActivity.class
            );

            startActivity(intent);
        });
    }

    private void etiqueta(
            LinearLayout layout, String texto) {

        TextView tv = new TextView(this);

        tv.setText(texto);
        tv.setTextColor(VERDE);
        tv.setTextSize(16);
        tv.setPadding(0, 12, 0, 4);

        layout.addView(tv);
    }

    private EditText campo(String hint, int tipo) {

        EditText editText = new EditText(this);

        editText.setHint(hint);
        editText.setTextSize(16);
        editText.setInputType(tipo);
        editText.setPadding(16, 8, 16, 8);

        if (tipo == InputType.TYPE_CLASS_NUMBER) {
            editText.setSingleLine(true);
        }

        return editText;
    }

    private Button boton(String texto, int color) {

        Button button = new Button(this);

        button.setText(texto);
        button.setTextColor(0xFFFFFFFF);

        button.setBackgroundTintList(
                ColorStateList.valueOf(color)
        );

        return button;
    }

    private void seleccionarFoto() {

        Intent intent = new Intent(
                Intent.ACTION_GET_CONTENT
        );

        intent.setType("image/*");

        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Seleccionar imagen"
                ),
                100
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode, resultCode, data
        );

        if (requestCode == 100
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {

            try {

                Uri uri = data.getData();

                Bitmap bitmap;

                try (InputStream is =
                             getContentResolver()
                                     .openInputStream(uri)) {

                    if (is == null) {
                        throw new Exception(
                                "No se pudo abrir la imagen"
                        );
                    }

                    bitmap = BitmapFactory.decodeStream(is);
                }

                if (bitmap == null) {
                    throw new Exception(
                            "Imagen no válida"
                    );
                }

                foto.setImageBitmap(bitmap);

                ByteArrayOutputStream out =
                        new ByteArrayOutputStream();

                bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        70,
                        out
                );

                // Compatible con Android API 24.
                imagenBase64 = Base64.encodeToString(
                        out.toByteArray(),
                        Base64.NO_WRAP
                );

                Toast.makeText(
                        this,
                        "Fotografía seleccionada",
                        Toast.LENGTH_SHORT
                ).show();

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "No se pudo cargar la imagen",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void guardarContacto() {

        String n = nombre.getText()
                .toString().trim();

        String t = telefono.getText()
                .toString().trim();

        String no = nota.getText()
                .toString().trim();

        if (n.isEmpty()) {

            nombre.setError("Ingrese el nombre");
            nombre.requestFocus();
            return;
        }

        if (!n.matches(
                "[\\p{L}][\\p{L} '\\-]*")) {

            nombre.setError(
                    "Ingrese un nombre válido"
            );

            nombre.requestFocus();
            return;
        }

        if (t.isEmpty()) {

            telefono.setError(
                    "Ingrese el teléfono"
            );

            telefono.requestFocus();
            return;
        }

        if (!t.matches("\\d{7,15}")) {

            telefono.setError(
                    "Use entre 7 y 15 dígitos"
            );

            telefono.requestFocus();
            return;
        }

        if (no.isEmpty()) {

            nota.setError("Ingrese una nota");
            nota.requestFocus();
            return;
        }

        String p = pais.getSelectedItem().toString();

        Contactos contacto = new Contactos(
                p,
                n,
                t,
                no,
                imagenBase64
        );

        long resultado;

        if (idEditar == -1) {

            resultado = db.insertar(contacto);

        } else {

            contacto.setId(idEditar);

            resultado = db.actualizar(contacto);
        }

        boolean guardado = idEditar == -1
                ? resultado != -1
                : resultado > 0;

        if (guardado) {

            Toast.makeText(
                    this,
                    idEditar == -1
                            ? "Contacto guardado correctamente"
                            : "Contacto actualizado correctamente",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {

            Toast.makeText(
                    this,
                    "No se pudo guardar el contacto",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void cargarContacto() {

        Contactos contacto = db.buscarPorId(idEditar);

        if (contacto == null) {

            Toast.makeText(
                    this,
                    "No se encontró el contacto",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        nombre.setText(contacto.getNombre());
        telefono.setText(contacto.getTelefono());
        nota.setText(contacto.getNota());

        for (int i = 0; i < paises.length; i++) {

            if (paises[i].equals(contacto.getPais())) {
                pais.setSelection(i);
                break;
            }
        }

        imagenBase64 = contacto.getImagen() == null
                ? ""
                : contacto.getImagen();

        if (!imagenBase64.isEmpty()) {

            try {

                byte[] bytes = Base64.decode(
                        imagenBase64,
                        Base64.DEFAULT
                );

                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.length
                );

                if (bitmap != null) {
                    foto.setImageBitmap(bitmap);
                }

            } catch (IllegalArgumentException e) {

                Toast.makeText(
                        this,
                        "No se pudo recuperar la fotografía",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}
