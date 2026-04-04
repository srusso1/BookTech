package reports.generators;

import database.EstudiantesDAO;
import database.InformesDAO;
import com.itextpdf.layout.element.Table;
import model.Estudiante;
import model.Prestamo;
import reports.models.ReportConfig;
import utils.Alertas;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Generador para reportes de estudiantes individuales
 * Genera un PDF con el historial de préstamos de un estudiante específico
 */
public class EstudianteReportGenerator extends BaseReportGenerator {

    private final InformesDAO informesDAO;
    private final EstudiantesDAO estudiantesDAO;
    private final int idEstudiante;

    /**
     * Constructor
     * @param config Configuración del reporte
     * @param idEstudiante ID del estudiante para el que se genera el reporte
     */
    public EstudianteReportGenerator(ReportConfig config, int idEstudiante) {
        super(config, construirNombreArchivo(idEstudiante));
        this.informesDAO = new InformesDAO();
        this.estudiantesDAO = new EstudiantesDAO();
        this.idEstudiante = idEstudiante;
    }

    private static String construirNombreArchivo(int idEstudiante) {
        Estudiante est = new EstudiantesDAO().obtenerEstudiante(idEstudiante);
        if (est == null) {
            return "ESTUDIANTE_" + idEstudiante + "_INFORME.pdf";
        }

        String base = String.join("_",
                sanitizarParte(est.getApellido_1()),
                sanitizarParte(est.getApellido_2()),
                sanitizarParte(est.getNombre_1()),
                sanitizarParte(est.getNombre_2())
        ).replaceAll("_+", "_");

        return base + "_INFORME.pdf";
    }

    private static String sanitizarParte(String valor) {
        String texto = valor == null ? "" : valor.trim();
        if (texto.isEmpty()) {
            return "SIN_DATO";
        }

        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String limpio = sinAcentos
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        return limpio.isEmpty() ? "SIN_DATO" : limpio;
    }

    /**
     * Genera el reporte del estudiante
     */
    @Override
    public void generar() {
        if (!puedeGenerar()) {
            return;
        }
        try {
            // Obtener datos del estudiante
            Estudiante estudiante = estudiantesDAO.obtenerEstudiante(idEstudiante);

            if (estudiante == null) {
                Alertas.mostrarError("Estudiante no encontrado con ID: " + idEstudiante);
                return;
            }

            // Agregar encabezado
            agregarEncabezadoEstandar("Informe de Préstamos del Estudiante");

            // Agregar datos del estudiante
            agregarDatosEstudiante(estudiante);

            // Agregar resumen
            agregarResumenEstudiante();

            // Agregar tabla de préstamos (opcional)
            if (config.isIncluirTablas()) {
                agregarTablaPrestamos();
            } else {
                pdfBuilder
                        .agregarSeccion("Historial de Préstamos")
                        .agregarParrafoIndentado("No se incluyó la tabla detallada por decisión del usuario.")
                        .agregarEspacio(8);
            }

            // Finalizar
            finalizarReporte();

            Alertas.mostrarExito("Reporte generado correctamente en:\n" + rutaArchivo);

        } catch (Exception e) {
            Alertas.mostrarError("Error al generar reporte: " + e.getMessage());
        }
    }

    /**
     * Agrega la información personal del estudiante
     */
    private void agregarDatosEstudiante(Estudiante estudiante) {
        pdfBuilder
                .agregarSeccion("Datos del Estudiante")
                .agregarLineaDetalle("Nombre", estudiante.getNombreCompleto())
                .agregarLineaDetalle("Identificación", String.valueOf(estudiante.getIdentificacion()))
                .agregarLineaDetalle("Grado", String.valueOf(estudiante.getGrado()))
                .agregarLineaDetalle("Género", estudiante.getGenero())
                .agregarEspacio(15);
    }

