package model;

public class Libro {
    private int id;
    private String titulo;
    private String ubicacion;
    private String categoria;
    private String editorial;
    private String autor;
    private int unidades;

    public Libro(String titulo, String ubicacion, String categoria, String editorial, String autor, int unidades) {
        this.titulo = titulo;
        this.ubicacion = ubicacion;
        this.categoria = categoria;
        this.editorial = editorial;
        this.autor = autor;
        this.unidades = unidades;
    }

    public Libro(int id, String titulo, String ubicacion, String categoria, String editorial, String autor, int unidades) {
        this.unidades = unidades;
        this.autor = autor;
        this.editorial = editorial;
        this.categoria = categoria;
        this.ubicacion = ubicacion;
        this.id = id;
        this.titulo = titulo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String nombre) {
        this.titulo = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getUnidades() {
        return unidades;
    }

    public void setUnidades(int unidades) {
        this.unidades = unidades;
    }
}
