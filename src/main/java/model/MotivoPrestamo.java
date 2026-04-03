package model;

public class MotivoPrestamo {
    private int id;
    private String nombre;

    public MotivoPrestamo(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }

    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}