package reports.models;

import java.time.LocalDate;

/**
 * Clase de configuración para reportes
 * Contiene parámetros comunes reutilizables
 */
public class ReportConfig {

    // Configuración institucional
    public static final String INSTITUCION = "INSTITUCION EDUCATIVA TRUJILLO";
    public static final String ESCUELA = "Becerril - Cesar";
    public static final String CIUDAD_REPORTE = "Becerril";
    public static final String ESCUDO_REPORTE = "/images/escudo.png";

    // Rutas de salida
    public static final String RUTA_REPORTES = "reportes/";
    public static final String FORMATO_FECHA = "yyyy-MM-dd";

    // Estados de préstamo (alineados con negocio)
    public static final int ESTADO_PRESTADO = 0;
    public static final int ESTADO_DEVUELTO = 1;
    public static final int ESTADO_PENDIENTE = 2;

    // Alias de compatibilidad con nomenclatura anterior
    public static final int ESTADO_ACTIVO = ESTADO_PRESTADO;
    public static final int ESTADO_ATRASADO = ESTADO_PENDIENTE;

    // Parámetros de reportes
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipoReporte;
    private int idEstudiante;
    private int grado;
    private boolean incluirTablas = true;

    // Constructor vacío
    public ReportConfig() {
    }

    // Constructor con parámetros comunes
    public ReportConfig(LocalDate fechaInicio, LocalDate fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    // Getters y Setters
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(String tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getGrado() {
        return grado;
    }

    public void setGrado(int grado) {
        this.grado = grado;
    }

    public boolean isIncluirTablas() {
        return incluirTablas;
    }

    public void setIncluirTablas(boolean incluirTablas) {
        this.incluirTablas = incluirTablas;
    }

    /**
     * Obtiene la ruta completa para guardar un reporte
     */
    public String getRutaArchivoReporte(String nombreArchivo) {
        return RUTA_REPORTES + nombreArchivo;
    }
}

