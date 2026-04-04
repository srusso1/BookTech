package reports.generators;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.element.Table;
import database.InformesDAO;
import database.RegistroPlataformaDAO;
import model.RegistroPlataformaDetalle;
import reports.models.ReportConfig;
import utils.Alertas;
import utils.Fechas;

import java.util.List;
import java.util.Map;

public class GeneralPlataformaReportGenerator extends BaseReportGenerator {

    private static final DeviceRgb COLOR_TOP_DOCENTES = new DeviceRgb(227, 242, 253);
    private static final DeviceRgb COLOR_TOP_GRADOS = new DeviceRgb(232, 245, 233);
    private static final DeviceRgb COLOR_TOP_MOTIVOS = new DeviceRgb(255, 243, 224);

    private final InformesDAO informesDAO;
    private final RegistroPlataformaDAO registroPlataformaDAO;

    public GeneralPlataformaReportGenerator(ReportConfig config) {
        super(config, "Informe_General_Plataforma_Virtual.pdf");
        this.informesDAO = new InformesDAO();
        this.registroPlataformaDAO = new RegistroPlataformaDAO();
    }

    @Override
    public void generar() {
        if (!puedeGenerar()) {
            return;
        }

        try {
            agregarEncabezadoEstandar("Informe General de Uso de Plataforma Virtual");
            agregarDescripcionResumen();

            agregarTopDocentes();
            agregarTopGrados();
            agregarTopMotivos();

            if (config.isIncluirTablas()) {
                agregarTablaDetalle();
            } else {
                pdfBuilder
                        .agregarSeccion("Detalle de uso de plataforma")
                        .agregarParrafoIndentado("No se incluyó la tabla detallada por decisión del usuario.")
                        .agregarEspacio(8);
            }

            finalizarReporte();
            Alertas.mostrarExito("Reporte generado correctamente en:\n" + rutaArchivo);
        } catch (Exception e) {
            Alertas.mostrarError("Error al generar reporte: " + e.getMessage());
        }
    }

    private void agregarDescripcionResumen() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;
        String filtroTexto = (fechaInicio != null && fechaFin != null) ? fechaInicio + " - " + fechaFin : "No aplicado";

        Map<String, Object> resumen = informesDAO.obtenerResumenPlataformaGeneral(fechaInicio, fechaFin);
        int totalMinutos = ((Number) resumen.getOrDefault("totalMinutos", 0)).intValue();

