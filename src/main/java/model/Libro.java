package model;

public class Libro {
    private int id;
    private String titulo;
    private String ubicacion;
    private int id_categoria;
    private String categoria;
    private String editorial;
    private String autor;
    private int unidades;
    private int id_editorial;
    private Editorial editorialObj;

    public Libro(String titulo, String ubicacion, int id_categoria, int id_editorial, String autor, int unidades) {
        this.titulo = titulo;
        this.ubicacion = ubicacion;
        this.id_categoria = id_categoria;
        this.id_editorial = id_editorial;
        this.autor = autor;
        this.unidades = unidades;
    }

    public Libro(int id, String titulo, String ubicacion, int id_categoria, int id_editorial, String autor, int unidades) {
        this.id = id;
        this.titulo = titulo;
        this.ubicacion = ubicacion;
        this.id_categoria = id_categoria;
        this.id_editorial = id_editorial;
        this.autor = autor;
        this.unidades = unidades;
    }

    public Libro(int id, String titulo, String ubicacion, int id_categoria, String categoria, Editorial editorialObj, String autor, int unidades) {
        this.id = id;
        this.titulo = titulo;
        this.ubicacion = ubicacion;
        this.id_categoria = id_categoria;
        this.categoria = categoria;
        this.id_editorial = editorialObj != null ? editorialObj.getId() : 0;
        this.editorialObj = editorialObj;
        this.editorial = editorialObj != null ? editorialObj.getNombre() : null;
        this.autor = autor;
        this.unidades = unidades;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String nombre) { this.titulo = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public int getId_categoria() { return id_categoria; }
    public void setId_categoria(int id_categoria) { this.id_categoria = id_categoria; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public int getUnidades() { return unidades; }
    public void setUnidades(int unidades) { this.unidades = unidades; }
    public int getId_editorial() { return id_editorial; }
    public void setId_editorial(int id_editorial) { this.id_editorial = id_editorial; }
    public Editorial getEditorialObj() { return editorialObj; }
    public void setEditorialObj(Editorial editorialObj) {
        this.editorialObj = editorialObj;
        if(editorialObj != null) {
            this.editorial = editorialObj.getNombre();
            this.id_editorial = editorialObj.getId();
        }
    }
}
