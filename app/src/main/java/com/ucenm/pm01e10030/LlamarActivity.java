package com.ucenm.pm01e10030;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class LlamarActivity extends AppCompatActivity {

    private static final int SOLICITAR_PERMISO_LLAMADA = 200;

    private ImageView imgContacto;
    private TextView tvNombre, tvTelefono;
    private Button btnLlamarContacto;

    private DBHelper db;
    private Contactos contacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamar);

        Button btnRegresar = findViewById(R.id.btnRegresar);

        btnRegresar.setOnClickListener(v -> {
            finish();
        });


        db = new DBHelper(this);

        imgContacto = findViewById(R.id.imgContacto);
        tvNombre = findViewById(R.id.tvNombre);
        tvTelefono = findViewById(R.id.tvTelefono);
        btnLlamarContacto = findViewById(R.id.btnLlamarContacto);

        int id = getIntent().getIntExtra("id", -1);

        if (id == -1) {
            Toast.makeText(this,
                    "No se recibió un contacto válido",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        contacto = db.obtenerContactoPorId(id);

        if (contacto == null) {
            Toast.makeText(this,
                    "No se encontró el contacto",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvNombre.setText(contacto.getNombre());
        tvTelefono.setText(contacto.getTelefono());
        mostrarFoto();

        btnLlamarContacto.setOnClickListener(v -> confirmarLlamada());
    }

    private void mostrarFoto() {
        String imagenBase64 = contacto.getImagen();

        if (imagenBase64 != null && !imagenBase64.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(
                        imagenBase64, Base64.DEFAULT
                );

                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        bytes, 0, bytes.length
                );

                if (bitmap != null) {
                    imgContacto.setImageBitmap(bitmap);
                    return;
                }
            } catch (Exception ignored) {
                // Se muestra la imagen predeterminada si falla la carga.
            }
        }

        imgContacto.setImageResource(
                android.R.drawable.ic_menu_myplaces
        );
    }

    private void confirmarLlamada() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar llamada")
                .setMessage("¿Deseas llamar a "
                        + contacto.getNombre()
                        + " al número "
                        + contacto.getTelefono() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Llamar",
                        (dialog, which) -> verificarPermisoYllamar())
                .show();
    }

    private void verificarPermisoYllamar() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED) {
            realizarLlamada();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.CALL_PHONE},
                    SOLICITAR_PERMISO_LLAMADA
            );
        }
    }

    private void realizarLlamada() {
        String numero = contacto.getTelefono().trim();

        // Elimina espacios y signos comunes que no forman parte del número.
        numero = numero.replaceAll("[\\s()-]", "");

        Intent intent = new Intent(
                Intent.ACTION_CALL,
                Uri.parse("tel:" + Uri.encode(numero))
        );

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED) {
            try {
                startActivity(intent);
            } catch (SecurityException e) {
                Toast.makeText(this,
                        "No se pudo iniciar la llamada",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,
                    "No se concedió el permiso para llamar",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );

        if (requestCode == SOLICITAR_PERMISO_LLAMADA) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                realizarLlamada();
            } else {
                Toast.makeText(this,
                        "Permiso de llamada denegado",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