        pdfBuilder
                .agregarSeccion("Descripción del Informe")
                .agregarParrafoIndentado("Este informe presenta el uso general de la plataforma virtual registrado por los docentes.")
                .agregarLineaDetalle("Filtro de fecha aplicado", filtroTexto)
                .agregarSeccion("Indicadores Principales")
                .agregarLineaDetalle("Total de registros", String.valueOf(resumen.getOrDefault("totalRegistros", 0)))
                .agregarLineaDetalle("Tiempo acumulado", formatearMinutos(totalMinutos))
                .agregarEspacio(12);
    }

    private void agregarTopDocentes() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;
        Map<String, Integer> datos = (fechaInicio != null && fechaFin != null)
                ? registroPlataformaDAO.obtenerTopDocentesUsoPlataforma(fechaInicio, fechaFin, 5)
                : registroPlataformaDAO.obtenerTopDocentesUsoPlataforma(5);

        pdfBuilder.agregarSeccion("Top 5 docentes que más uso hacen de la plataforma");

        if (datos.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("Sin datos").agregarEspacio(8);
            return;
        }

        String[] headers = {"Docente", "Tiempo acumulado"};
        float[] anchos = {3.8f, 1.4f};
        Table tabla = pdfBuilder.crearTabla(anchos, headers, COLOR_TOP_DOCENTES);

        for (Map.Entry<String, Integer> entry : datos.entrySet()) {
            String[] fila = {
                    valorSeguro(entry.getKey()),
                    formatearMinutos(entry.getValue())
            };
            pdfBuilder.agregarFilaTabla(tabla, fila);
        }

        pdfBuilder.agregarTabla(tabla).agregarEspacio(8);
    }

    private void agregarTopGrados() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;
        Map<String, Integer> datos = (fechaInicio != null && fechaFin != null)
                ? registroPlataformaDAO.obtenerTopGradosUsoPlataforma(fechaInicio, fechaFin, 5)
                : registroPlataformaDAO.obtenerTopGradosUsoPlataforma(5);

        pdfBuilder.agregarSeccion("Top 5 grados con más uso de la plataforma");

        if (datos.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("Sin datos").agregarEspacio(8);
            return;
        }

        String[] headers = {"Grado", "Tiempo acumulado"};
        float[] anchos = {1.5f, 1.8f};
        Table tabla = pdfBuilder.crearTabla(anchos, headers, COLOR_TOP_GRADOS);

        for (Map.Entry<String, Integer> entry : datos.entrySet()) {
            String[] fila = {
                    valorSeguro(entry.getKey()),
                    formatearMinutos(entry.getValue())
            };
            pdfBuilder.agregarFilaTabla(tabla, fila);
        }

        pdfBuilder.agregarTabla(tabla).agregarEspacio(8);
    }

    private void agregarTopMotivos() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;
        List<Map<String, Object>> datos = (fechaInicio != null && fechaFin != null)
                ? registroPlataformaDAO.obtenerTopMotivosUsoPlataformaConTiempo(fechaInicio, fechaFin, 5)
                : registroPlataformaDAO.obtenerTopMotivosUsoPlataformaConTiempo(5);

        pdfBuilder.agregarSeccion("Top 5 motivos de uso más frecuentes");

        if (datos.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("Sin datos").agregarEspacio(8);
            return;
        }

        String[] headers = {"Motivo de uso", "Cantidad", "Tiempo acumulado"};
        float[] anchos = {3.2f, 0.8f, 1.4f};
        Table tabla = pdfBuilder.crearTabla(anchos, headers, COLOR_TOP_MOTIVOS);

        for (Map<String, Object> filaDato : datos) {
            String[] fila = {
                    valorSeguro(String.valueOf(filaDato.get("motivo"))),
                    String.valueOf(filaDato.get("total")),
                    formatearMinutos(((Number) filaDato.getOrDefault("minutos", 0)).intValue())
            };
            pdfBuilder.agregarFilaTabla(tabla, fila);
        }

        pdfBuilder.agregarTabla(tabla).agregarEspacio(8);
    }

    private void agregarTablaDetalle() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;

        List<RegistroPlataformaDetalle> datos = informesDAO.obtenerRegistrosPlataforma(fechaInicio, fechaFin);
        pdfBuilder.agregarSeccion("Tabla general a detalle");

        if (datos.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No se encontraron registros de uso de plataforma con los filtros aplicados.");
            return;
        }

        String[] encabezados = {"Docente", "Motivo de uso", "Fecha", "Hora inicio", "Hora fin", "Duración", "Grado"};
        float[] anchos = {2.4f, 2.2f, 1.2f, 1.0f, 1.0f, 1.1f, 0.8f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (RegistroPlataformaDetalle registro : datos) {
            String[] fila = {
                    valorSeguro(registro.getDocente()),
                    valorSeguro(registro.getMotivoUso()),
                    valorSeguro(registro.getFecha()),
                    valorSeguro(registro.getHoraInicio()),
                    valorSeguro(registro.getHoraFin()),
                    formatearMinutos(registro.getTotalMinutos()),
                    registro.getGrado() > 0 ? String.valueOf(registro.getGrado()) : "--"
            };
            pdfBuilder.agregarFilaTabla(tabla, fila);
        }

        pdfBuilder.agregarTabla(tabla);
    }

    private String formatearMinutos(int minutos) {
        int horas = minutos / 60;
        int restantes = minutos % 60;
        return horas + "h " + restantes + "m";
    }

    private String valorSeguro(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "--";
        }
        return valor;
    }
}

