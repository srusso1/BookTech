package reports.generators;

import com.itextpdf.layout.element.Table;
import database.InformesDAO;
import model.Prestamo;
import reports.models.ReportConfig;
import utils.Alertas;
import utils.Fechas;

import java.util.List;

/**
 * Generador de informe de préstamos devueltos tarde
 */
public class PrestamosDevueltosTardeReportGenerator extends BaseReportGenerator {

    private final InformesDAO informesDAO;
    private final Integer gradoFiltro;

    public PrestamosDevueltosTardeReportGenerator(ReportConfig config, Integer gradoFiltro) {
        super(config, construirNombreArchivo(gradoFiltro));
        this.informesDAO = new InformesDAO();
        this.gradoFiltro = gradoFiltro;
    }

    private static String construirNombreArchivo(Integer gradoFiltro) {
        if (gradoFiltro == null) {
            return "Reporte_Prestamos_Devueltos_Tarde.pdf";
        }
        return "Reporte_Prestamos_Devueltos_Tarde_Grado_" + gradoFiltro + ".pdf";
    }

    @Override
    public void generar() {
        if (!puedeGenerar()) {
            return;
        }

        try {
            agregarEncabezadoEstandar("Reporte Préstamos Devueltos Tarde");
            agregarDescripcion();
            if (config.isIncluirTablas()) {
                agregarTabla();
            } else {
                pdfBuilder
                        .agregarSeccion("Detalle de préstamos devueltos tarde")
                        .agregarParrafoIndentado("No se incluyó la tabla detallada por decisión del usuario.")
                        .agregarEspacio(8);
            }
            finalizarReporte();
        } catch (Exception e) {
            Alertas.mostrarError("Error al generar reporte: " + e.getMessage());
        }
    }

    private void agregarDescripcion() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;
        boolean hayFiltroFecha = fechaInicio != null && fechaFin != null;

        String textoFiltroFecha = hayFiltroFecha ? fechaInicio + " - " + fechaFin : "No aplicado";
        String textoFiltroGrado = gradoFiltro == null ? "Todos los grados" : "Grado " + gradoFiltro;
        Integer gradoTop = gradoFiltro == null
                ? informesDAO.obtenerGradoConMasPrestamosDevueltosTarde(fechaInicio, fechaFin)
                : null;

        List<Prestamo> datos = informesDAO.obtenerPrestamosDevueltosTarde(fechaInicio, fechaFin, gradoFiltro);
        int total = datos.size();
        int diasTardiaAcumulados = datos.stream().mapToInt(Prestamo::getDias_atraso).sum();

        pdfBuilder
                .agregarSeccion("Criterios del reporte")
                .agregarLineaDetalle("Filtro de fecha", textoFiltroFecha)
                .agregarLineaDetalle("Filtro de grado", textoFiltroGrado)
                .agregarSeccion("Resumen")
                .agregarLineaDetalle("Total de préstamos devueltos tarde", String.valueOf(total))
                .agregarLineaDetalle("Días de tardanza acumulados", String.valueOf(diasTardiaAcumulados));

        if (gradoFiltro == null) {
            pdfBuilder.agregarLineaDetalle(
                    "Grado con más préstamos devueltos tarde",
                    gradoTop != null ? String.valueOf(gradoTop) : "Sin datos"
            );
        }

        pdfBuilder.agregarEspacio(12);
    }

    private void agregarTabla() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;
        List<Prestamo> datos = informesDAO.obtenerPrestamosDevueltosTarde(fechaInicio, fechaFin, gradoFiltro);

        if (datos.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No se encontraron préstamos devueltos tarde con los filtros aplicados.");
            return;
        }

        pdfBuilder.agregarSeccion("Detalle de préstamos devueltos tarde");

        String[] encabezados = {
                "Libro", "Estudiante", "Grado", "Docente", "Fecha Préstamo", "Fecha Límite", "Fecha Devolución", "Días de tardanza"
        };
        float[] anchos = {2.3f, 2.0f, 0.9f, 1.8f, 1.2f, 1.2f, 1.3f, 1.0f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (Prestamo p : datos) {
            String docente = p.getDocente() != null ? p.getDocente().getNombreCompleto() : "N/A";

            String[] fila = {
                    p.getTituloLibro(),
                    p.getEstudiante(),
                    String.valueOf(p.getGrado()),
                    docente,
                    p.getFecha_prestamo(),
                    p.getFecha_limite(),
                    p.getFecha_devolucion() != null ? p.getFecha_devolucion() : "--",
                    String.valueOf(p.getDias_atraso())
            };

            pdfBuilder.agregarFilaTabla(tabla, fila);
        }

        pdfBuilder.agregarTabla(tabla);
    }
}



