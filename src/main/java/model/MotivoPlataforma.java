package model;

public class MotivoPlataforma {
    private final int id;
    private final String nombre;
    private int estado;

    public MotivoPlataforma(int id, String nombre) {
        this(id, nombre, 1);
    }

    public MotivoPlataforma(int id, String nombre, int estado) {
        this.id = id;
        this.nombre = nombre;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

