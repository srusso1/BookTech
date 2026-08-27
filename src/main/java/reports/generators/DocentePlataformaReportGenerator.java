package reports.generators;

import com.itextpdf.layout.element.Table;
import database.InformesDAO;
import model.RegistroPlataformaDetalle;
import reports.models.ReportConfig;
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
    public void generar() throws Exception {
        if (!puedeGenerar()) {
            return;
        }

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
                .agregarSeccion("Resumen de Uso")
                .agregarLineaDetalle("Total de registros", String.valueOf(resumen.getOrDefault("totalRegistros", 0)))
                .agregarLineaDetalle("Tiempo total de uso", formatearMinutos(totalMinutos))
                .agregarLineaDetalle("Motivo más frecuente", String.valueOf(resumen.getOrDefault("motivoMasFrecuente", "Sin datos")))
                .agregarLineaDetalle("Grado más atendido", String.valueOf(resumen.getOrDefault("gradoMasFrecuente", "Sin datos")))
                .agregarEspacio(10);
    }

    private void agregarTablaDetalle() {
        String fechaInicio = config.getFechaInicio() != null ? Fechas.convertirAISO(config.getFechaInicio()) : null;
        String fechaFin = config.getFechaFin() != null ? Fechas.convertirAISO(config.getFechaFin()) : null;

        List<RegistroPlataformaDetalle> registros = informesDAO.obtenerRegistrosPlataformaPorDocente(idDocente, fechaInicio, fechaFin);

        pdfBuilder.agregarSeccion("Detalle cronológico de uso");

        if (registros.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No se encontraron registros de uso para el docente en el período seleccionado.");
            return;
        }

        String[] encabezados = {"Fecha", "Motivo", "Inicio", "Fin", "Duración", "Grado"};
        float[] anchos = {1.5f, 3.2f, 1.2f, 1.2f, 1.5f, 1.0f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (RegistroPlataformaDetalle r : registros) {
            String duracion = formatearMinutos(r.getTotalMinutos());
            String grado = r.getGrado() > 0 ? String.valueOf(r.getGrado()) : "--";

            String[] valores = {
                    valorSeguro(r.getFecha()),
                    valorSeguro(r.getMotivoUso()),
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
