package com.ucenm.pm01e10030;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int SOLICITAR_FOTO = 100;

    private ImageView foto;
    private EditText nombre, telefono, nota;
    private Spinner pais;
    private Button btnGuardar, btnFoto, btnVer;

    private DBHelper db;
    private String imagenBase64 = "";
    private int idEditar = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(this);

        foto = findViewById(R.id.imgFoto);
        nombre = findViewById(R.id.etNombre);
        telefono = findViewById(R.id.etTelefono);
        nota = findViewById(R.id.etNota);
        pais = findViewById(R.id.spPais);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnFoto = findViewById(R.id.btnFoto);
        btnVer = findViewById(R.id.btnVerContactos);

        String[] paises = {
                "Honduras (+504)",
                "Guatemala (+502)",
                "El Salvador (+503)",
                "Nicaragua (+505)",
                "Costa Rica (+506)",
                "Estados Unidos (+1)",
                "México (+52)"
        };

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                paises
        );
        adaptador.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        pais.setAdapter(adaptador);

        btnFoto.setOnClickListener(v -> seleccionarFoto());
        btnGuardar.setOnClickListener(v -> guardarContacto());
        btnVer.setOnClickListener(v ->
                startActivity(new Intent(this, ListaContactosActivity.class))
        );

        idEditar = getIntent().getIntExtra("id", -1);

        if (idEditar != -1) {
            cargarContactoParaEditar();
            btnGuardar.setText("Actualizar contacto");
        }
    }

    private void seleccionarFoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Seleccionar fotografía"),
                SOLICITAR_FOTO
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SOLICITAR_FOTO
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {
            try {
                Uri uri = data.getData();

                InputStream input = getContentResolver()
                        .openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(input);

                if (input != null) {
                    input.close();
                }

                if (bitmap != null) {
                    // Reducir la imagen para no guardar un archivo enorme
                    Bitmap reducida = Bitmap.createScaledBitmap(
                            bitmap, 400, 400, true
                    );

                    ByteArrayOutputStream salida =
                            new ByteArrayOutputStream();

                    reducida.compress(
                            Bitmap.CompressFormat.JPEG, 75, salida
                    );

                    imagenBase64 = Base64.encodeToString(
                            salida.toByteArray(), Base64.DEFAULT
                    );

                    foto.setImageBitmap(reducida);
                }
            } catch (Exception e) {
                Toast.makeText(this,
                        "No se pudo cargar la fotografía",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarContacto() {
        String nombreTexto = nombre.getText().toString().trim();
        String telefonoTexto = telefono.getText().toString().trim();
        String notaTexto = nota.getText().toString().trim();
        String paisTexto = pais.getSelectedItem().toString();

        if (nombreTexto.isEmpty()) {
            nombre.setError("Escribe el nombre");
            nombre.requestFocus();
            return;
        }

        if (telefonoTexto.isEmpty()) {
            telefono.setError("Escribe el teléfono");
            telefono.requestFocus();
            return;
        }

        String soloDigitos = telefonoTexto.replaceAll("[\\s()-]", "");
        if (!soloDigitos.matches("\\+?\\d{7,15}")) {
            telefono.setError("Introduce un teléfono válido");
            telefono.requestFocus();
            return;
        }

        Contactos contacto = new Contactos(
                paisTexto,
                nombreTexto,
                telefonoTexto,
                notaTexto,
                imagenBase64
        );

        boolean guardado;

        if (idEditar == -1) {
            guardado = db.insertarContacto(contacto);
        } else {
            contacto.setId(idEditar);
            guardado = db.actualizarContacto(contacto);
        }

        if (guardado) {
            Toast.makeText(this,
                    idEditar == -1
                            ? "Contacto guardado"
                            : "Contacto actualizado",
                    Toast.LENGTH_SHORT).show();

            if (idEditar != -1) {
                finish();
            } else {
                limpiarFormulario();
            }
        } else {
            Toast.makeText(this,
                    "No se pudo guardar el contacto",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarContactoParaEditar() {
        Contactos contacto = db.obtenerContactoPorId(idEditar);

        if (contacto == null) {
            Toast.makeText(this, "No se encontró el contacto",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nombre.setText(contacto.getNombre());
        telefono.setText(contacto.getTelefono());
        nota.setText(contacto.getNota());

        if (contacto.getPais() != null) {
            for (int i = 0; i < pais.getCount(); i++) {
                if (pais.getItemAtPosition(i).toString()
                        .equals(contacto.getPais())) {
                    pais.setSelection(i);
                    break;
                }
            }
        }

        imagenBase64 = contacto.getImagen() == null
                ? "" : contacto.getImagen();

        mostrarFotoGuardada();
    }

    private void mostrarFotoGuardada() {
        if (!imagenBase64.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(
                        imagenBase64, Base64.DEFAULT
                );
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        bytes, 0, bytes.length
                );
                if (bitmap != null) {
                    foto.setImageBitmap(bitmap);
                }
            } catch (Exception ignored) {
                foto.setImageResource(android.R.drawable.ic_menu_camera);
            }
        }
    }

    private void limpiarFormulario() {
        nombre.setText("");
        telefono.setText("");
        nota.setText("");
        pais.setSelection(0);
        imagenBase64 = "";
        foto.setImageResource(android.R.drawable.ic_menu_camera);
    }
}
