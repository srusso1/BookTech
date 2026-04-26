package reports.generators;

import com.itextpdf.kernel.colors.DeviceRgb;
import database.InformesDAO;
import com.itextpdf.layout.element.Table;
import model.Prestamo;
import reports.models.ReportConfig;
import utils.Alertas;
import utils.Fechas;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Generador para reportes generales de préstamos
 * Genera un PDF con el listado completo de préstamos del sistema
 */
public class GeneralReportGenerator extends BaseReportGenerator {

    private static final DeviceRgb COLOR_TOP_ESTUDIANTES = new DeviceRgb(232, 245, 233);
    private static final DeviceRgb COLOR_TOP_DOCENTES = new DeviceRgb(227, 242, 253);
    private static final DeviceRgb COLOR_TOP_LIBROS = new DeviceRgb(255, 243, 224);

    private final InformesDAO informesDAO;

    /**
     * Constructor
     * @param config Configuración del reporte
     */
    public GeneralReportGenerator(ReportConfig config) {
        super(config, "Informe_General_Prestamos.pdf");
        this.informesDAO = new InformesDAO();
    }

    /**
     * Genera el reporte general
     */
    @Override
    public void generar() {
        if (!puedeGenerar()) {
            return;
        }
        try {
            // Agregar encabezado
            agregarEncabezadoEstandar("Informe General de Préstamos");

            // Agregar descripción
            agregarDescripcionResumen();

            // Agregar tabla de préstamos (opcional)
            if (config.isIncluirTablas()) {
                agregarTablaPrestamos();
            } else {
                pdfBuilder
                        .agregarSeccion("Detalle de préstamos")
                        .agregarParrafoIndentado("No se incluyó la tabla detallada por decisión del usuario.")
                        .agregarEspacio(8);
            }

            // Finalizar
            finalizarReporte();

        } catch (Exception e) {
            Alertas.mostrarError("Error al generar reporte: " + e.getMessage());
        }
    }

    /**
     * Agrega tabla con historial de préstamos
     */
    private void agregarTablaPrestamos() {
        pdfBuilder.agregarSeccion("Tabla general a detalle");

        List<Prestamo> prestamos;
        if (config.getFechaInicio() != null && config.getFechaFin() != null) {
            prestamos = informesDAO.obtenerTodosPrestamos(
                    Fechas.convertirAISO(config.getFechaInicio()),
                    Fechas.convertirAISO(config.getFechaFin())
            );
        } else {
            prestamos = informesDAO.obtenerTodosPrestamos();
        }

        if (prestamos.isEmpty()) {
            pdfBuilder.agregarParrafo("No hay préstamos registrados en el sistema.");
            return;
        }

        // Crear tabla con 9 columnas
        String[] encabezados = {"Libro", "Estudiante", "Docente", "Fecha Préstamo", "Fecha Límite", "Fecha Devolución", "Estado", "Regresado tarde", "Días de tardanza"};
        float[] anchos = {2.3f, 2.0f, 1.8f, 1.2f, 1.2f, 1.3f, 1.0f, 1.2f, 1.0f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        // Agregar filas
        for (Prestamo prestamo : prestamos) {
            String estado = obtenerNombreEstado(prestamo.getEstado());
            String docente = prestamo.getDocente() != null
                    ? prestamo.getDocente().getNombreCompleto()
                    : "N/A";
            String fechaDevolucion = prestamo.getFecha_devolucion() != null
                    ? prestamo.getFecha_devolucion()
                    : "Pendiente";
            String regresadoTarde = prestamo.getEstado() == ReportConfig.ESTADO_DEVUELTO
                    ? (prestamo.getDevuelto_tarde() == 1 ? "Sí" : "No")
                    : "--";
            String diasTardia = prestamo.getEstado() == ReportConfig.ESTADO_DEVUELTO
                    ? String.valueOf(prestamo.getDias_atraso())
                    : "--";

            String[] valores = {
                    prestamo.getTituloLibro(),
                    prestamo.getEstudiante(),
                    docente,
                    prestamo.getFecha_prestamo(),
                    prestamo.getFecha_limite(),
                    fechaDevolucion,
                    estado,
                    regresadoTarde,
                    diasTardia
            };

            pdfBuilder.agregarFilaTabla(tabla, valores);
        }

        pdfBuilder.agregarTabla(tabla);
    }

    private void agregarDescripcionResumen() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;
        boolean hayFiltro = fechaInicio != null && fechaFin != null;

        Map<String, Object> resumen = informesDAO.obtenerResumenGeneral(fechaInicio, fechaFin);

        String filtroTexto = hayFiltro
                ? fechaInicio + " - " + fechaFin
                : "No aplicado";

        pdfBuilder
                .agregarSeccion("Descripción del Informe")
                .agregarParrafoIndentado("Este informe contiene el listado completo de los préstamos registrados en el sistema.")
                .agregarLineaDetalle("Fecha de generación", String.valueOf(LocalDate.now()))
                .agregarLineaDetalle("Filtro de fecha aplicado", filtroTexto)
                .agregarSeccion("Indicadores Principales")
                .agregarLineaDetalle("Total de préstamos", String.valueOf(resumen.get("totalPrestamos")))
                .agregarLineaDetalle("Total de préstamos regresados tarde", String.valueOf(resumen.get("totalPrestamosTarde")))
                .agregarEspacio(8);

        agregarTopEstudiantes(fechaInicio, fechaFin);
        agregarTopDocentes(fechaInicio, fechaFin);
        agregarTopLibros(fechaInicio, fechaFin);
        pdfBuilder.agregarEspacio(15);
    }

