package com.example.gestioncontactos;

public class Contact {
    public long id;
    public String country, name, phone, note;
    public byte[] image;

    public Contact(long id, String country, String name, String phone, String note, byte[] image) {
        this.id = id;
        this.country = country;
        this.name = name;
        this.phone = phone;
        this.note = note;
        this.image = image;
    }

    @Override public String toString() {
        return name + "\n" + country + " " + phone + (note == null || note.trim().isEmpty() ? "" : "\n" + note);
    }
}
