package model;

public class Categoria {
    private int id;
    private String nombre_categoria;

    public Categoria(int id, String nombre_categoria) {
        this.id = id;
        this.nombre_categoria = nombre_categoria;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreCategoria() {
        return nombre_categoria;
    }

    public void setNombreCategoria(String nombre_categoria) {
        this.nombre_categoria = nombre_categoria;
    }

    @Override
    public String toString() {
        return nombre_categoria;
    }
}
