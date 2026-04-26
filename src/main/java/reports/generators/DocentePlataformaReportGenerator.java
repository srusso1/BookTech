package reports.generators;

import com.itextpdf.layout.element.Table;
import database.InformesDAO;
import model.RegistroPlataformaDetalle;
import reports.models.ReportConfig;
import utils.Alertas;
import utils.Fechas;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DocentePlataformaReportGenerator extends BaseReportGenerator {

    private final InformesDAO informesDAO;
    private final int idDocente;
    private final String nombreDocente;

    public DocentePlataformaReportGenerator(ReportConfig config, int idDocente, String nombreDocente) {
        super(config, construirNombreArchivo(nombreDocente));
        this.informesDAO = new InformesDAO();
        this.idDocente = idDocente;
        this.nombreDocente = nombreDocente;
    }

    private static String construirNombreArchivo(String nombreDocente) {
        String base = nombreDocente == null ? "DOCENTE" : nombreDocente;
        String limpio = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        if (limpio.isEmpty()) {
            limpio = "DOCENTE";
        }
        return "Informe_Plataforma_" + limpio + ".pdf";
    }

    @Override
    public void generar() {
        if (!puedeGenerar()) {
            return;
        }

        try {
            agregarEncabezadoEstandar("Informe de Uso de Plataforma por Docente");
            agregarDescripcionResumen();

            if (config.isIncluirTablas()) {
                agregarTablaDetalle();
            } else {
                pdfBuilder
                        .agregarSeccion("Detalle de uso de plataforma")
                        .agregarParrafoIndentado("No se incluyó la tabla detallada por decisión del usuario.")
                        .agregarEspacio(8);
            }

            finalizarReporte();

        } catch (Exception e) {
            Alertas.mostrarError("Error al generar reporte: " + e.getMessage());
        }
    }

    private void agregarDescripcionResumen() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;
        String filtroTexto = (fechaInicio != null && fechaFin != null) ? fechaInicio + " - " + fechaFin : "No aplicado";

        Map<String, Object> resumen = informesDAO.obtenerResumenPlataformaDocente(idDocente, fechaInicio, fechaFin);
        int totalMinutos = ((Number) resumen.getOrDefault("totalMinutos", 0)).intValue();

        pdfBuilder
                .agregarSeccion("Descripción del Informe")
                .agregarParrafoIndentado("Este informe presenta el uso de la plataforma virtual para el docente seleccionado.")
                .agregarLineaDetalle("Docente", valorSeguro(nombreDocente))
                .agregarLineaDetalle("Filtro de fecha aplicado", filtroTexto)
                .agregarSeccion("Indicadores Principales")
                .agregarLineaDetalle("Total de registros", String.valueOf(resumen.getOrDefault("totalRegistros", 0)))
                .agregarLineaDetalle("Tiempo acumulado", formatearMinutos(totalMinutos))
                .agregarLineaDetalle("Motivo de uso más frecuente", String.valueOf(resumen.getOrDefault("motivoTop", "Sin datos")))
                .agregarLineaDetalle("Grado más frecuente", String.valueOf(resumen.getOrDefault("gradoTop", "Sin datos")))
                .agregarEspacio(12);
    }

    private void agregarTablaDetalle() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;

        List<RegistroPlataformaDetalle> datos = informesDAO.obtenerRegistrosPlataformaPorDocente(idDocente, fechaInicio, fechaFin);
        pdfBuilder.agregarSeccion("Tabla general a detalle");

        if (datos.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No se encontraron registros de uso de plataforma para el docente con los filtros aplicados.");
            return;
        }

        String[] encabezados = {"Motivo de uso", "Fecha", "Hora inicio", "Hora fin", "Duración", "Grado"};
        float[] anchos = {2.8f, 1.3f, 1.1f, 1.1f, 1.2f, 0.9f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (RegistroPlataformaDetalle registro : datos) {
            String[] fila = {
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