    /**
     * Agrega un resumen de estadísticas del estudiante
     */
    private void agregarResumenEstudiante() {
        List<Prestamo> prestamos = informesDAO.obtenerHistorialEstudiante(idEstudiante);

        int totalPrestamos = prestamos.size();
        int prestamosDevueltos = (int) prestamos.stream()
                .filter(p -> p.getEstado() == ReportConfig.ESTADO_DEVUELTO)
                .count();
        int prestamosPendientes = (int) prestamos.stream()
                .filter(p -> p.getEstado() == ReportConfig.ESTADO_PENDIENTE)
                .count();
        int prestamosPrestados = (int) prestamos.stream()
                .filter(p -> p.getEstado() == ReportConfig.ESTADO_PRESTADO)
                .count();
        int prestamosRegresadosTarde = (int) prestamos.stream()
                .filter(p -> p.getDevuelto_tarde() == 1)
                .count();
        int diasTardiaAcumulados = prestamos.stream()
                .mapToInt(Prestamo::getDias_atraso)
                .sum();

        pdfBuilder
                .agregarSeccion("Resumen de Préstamos")
                .agregarLineaDetalle("Total de préstamos", String.valueOf(totalPrestamos))
                .agregarLineaDetalle("Préstamos devueltos", String.valueOf(prestamosDevueltos))
                .agregarLineaDetalle("Préstamos activos", String.valueOf(prestamosPrestados))
                .agregarLineaDetalle("Préstamos pendientes", String.valueOf(prestamosPendientes))
                .agregarLineaDetalle("Préstamos regresados tarde", String.valueOf(prestamosRegresadosTarde))
                .agregarLineaDetalle("Días de tardía acumulados", String.valueOf(diasTardiaAcumulados))
                .agregarEspacio(15);
    }

    /**
     * Agrega tabla con historial de préstamos
     */
    private void agregarTablaPrestamos() {
        List<Prestamo> prestamos = informesDAO.obtenerHistorialEstudiante(idEstudiante);

        if (prestamos.isEmpty()) {
            pdfBuilder
                    .agregarSeccion("Historial de Préstamos")
                    .agregarParrafoIndentado("No hay préstamos registrados para este estudiante.");
            return;
        }

        pdfBuilder.agregarSeccion("Historial de Préstamos");

        // Crear tabla con 8 columnas
        String[] encabezados = {"Libro", "Docente", "Motivo", "Fecha Préstamo", "Fecha Límite", "Estado", "Regresado tarde", "Días de tardanza"};
        float[] anchos = {2.3f, 1.8f, 1.4f, 1.2f, 1.2f, 1.0f, 1.1f, 1.0f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        // Agregar filas
        for (Prestamo prestamo : prestamos) {
            String estado = obtenerNombreEstado(prestamo.getEstado());
            String docente = prestamo.getDocente() != null
                    ? prestamo.getDocente().getNombreCompleto()
                    : "N/A";
            String motivo = prestamo.getMotivoPrestamo() != null
                    ? prestamo.getMotivoPrestamo().getNombre()
                    : "N/A";
            String regresadoTarde = prestamo.getEstado() == ReportConfig.ESTADO_DEVUELTO
                    ? (prestamo.getDevuelto_tarde() == 1 ? "Sí" : "No")
                    : "--";
            String diasTardia = prestamo.getEstado() == ReportConfig.ESTADO_DEVUELTO
                    ? String.valueOf(prestamo.getDias_atraso())
                    : "--";

            String[] valores = {
                    prestamo.getTituloLibro(),
                    docente,
                    motivo,
                    prestamo.getFecha_prestamo(),
                    prestamo.getFecha_limite(),
                    estado,
                    regresadoTarde,
                    diasTardia
            };

            pdfBuilder.agregarFilaTabla(tabla, valores);
        }

        pdfBuilder.agregarTabla(tabla);
    }

    /**
     * Obtiene el nombre del estado del préstamo
     */
    private String obtenerNombreEstado(int estado) {
        return switch (estado) {
            case ReportConfig.ESTADO_PRESTADO -> "Prestado";
            case ReportConfig.ESTADO_DEVUELTO -> "Devuelto";
            case ReportConfig.ESTADO_PENDIENTE -> "Pendiente";
            default -> "Desconocido";
        };
    }
}



