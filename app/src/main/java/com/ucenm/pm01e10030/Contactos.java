package com.ucenm.pm01e10030;

public class Contactos {

    private int id;
    private String pais;
    private String nombre;
    private String telefono;
    private String nota;
    private String imagen;

    public Contactos(int id, String pais, String nombre,
                     String telefono, String nota, String imagen) {
        this.id = id;
        this.pais = pais;
        this.nombre = nombre;
        this.telefono = telefono;
        this.nota = nota;
        this.imagen = imagen;
    }

    public Contactos(String pais, String nombre,
                     String telefono, String nota, String imagen) {
        this(0, pais, nombre, telefono, nota, imagen);
    }

    public int getId() {
        return id;
    }

    public String getPais() {
        return pais;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getNota() {
        return nota;
    }

    public String getImagen() {
        return imagen;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}