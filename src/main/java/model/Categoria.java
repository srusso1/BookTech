package model;

public class Categoria {
    private int id;
    private String nombre_categoria;
    private int estado;

    public Categoria(int id, String nombre_categoria) {
        this.id = id;
        this.nombre_categoria = nombre_categoria;
        this.estado = 1;
    }

    public Categoria(int id, String nombre_categoria, int estado) {
        this.id = id;
        this.nombre_categoria = nombre_categoria;
        this.estado = estado;
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

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombre_categoria;
    }
}