    private void agregarTopEstudiantes(String fechaInicio, String fechaFin) {
        pdfBuilder.agregarSeccion("Top 5 estudiantes que más prestaron libros");
        List<Map<String, Object>> top = informesDAO.obtenerTopEstudiantesDetalle(fechaInicio, fechaFin, 5);

        if (top.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("Sin datos");
            return;
        }

        String[] headers = {"Estudiante", "Préstamos", "Devueltos tarde", "Motivo frecuente"};
        Table tabla = pdfBuilder.crearTabla(4, headers, COLOR_TOP_ESTUDIANTES);
        for (Map<String, Object> fila : top) {
            String[] valores = {
                    String.valueOf(fila.get("estudiante")),
                    String.valueOf(fila.get("total_prestamos")),
                    String.valueOf(fila.get("devoluciones_tarde")),
                    valorSeguro(fila.get("motivo_frecuente"))
            };
            pdfBuilder.agregarFilaTabla(tabla, valores);
        }
        pdfBuilder.agregarTabla(tabla).agregarEspacio(8);
    }

    private void agregarTopDocentes(String fechaInicio, String fechaFin) {
        pdfBuilder.agregarSeccion("Top 5 docentes que enviaron a más estudiantes");
        List<Map<String, Object>> top = informesDAO.obtenerTopDocentesDetalle(fechaInicio, fechaFin, 5);

        if (top.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("Sin datos");
            return;
        }

        String[] headers = {"Docente", "Estudiantes enviados", "Total solicitudes de préstamo", "Motivo frecuente"};
        Table tabla = pdfBuilder.crearTabla(4, headers, COLOR_TOP_DOCENTES);
        for (Map<String, Object> fila : top) {
            String[] valores = {
                    String.valueOf(fila.get("docente")),
                    String.valueOf(fila.get("estudiantes_enviados")),
                    String.valueOf(fila.get("total_solicitudes")),
                    valorSeguro(fila.get("motivo_frecuente"))
            };
            pdfBuilder.agregarFilaTabla(tabla, valores);
        }
        pdfBuilder.agregarTabla(tabla).agregarEspacio(8);
    }

    private void agregarTopLibros(String fechaInicio, String fechaFin) {
        pdfBuilder.agregarSeccion("Top 5 libros más solicitados");
        List<Map<String, Object>> top = informesDAO.obtenerTopLibrosDetalle(fechaInicio, fechaFin, 5);

        if (top.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("Sin datos");
            return;
        }

        String[] headers = {"Libro", "Solicitudes", "Estudiantes únicos", "Grado más solicitante"};
        Table tabla = pdfBuilder.crearTabla(4, headers, COLOR_TOP_LIBROS);
        for (Map<String, Object> fila : top) {
            String[] valores = {
                    String.valueOf(fila.get("libro")),
                    String.valueOf(fila.get("total_solicitudes")),
                    String.valueOf(fila.get("estudiantes_unicos")),
                    valorSeguro(fila.get("grado_frecuente"))
            };
            pdfBuilder.agregarFilaTabla(tabla, valores);
        }
        pdfBuilder.agregarTabla(tabla).agregarEspacio(8);
    }

    private String valorSeguro(Object valor) {
        String texto = valor == null ? "" : String.valueOf(valor).trim();
        return texto.isEmpty() ? "Sin datos" : texto;
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

