package model;

public class RegistroPlataforma {
    private int id;
    private int id_docente;
    private int id_motivo_uso;

    public RegistroPlataforma(int id, int id_docente, int id_motivo_uso) {
        this.id = id;
        this.id_docente = id_docente;
        this.id_motivo_uso = id_motivo_uso;
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

    public int getId_motivo_uso() {
        return id_motivo_uso;
    }

    public void setId_motivo_uso(int id_motivo_uso) {
        this.id_motivo_uso = id_motivo_uso;
    }
}
