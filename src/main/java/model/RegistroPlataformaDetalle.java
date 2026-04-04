package model;

public class RegistroPlataformaDetalle {
    private final int id;
    private final int idDocente;
    private final String docente;
    private final String motivoUso;
    private final String fecha;
    private final String horaInicio;
    private final String horaFin;
    private final int totalMinutos;
    private final int grado;

    public RegistroPlataformaDetalle(int id, int idDocente, String docente, String motivoUso, String fecha,
                                     String horaInicio, String horaFin, int totalMinutos, int grado) {
        this.id = id;
        this.idDocente = idDocente;
        this.docente = docente;
        this.motivoUso = motivoUso;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.totalMinutos = totalMinutos;
        this.grado = grado;
    }

    public int getId() {
        return id;
    }

    public int getIdDocente() {
        return idDocente;
    }

    public String getDocente() {
        return docente;
    }

    public String getMotivoUso() {
        return motivoUso;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public int getTotalMinutos() {
        return totalMinutos;
    }

    public int getGrado() {
        return grado;
    }
}

