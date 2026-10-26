package com.example.gestioncontactos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends Activity {
    private ContactDbHelper db;
    private EditText search;
    private ListView listView;
    private ArrayAdapter<Contact> adapter;
    private final List<Contact> contacts = new ArrayList<>();
    private Contact selected;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        db = new ContactDbHelper(this);
        buildUi();
        refresh("");
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        TextView title = new TextView(this);
        title.setText("Contactos guardados"); title.setTextSize(26); title.setTextColor(0xFF0D477A);
        root.addView(title);
        search = new EditText(this); search.setHint("Buscar por nombre o teléfono");
        root.addView(search);
        listView = new ListView(this);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, contacts);
        listView.setAdapter(adapter);
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selected = contacts.get(position);
            listView.setItemChecked(position, true);
            showActions();
        });
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { refresh(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });
        Button add = button("Registrar contacto");
        root.addView(add);
        add.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        setContentView(root);
    }

    private Button button(String text) { Button b = new Button(this); b.setText(text); return b; }

    private void refresh(String q) {
        contacts.clear(); contacts.addAll(db.all(q)); adapter.notifyDataSetChanged();
        selected = null;
    }

    private void showActions() {
        if (selected == null) return;
        String[] options = {"Actualizar", "Eliminar", "Compartir", "Ver imagen", "Llamar"};
        new AlertDialog.Builder(this).setTitle(selected.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent i = new Intent(this, MainActivity.class);
                        i.putExtra("contact_id", selected.id); startActivity(i);
                    } else if (which == 1) confirmDelete();
                    else if (which == 2) shareContact();
                    else if (which == 3) viewImage();
                    else if (which == 4) {
                        Intent i = new Intent(this, CallActivity.class);
                        i.putExtra("contact_id", selected.id); startActivity(i);
                    }
                }).show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this).setTitle("Eliminar contacto")
                .setMessage("¿Desea eliminar a " + selected.name + "?")
                .setNegativeButton("No", (d, w) -> {})
                .setPositiveButton("Sí", (d, w) -> {
                    db.delete(selected.id); refresh(search.getText().toString());
                    Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void shareContact() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, selected.name + " — " + selected.country + " " + selected.phone +
                (selected.note == null || selected.note.isEmpty() ? "" : "\nNota: " + selected.note));
        startActivity(Intent.createChooser(send, "Compartir contacto"));
    }

    private void viewImage() {
        if (selected.image == null || selected.image.length == 0) {
            new AlertDialog.Builder(this).setMessage("Este contacto no tiene una imagen guardada.").setPositiveButton("Aceptar", null).show();
            return;
        }
        ImageView image = new ImageView(this);
        image.setAdjustViewBounds(true);
        image.setImageBitmap(BitmapFactory.decodeByteArray(selected.image, 0, selected.image.length));
        new AlertDialog.Builder(this).setTitle(selected.name).setView(image).setPositiveButton("Cerrar", null).show();
    }

    @Override protected void onResume() {
        super.onResume();
        if (db != null && search != null) refresh(search.getText().toString());
    }
}
