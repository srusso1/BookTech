package model;

/**
 * Representa una notificación o alerta de vencimiento de préstamo.
 */
public class AlertaPrestamo {

    public enum TipoAlerta {
        VENCIDO,
        POR_VENCER_HOY,
        PROXIMO_A_VENCER
    }

    private final int idPrestamo;
    private final String libroTitulo;
    private final String estudiante;
    private final int grado;
    private final String fechaLimite;
    private final TipoAlerta tipo;
    private final int diasDiferencia;

    public AlertaPrestamo(int idPrestamo, String libroTitulo, String estudiante, int grado, String fechaLimite, TipoAlerta tipo, int diasDiferencia) {
        this.idPrestamo = idPrestamo;
        this.libroTitulo = libroTitulo;
        this.estudiante = estudiante;
        this.grado = grado;
        this.fechaLimite = fechaLimite;
        this.tipo = tipo;
        this.diasDiferencia = diasDiferencia;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public String getLibroTitulo() {
        return libroTitulo;
    }

    public String getEstudiante() {
        return estudiante;
    }

    public int getGrado() {
        return grado;
    }

    public String getFechaLimite() {
        return fechaLimite;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public int getDiasDiferencia() {
        return diasDiferencia;
    }

    public String getDescripcion() {
        return switch (tipo) {
            case VENCIDO -> "¡VENCIDO hace " + Math.abs(diasDiferencia) + " día(s)! (" + fechaLimite + ") • " + estudiante + " (Grado " + grado + ") • \"" + libroTitulo + "\"";
            case POR_VENCER_HOY -> "¡Vence HOY (" + fechaLimite + ")! • " + estudiante + " (Grado " + grado + ") • \"" + libroTitulo + "\"";
            case PROXIMO_A_VENCER -> "Vence en " + diasDiferencia + " día(s) (" + fechaLimite + ") • " + estudiante + " (Grado " + grado + ") • \"" + libroTitulo + "\"";
        };
    }
}
