package reports.generators;

import com.itextpdf.layout.element.Table;
import database.InformesDAO;
import model.InventarioLibroDetalle;
import reports.models.ReportConfig;
import utils.Alertas;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Generador de reporte de inventario con enfoque descriptivo.
 */
public class InventarioReportGenerator extends BaseReportGenerator {

    private final InformesDAO informesDAO;
    private final int umbralStock;

    public InventarioReportGenerator(ReportConfig config, int umbralStock) {
        super(config, "Reporte_Inventario_Libros.pdf");
        this.informesDAO = new InformesDAO();
        this.umbralStock = Math.max(1, umbralStock);
    }

    @Override
    public void generar() {
        if (!puedeGenerar()) {
            return;
        }

        try {
            agregarEncabezadoEstandar("Reporte de Inventario de Libros");
            agregarResumen();

            if (config.isIncluirTablas()) {
                agregarTablaDetalle();
            } else {
                pdfBuilder
                        .agregarSeccion("Detalle del inventario")
                        .agregarParrafoIndentado("No se incluyo la tabla detallada por decision del usuario.")
                        .agregarEspacio(8);
            }

            finalizarReporte();
            Alertas.mostrarExito("Reporte generado correctamente en:\n" + rutaArchivo);
        } catch (Exception e) {
            Alertas.mostrarError("Error al generar reporte de inventario: " + e.getMessage());
        }
    }

    private void agregarResumen() {
        List<InventarioLibroDetalle> inventario = informesDAO.obtenerInventarioParaCompra(umbralStock);
        int totalTitulos = inventario.size();
        int totalUnidades = inventario.stream().mapToInt(InventarioLibroDetalle::getUnidades).sum();
        int totalPrestamosActivos = inventario.stream().mapToInt(InventarioLibroDetalle::getPrestamosActivos).sum();
        long librosSinUnidades = inventario.stream().filter(item -> item.getUnidades() == 0).count();
        int menorStock = inventario.stream().mapToInt(InventarioLibroDetalle::getUnidades).min().orElse(0);

        pdfBuilder
                .agregarSeccion("Corte del reporte")
                .agregarLineaDetalle("Fecha de generacion", String.valueOf(LocalDate.now()))
                .agregarParrafoIndentado("Vista descriptiva del inventario actual de libros.")
                .agregarSeccion("Resumen")
                .agregarLineaDetalle("Titulos registrados", String.valueOf(totalTitulos))
                .agregarLineaDetalle("Unidades totales actuales", String.valueOf(totalUnidades))
                .agregarLineaDetalle("Prestamos activos", String.valueOf(totalPrestamosActivos))
                .agregarLineaDetalle("Libros sin unidades", String.valueOf(librosSinUnidades))
                .agregarLineaDetalle("Menor stock detectado", String.valueOf(menorStock) + " unidades")
                .agregarEspacio(12);

        agregarTop10MenosUnidades();
        agregarTop10MasSolicitados();
    }

    private void agregarTop10MenosUnidades() {
        List<InventarioLibroDetalle> topMenosUnidades = informesDAO.obtenerInventarioParaCompra(umbralStock)
                .stream()
                .sorted(Comparator
                        .comparingInt(InventarioLibroDetalle::getUnidades)
                        .thenComparing(Comparator.comparingInt(InventarioLibroDetalle::getPrestamosHistoricos).reversed())
                        .thenComparing(InventarioLibroDetalle::getTitulo, String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .toList();

        if (topMenosUnidades.isEmpty()) {
            return;
        }

        pdfBuilder.agregarSeccion("Top 10 libros con menos unidades");
        String[] encabezados = {"#", "Libro", "Categoria", "Unidades", "Prestamos activos"};
        float[] anchos = {0.5f, 3.4f, 1.8f, 0.9f, 1.2f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        int posicion = 1;
        for (InventarioLibroDetalle libro : topMenosUnidades) {
            String[] fila = {
                    String.valueOf(posicion++),
                    libro.getTitulo(),
                    libro.getCategoria(),
                    String.valueOf(libro.getUnidades()),
                    String.valueOf(libro.getPrestamosActivos())
            };
            pdfBuilder.agregarFilaTabla(tabla, fila);
        }
        pdfBuilder.agregarTabla(tabla).agregarEspacio(8);
    }

    private void agregarTop10MasSolicitados() {
        List<Map<String, Object>> topSolicitados = informesDAO.obtenerTopLibrosDetalle(null, null, 10);
        if (topSolicitados.isEmpty()) {
            return;
        }

        pdfBuilder.agregarSeccion("Top 10 libros mas solicitados");
        String[] encabezados = {"#", "Libro", "Solicitudes", "Estudiantes", "Grado frecuente"};
        float[] anchos = {0.5f, 3.4f, 1.0f, 1.0f, 1.2f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        int posicion = 1;
        for (Map<String, Object> filaTop : topSolicitados) {
            String[] fila = {
                    String.valueOf(posicion++),
                    String.valueOf(filaTop.getOrDefault("libro", "N/A")),
                    String.valueOf(filaTop.getOrDefault("total_solicitudes", 0)),
                    String.valueOf(filaTop.getOrDefault("estudiantes_unicos", 0)),
                    String.valueOf(filaTop.getOrDefault("grado_frecuente", "N/A"))
            };
            pdfBuilder.agregarFilaTabla(tabla, fila);
        }
        pdfBuilder.agregarTabla(tabla).agregarEspacio(8);
    }

    private void agregarTablaDetalle() {
        List<InventarioLibroDetalle> inventario = informesDAO.obtenerInventarioParaCompra(umbralStock);

        if (inventario.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No se encontraron libros en inventario.");
            return;
        }

        pdfBuilder.agregarSeccion("Detalle del inventario");

        String[] encabezados = {
                "Libro", "Categoria", "Unidades", "Prestamos activos", "Stock objetivo", "Comprar", "Estado"
        };
        float[] anchos = {2.8f, 1.8f, 0.9f, 1.1f, 1.1f, 0.9f, 1.2f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (InventarioLibroDetalle item : inventario) {
            String[] fila = {
                    item.getTitulo(),
                    item.getCategoria(),
                    String.valueOf(item.getUnidades()),
                    String.valueOf(item.getPrestamosActivos()),
                    String.valueOf(item.getStockObjetivo()),
                    String.valueOf(item.getRecomendadasComprar()),
                    item.getEstadoStock()
            };
            pdfBuilder.agregarFilaTabla(tabla, fila);
        }

        pdfBuilder.agregarTabla(tabla);
    }
}




