package model.enums;

public enum RolUsuario {
    BIBLIOTECARIO(0, "Bibliotecario"),
    RECTOR(1, "Rector");

    private final int id;
    private final String descripcion;

    RolUsuario(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static RolUsuario fromId(int id) {
        for (RolUsuario rol : values()) {
            if (rol.getId() == id) {
                return rol;
            }
        }
        throw new IllegalArgumentException("ID de RolUsuario no válido: " + id);
    }
}
