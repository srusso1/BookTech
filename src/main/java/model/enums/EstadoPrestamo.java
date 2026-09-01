package model.enums;

public enum EstadoPrestamo {
    PRESTADO(0, "Prestado"),
    DEVUELTO(1, "Devuelto"),
    VENCIDO(2, "Vencido");

    private final int id;
    private final String descripcion;

    EstadoPrestamo(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static EstadoPrestamo fromId(int id) {
        for (EstadoPrestamo estado : values()) {
            if (estado.getId() == id) {
                return estado;
            }
        }
        throw new IllegalArgumentException("ID de EstadoPrestamo no válido: " + id);
    }
}
