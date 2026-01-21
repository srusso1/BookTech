package model;

public class Rector extends Usuario {
    public Rector(String nombre, String apellido, String usuario, String password) {
        super(nombre, apellido, usuario, password);
        rol = 1;
    }
}
