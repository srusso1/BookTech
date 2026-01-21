package model;

public class Bibliotecario extends Usuario {
    public Bibliotecario(String nombre, String apellido, String usuario, String password) {
        super(nombre, apellido, usuario, password);
        rol = 0;
    }
}
