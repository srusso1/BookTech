package reports.generators;

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

            // Agregar tabla de préstamos
            agregarTablaPrestamos();

            // Finalizar
            finalizarReporte();

            Alertas.mostrarExito("Reporte generado correctamente en:\n" + rutaArchivo);

        } catch (Exception e) {
            Alertas.mostrarError("Error al generar reporte: " + e.getMessage());
        }
    }

    /**
     * Agrega tabla con historial de préstamos
     */
    private void agregarTablaPrestamos() {
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
        Table tabla = pdfBuilder.crearTabla(9, encabezados);

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
                .agregarLineaDetalle("Estudiante con más préstamos", String.valueOf(resumen.get("estudianteTop")))
                .agregarLineaDetalle("Docente que envió a más estudiantes", String.valueOf(resumen.get("docenteTop")))
                .agregarLineaDetalle("Libro más solicitado", String.valueOf(resumen.get("libroTop")))
                .agregarEspacio(15);
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

