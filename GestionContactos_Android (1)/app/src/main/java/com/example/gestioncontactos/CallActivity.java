package com.example.gestioncontactos;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CallActivity extends Activity {
    private static final int REQUEST_CALL = 41;
    private Contact contact;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        long id = getIntent().getLongExtra("contact_id", -1);
        contact = new ContactDbHelper(this).get(id);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 50, 28, 28);
        TextView title = new TextView(this); title.setText("Llamar"); title.setTextSize(28); title.setTextColor(0xFF0D477A);
        root.addView(title);
        TextView detail = new TextView(this);
        detail.setText(contact == null ? "No se encontró el contacto" : contact.name + "\n" + contact.country + " " + contact.phone);
        detail.setTextSize(20); detail.setPadding(0, 30, 0, 30); root.addView(detail);
        setContentView(root);
        if (contact != null) new AlertDialog.Builder(this)
                .setTitle("Confirmar llamada")
                .setMessage("¿Desea llamar a " + contact.name + "?\n" + fullNumber(contact))
                .setNegativeButton("No", (d, w) -> finish())
                .setPositiveButton("Sí", (d, w) -> requestAndCall())
                .setOnCancelListener(d -> finish())
                .show();
    }

    private String fullNumber(Contact c) {
        String code = c.country.substring(c.country.indexOf('(') + 1, c.country.indexOf(')'));
        return "+" + code + " " + c.phone;
    }

    private void requestAndCall() {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        } else makeCall();
    }

    private void makeCall() {
        if (contact == null) return;
        String uri = "tel:+" + contact.country.substring(contact.country.indexOf('(') + 1, contact.country.indexOf(')')) + contact.phone;
        try {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(uri)));
        } catch (SecurityException e) {
            Toast.makeText(this, "No se concedió permiso para llamar", Toast.LENGTH_LONG).show();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) makeCall();
        else if (requestCode == REQUEST_CALL) Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_LONG).show();
    }
}
