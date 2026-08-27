package reports.generators;

import com.itextpdf.kernel.colors.DeviceRgb;
import database.InformesDAO;
import com.itextpdf.layout.element.Table;
import model.Prestamo;
import reports.models.ReportConfig;
import utils.Fechas;

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
    public void generar() throws Exception {
        if (!puedeGenerar()) {
            return;
        }

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
            pdfBuilder.agregarParrafoIndentado("No hay préstamos registrados en el sistema.");
            return;
        }

        String[] encabezados = {"Libro", "Estudiante", "Docente", "Motivo", "Fecha Préstamo", "Fecha Límite", "Estado", "Regresado tarde", "Días tardanza"};
        float[] anchos = {2.2f, 2.0f, 1.8f, 1.3f, 1.1f, 1.1f, 1.0f, 1.1f, 0.9f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

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
                    prestamo.getEstudiante(),
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

    @SuppressWarnings("unchecked")
    private void agregarDescripcionResumen() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;

        Map<String, Object> resumen = informesDAO.obtenerResumenGeneral(fechaInicio, fechaFin);
        String filtroTexto = (fechaInicio != null && fechaFin != null) ? fechaInicio + " - " + fechaFin : "No aplicado";

        pdfBuilder
                .agregarSeccion("Descripción del Informe")
                .agregarParrafoIndentado("Este informe presenta el consolidado general de los préstamos registrados en el sistema.")
                .agregarLineaDetalle("Filtro de fecha aplicado", filtroTexto)
                .agregarSeccion("Estadísticas Generales")
                .agregarLineaDetalle("Total de préstamos registrados", String.valueOf(resumen.getOrDefault("totalPrestamos", 0)))
                .agregarLineaDetalle("Préstamos devueltos tarde", String.valueOf(resumen.getOrDefault("totalPrestamosTarde", 0)))
                .agregarLineaDetalle("Estudiante destacado", String.valueOf(resumen.getOrDefault("estudianteTop", "Sin datos")))
                .agregarLineaDetalle("Docente con más estudiantes", String.valueOf(resumen.getOrDefault("docenteTop", "Sin datos")))
                .agregarLineaDetalle("Libro más prestado", String.valueOf(resumen.getOrDefault("libroTop", "Sin datos")))
                .agregarEspacio(8);

        List<String> topEstudiantes = (List<String>) resumen.getOrDefault("top5Estudiantes", List.of());
        agregarListaRanking("Top 5 Estudiantes con Más Préstamos", topEstudiantes, COLOR_TOP_ESTUDIANTES);

        List<String> topDocentes = (List<String>) resumen.getOrDefault("top5Docentes", List.of());
        agregarListaRanking("Top 5 Docentes con Más Préstamos Asociados", topDocentes, COLOR_TOP_DOCENTES);

        List<String> topLibros = (List<String>) resumen.getOrDefault("top5Libros", List.of());
        agregarListaRanking("Top 5 Libros Más Prestados", topLibros, COLOR_TOP_LIBROS);
    }

    private void agregarListaRanking(String titulo, List<String> items, DeviceRgb colorEncabezado) {
        pdfBuilder.agregarSeccion(titulo);
        if (items == null || items.isEmpty() || (items.size() == 1 && "Sin datos".equals(items.get(0)))) {
            pdfBuilder.agregarParrafoIndentado("No hay datos para este ranking.");
            return;
        }

        Table tabla = pdfBuilder.crearTabla(new float[]{1.0f, 6.0f}, new String[]{"#", "Detalle"}, colorEncabezado);
        int pos = 1;
        for (String item : items) {
            String texto = item.contains(". ") ? item.substring(item.indexOf(". ") + 2) : item;
            tabla.addCell(String.valueOf(pos++));
            tabla.addCell(texto);
        }
        pdfBuilder.agregarTabla(tabla);
        pdfBuilder.agregarEspacio(6);
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
