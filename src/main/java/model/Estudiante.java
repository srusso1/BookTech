package model;

public class Estudiante {
    private int id;
    private long identificacion;
    private int grado;
    private String apellido_1;
    private String apellido_2;
    private String nombre_1;
    private String nombre_2;
    private String genero;


    public Estudiante() {
    }

    public String getNombreApellido(){
        return apellido_1 + " " + nombre_1;
    }

    public String getNombreCompleto(){
        return apellido_1 + " " + apellido_2 + " " + nombre_1 + " " + nombre_2;
    }

    public String getNombreCompletoYGrado(){
        return apellido_1 + " " + apellido_2 + " " + nombre_1 + " " + nombre_2 + " - Grado: " + grado;
    }


    public Estudiante(int id, long identificacion, int grado, String apellido_1, String apellido_2, String nombre_1, String nombre_2, String genero) {
        this.id = id;
        this.identificacion = identificacion;
        this.grado = grado;
        this.apellido_1 = apellido_1;
        this.apellido_2 = apellido_2;
        this.nombre_1 = nombre_1;
        this.nombre_2 = nombre_2;
        this.genero = genero;
    }

    public Estudiante(long identificacion, int grado, String apellido_1, String apellido_2, String nombre_1, String nombre_2, String genero) {
        this.identificacion = identificacion;
        this.grado = grado;
        this.apellido_1 = apellido_1;
        this.apellido_2 = apellido_2;
        this.nombre_1 = nombre_1;
        this.nombre_2 = nombre_2;
        this.genero = genero;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(long identificacion) {
        this.identificacion = identificacion;
    }

    public int getGrado() {
        return grado;
    }

    public void setGrado(int grado) {
        this.grado = grado;
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

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
}
