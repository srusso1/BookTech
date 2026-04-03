package model;

public class Docente {
    private int id;
    private String nombre_1;
    private String nombre_2;
    private String apellido_1;
    private String apellido_2;

    public Docente(int id, String nombre_1, String nombre_2, String apellido_1, String apellido_2) {
        this.id = id;
        this.nombre_1 = nombre_1;
        this.nombre_2 = nombre_2;
        this.apellido_1 = apellido_1;
        this.apellido_2 = apellido_2;
    }

    public String getNombreApellido(){
        return nombre_1 + " " + apellido_1;
    }

    public String getNombreCompleto(){
        return nombre_1 + " " + nombre_2 + " " + apellido_1 + " " + apellido_2;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre_1() {
        return nombre_1;
    }

    public void setNombre_1(String nombre_1) {
        this.nombre_1 = nombre_1;
    }

    public String getNombre_2() {
        return nombre_2;
    }

    public void setNombre_2(String nombre_2) {
        this.nombre_2 = nombre_2;
    }

    public String getApellido_1() {
        return apellido_1;
    }

    public void setApellido_1(String apellido_1) {
        this.apellido_1 = apellido_1;
    }

    public String getApellido_2() {
        return apellido_2;
    }

    public void setApellido_2(String apellido_2) {
        this.apellido_2 = apellido_2;
    }
}
