package com.example.gestioncontactos;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE = 100;
    private Spinner countrySpinner;
    private EditText nameInput, phoneInput, noteInput;
    private ImageView photoPreview;
    private byte[] imageBytes;
    private ContactDbHelper db;
    private long editId = -1;
    private final String[] countries = {"Honduras (+504)", "Costa Rica (+506)", "Guatemala (+502)", "El Salvador (+503)", "Nicaragua (+505)", "Estados Unidos (+1)"};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        db = new ContactDbHelper(this);
        buildUi();
        editId = getIntent().getLongExtra("contact_id", -1);
        if (editId >= 0) loadContact();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 24, 28, 24);
        scroll.addView(root);
        TextView title = new TextView(this);
        title.setText("Nuevo contacto");
        title.setTextSize(27);
        title.setTextColor(0xFF0D477A);
        title.setPadding(0, 0, 0, 16);
        root.addView(title);

        photoPreview = new ImageView(this);
        photoPreview.setImageResource(android.R.drawable.ic_menu_camera);
        photoPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        photoPreview.setBackgroundColor(0xFFEAF2F8);
        root.addView(photoPreview, new LinearLayout.LayoutParams(-1, 170));
        Button imageButton = button("Elegir imagen de galería");
        root.addView(imageButton);
        imageButton.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "Seleccionar imagen"), PICK_IMAGE);
        });

        addLabel(root, "País");
        countrySpinner = new Spinner(this);
        countrySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countries));
        root.addView(countrySpinner);
        addLabel(root, "Nombre");
        nameInput = editText("Nombre del contacto", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        nameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(80)});
        root.addView(nameInput);
        addLabel(root, "Teléfono");
        phoneInput = editText("Solo dígitos, sin espacios", InputType.TYPE_CLASS_PHONE);
        phoneInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        root.addView(phoneInput);
        addLabel(root, "Nota");
        noteInput = editText("Nota del contacto", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        noteInput.setMinLines(3);
        root.addView(noteInput);
        Button save = button("Guardar contacto");
        root.addView(save);
        Button list = button("Ver contactos guardados");
        root.addView(list);
        save.setOnClickListener(v -> saveContact());
        list.setOnClickListener(v -> startActivity(new Intent(this, ContactListActivity.class)));
        setContentView(scroll);
    }

    private void addLabel(LinearLayout root, String label) {
        TextView t = new TextView(this); t.setText(label); t.setTextSize(15); t.setPadding(0, 12, 0, 2); root.addView(t);
    }
    private EditText editText(String hint, int type) {
        EditText e = new EditText(this); e.setSingleLine(type != (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE));
        e.setInputType(type); e.setHint(hint); return e;
    }
    private Button button(String text) {
        Button b = new Button(this); b.setText(text); return b;
    }

    private void saveContact() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String note = noteInput.getText().toString().trim();
        if (name.isEmpty()) { nameInput.setError("Debe escribir un nombre"); nameInput.requestFocus(); return; }
        if (!Pattern.matches("[\\p{L} ]+", name)) { nameInput.setError("Use solo letras y espacios"); nameInput.requestFocus(); return; }
        if (phone.isEmpty()) { phoneInput.setError("Debe escribir un teléfono"); phoneInput.requestFocus(); return; }
        if (!Pattern.matches("[0-9]{4,15}", phone)) { phoneInput.setError("Use de 4 a 15 dígitos"); phoneInput.requestFocus(); return; }
        Contact c = new Contact(editId, countrySpinner.getSelectedItem().toString(), name, phone, note, imageBytes);
        long result;
        if (editId >= 0) result = db.update(c);
        else result = db.insert(c);
        if (result >= 0) {
            Toast.makeText(this, editId >= 0 ? "Contacto actualizado" : "Contacto guardado", Toast.LENGTH_SHORT).show();
            finish();
        } else Toast.makeText(this, "No se pudo guardar el contacto", Toast.LENGTH_LONG).show();
    }

    private void loadContact() {
        Contact c = db.get(editId);
        if (c == null) { Toast.makeText(this, "No se encontró el contacto", Toast.LENGTH_SHORT).show(); finish(); return; }
        setTitle("Actualizar contacto");
        nameInput.setText(c.name);
        phoneInput.setText(c.phone);
        noteInput.setText(c.note);
        for (int i=0; i<countries.length; i++) if (countries[i].equals(c.country)) countrySpinner.setSelection(i);
        imageBytes = c.image;
        if (imageBytes != null) photoPreview.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try (InputStream in = getContentResolver().openInputStream(uri);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096]; int n;
                while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
                imageBytes = out.toByteArray();
                photoPreview.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
            } catch (Exception e) {
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
