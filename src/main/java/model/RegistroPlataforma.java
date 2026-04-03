package model;

public class RegistroPlataforma {
    private int id;
    private int id_docente;
    private String motivo_uso;

    public RegistroPlataforma(int id, int id_docente, String motivo_uso) {
        this.id = id;
        this.id_docente = id_docente;
        this.motivo_uso = motivo_uso;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_docente() {
        return id_docente;
    }

    public void setId_docente(int id_docente) {
        this.id_docente = id_docente;
    }

    public String getMotivo_uso() {
        return motivo_uso;
    }

    public void setMotivo_uso(String motivo_uso) {
        this.motivo_uso = motivo_uso;
    }
}
