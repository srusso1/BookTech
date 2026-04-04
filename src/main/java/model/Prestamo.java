package model;

public class Prestamo {
    private int id;
    private int id_libro;
    private String estudiante;
    private String fecha_prestamo;
    private String fecha_limite;
    private String fecha_devolucion;
    private int devuelto_tarde;
    private int dias_atraso;
    private int estado;
    private int grado;
    private String tituloLibro;
    private Docente docente;
    private MotivoPrestamo motivoPrestamo;

    public Prestamo(int estado, String fecha_limite, String fecha_prestamo, String estudiante, int id_libro, int id, int grado) {
        this.estado = estado;
        this.fecha_limite = fecha_limite;
        this.fecha_prestamo = fecha_prestamo;
        this.estudiante = estudiante;
        this.id_libro = id_libro;
        this.id = id;
        this.grado = grado;
    }

    public Prestamo(int id, String tituloLibro, int grado, int estado, String fecha_limite, String fecha_prestamo, String estudiante, int id_libro) {
        this.id = id;
        this.tituloLibro = tituloLibro;
        this.grado = grado;
        this.estado = estado;
        this.fecha_limite = fecha_limite;
        this.fecha_prestamo = fecha_prestamo;
        this.estudiante = estudiante;
        this.id_libro = id_libro;
    }

    public Prestamo(int id, int id_libro, String estudiante, String fecha_prestamo, String fecha_limite, int estado, int grado, String tituloLibro, Docente docente, MotivoPrestamo motivoPrestamo) {
        this.id = id;
        this.id_libro = id_libro;
        this.estudiante = estudiante;
        this.fecha_prestamo = fecha_prestamo;
        this.fecha_limite = fecha_limite;
        this.estado = estado;
        this.grado = grado;
        this.tituloLibro = tituloLibro;
        this.docente = docente;
        this.motivoPrestamo = motivoPrestamo;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public MotivoPrestamo getMotivoPrestamo() {
        return motivoPrestamo;
    }

    public void setMotivoPrestamo(MotivoPrestamo motivoPrestamo) {
        this.motivoPrestamo = motivoPrestamo;
    }

    public int getGrado() {
        return grado;
    }

    public void setGrado(int grado) {
        this.grado = grado;
    }

    public int getId() {
        return id;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_libro() {
        return id_libro;
    }

    public void setId_libro(int id_libro) {
        this.id_libro = id_libro;
    }

    public String getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(String estudiante) {
        this.estudiante = estudiante;
    }

    public String getFecha_prestamo() {
        return fecha_prestamo;
    }

    public void setFecha_prestamo(String fecha_prestamo) {
        this.fecha_prestamo = fecha_prestamo;
    }

    public String getFecha_limite() {
        return fecha_limite;
    }

    public void setFecha_limite(String fecha_devolucion) {
        this.fecha_limite = fecha_devolucion;
    }

    public String getFecha_devolucion() {
        return fecha_devolucion;
    }

    public void setFecha_devolucion(String fecha_devolucion) {
        this.fecha_devolucion = fecha_devolucion;
    }

    public int getDevuelto_tarde() {
        return devuelto_tarde;
    }

    public void setDevuelto_tarde(int devuelto_tarde) {
        this.devuelto_tarde = devuelto_tarde;
    }

    public int getDias_atraso() {
        return dias_atraso;
    }

    public void setDias_atraso(int dias_atraso) {
        this.dias_atraso = dias_atraso;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}
