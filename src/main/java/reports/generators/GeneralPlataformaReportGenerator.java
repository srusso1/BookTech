package reports.generators;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.element.Table;
import database.InformesDAO;
import database.RegistroPlataformaDAO;
import model.RegistroPlataformaDetalle;
import reports.models.ReportConfig;
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
    public void generar() throws Exception {
        if (!puedeGenerar()) {
            return;
        }

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
    }

    private void agregarDescripcionResumen() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;
        String filtroTexto = (fechaInicio != null && fechaFin != null) ? fechaInicio + " - " + fechaFin : "No aplicado";

        Map<String, Object> resumen = informesDAO.obtenerResumenPlataformaGeneral(fechaInicio, fechaFin);
        int totalMinutos = ((Number) resumen.getOrDefault("totalMinutos", 0)).intValue();

        pdfBuilder
                .agregarSeccion("Descripción del Informe")
                .agregarParrafoIndentado("Este informe presenta el consolidado general de uso de la plataforma virtual.")
                .agregarLineaDetalle("Filtro de fecha aplicado", filtroTexto)
                .agregarSeccion("Resumen General")
                .agregarLineaDetalle("Total de registros de uso", String.valueOf(resumen.getOrDefault("totalRegistros", 0)))
                .agregarLineaDetalle("Docentes distintos con registro", String.valueOf(resumen.getOrDefault("docentesDistintos", 0)))
                .agregarLineaDetalle("Tiempo total acumulado", formatearMinutos(totalMinutos))
                .agregarLineaDetalle("Motivo más frecuente", String.valueOf(resumen.getOrDefault("motivoMasFrecuente", "Sin datos")))
                .agregarLineaDetalle("Grado más atendido", String.valueOf(resumen.getOrDefault("gradoMasFrecuente", "Sin datos")))
                .agregarEspacio(10);
    }

    private void agregarTopDocentes() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;

        Map<String, Integer> topDocentes = (fechaInicio != null && fechaFin != null)
                ? registroPlataformaDAO.obtenerTopDocentesUsoPlataforma(fechaInicio, fechaFin, 5)
                : registroPlataformaDAO.obtenerTopDocentesUsoPlataforma(5);

        pdfBuilder.agregarSeccion("Top 5 Docentes por Tiempo de Uso");

        if (topDocentes.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No hay datos para este ranking.");
            return;
        }

        Table tabla = pdfBuilder.crearTabla(new float[]{1.0f, 6.0f, 2.5f}, new String[]{"#", "Docente", "Tiempo total"}, COLOR_TOP_DOCENTES);
        int pos = 1;
        for (Map.Entry<String, Integer> entry : topDocentes.entrySet()) {
            String[] valores = {
                    String.valueOf(pos++),
                    entry.getKey(),
                    formatearMinutos(entry.getValue())
            };
            pdfBuilder.agregarFilaTabla(tabla, valores);
        }

        pdfBuilder.agregarTabla(tabla);
        pdfBuilder.agregarEspacio(8);
    }

    private void agregarTopGrados() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;

        Map<String, Integer> topGrados = (fechaInicio != null && fechaFin != null)
                ? registroPlataformaDAO.obtenerTopGradosUsoPlataforma(fechaInicio, fechaFin, 5)
                : registroPlataformaDAO.obtenerTopGradosUsoPlataforma(5);

        pdfBuilder.agregarSeccion("Top 5 Grados por Tiempo de Uso");

        if (topGrados.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No hay datos para este ranking.");
            return;
        }

        Table tabla = pdfBuilder.crearTabla(new float[]{1.0f, 6.0f, 2.5f}, new String[]{"#", "Grado", "Tiempo total"}, COLOR_TOP_GRADOS);
        int pos = 1;
        for (Map.Entry<String, Integer> entry : topGrados.entrySet()) {
            String[] valores = {
                    String.valueOf(pos++),
                    entry.getKey(),
                    formatearMinutos(entry.getValue())
            };
            pdfBuilder.agregarFilaTabla(tabla, valores);
        }

        pdfBuilder.agregarTabla(tabla);
        pdfBuilder.agregarEspacio(8);
    }

    private void agregarTopMotivos() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;

        List<Map<String, Object>> topMotivos = (fechaInicio != null && fechaFin != null)
                ? registroPlataformaDAO.obtenerTopMotivosUsoPlataformaConTiempo(fechaInicio, fechaFin, 5)
                : registroPlataformaDAO.obtenerTopMotivosUsoPlataformaConTiempo(5);

        pdfBuilder.agregarSeccion("Top 5 Motivos de Uso de Plataforma");

        if (topMotivos.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No hay datos para este ranking.");
            return;
        }

        Table tabla = pdfBuilder.crearTabla(new float[]{1.0f, 4.5f, 2.0f, 2.5f}, new String[]{"#", "Motivo", "Registros", "Tiempo total"}, COLOR_TOP_MOTIVOS);
        int pos = 1;
        for (Map<String, Object> fila : topMotivos) {
            int minutos = ((Number) fila.getOrDefault("minutos", 0)).intValue();
            String[] valores = {
                    String.valueOf(pos++),
                    String.valueOf(fila.getOrDefault("motivo", "Sin motivo")),
                    String.valueOf(fila.getOrDefault("total", 0)),
                    formatearMinutos(minutos)
            };
            pdfBuilder.agregarFilaTabla(tabla, valores);
        }

        pdfBuilder.agregarTabla(tabla);
        pdfBuilder.agregarEspacio(8);
    }

    private void agregarTablaDetalle() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;

        List<RegistroPlataformaDetalle> registros = informesDAO.obtenerRegistrosPlataforma(fechaInicio, fechaFin);

        pdfBuilder.agregarSeccion("Detalle General de Uso de Plataforma");

        if (registros.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No se encontraron registros de uso para el período seleccionado.");
            return;
        }

        String[] encabezados = {"Docente", "Motivo", "Fecha", "Inicio", "Fin", "Duración", "Grado"};
        float[] anchos = {2.2f, 2.6f, 1.2f, 1.0f, 1.0f, 1.2f, 0.8f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (RegistroPlataformaDetalle r : registros) {
            String duracion = formatearMinutos(r.getTotalMinutos());
            String grado = r.getGrado() > 0 ? String.valueOf(r.getGrado()) : "--";

            String[] valores = {
                    valorSeguro(r.getDocente()),
                    valorSeguro(r.getMotivoUso()),
                    valorSeguro(r.getFecha()),
                    valorSeguro(r.getHoraInicio()),
                    valorSeguro(r.getHoraFin()),
                    duracion,
                    grado
            };

            pdfBuilder.agregarFilaTabla(tabla, valores);
        }

        pdfBuilder.agregarTabla(tabla);
    }

    private String formatearMinutos(int minutos) {
        int horas = minutos / 60;
        int restantes = minutos % 60;
        return horas + "h " + restantes + "m";
    }

    private String valorSeguro(String valor) {
        return (valor == null || valor.isBlank()) ? "--" : valor;
    }
}
