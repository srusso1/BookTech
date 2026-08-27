package reports.generators;

import com.itextpdf.layout.element.Table;
import database.InformesDAO;
import model.InventarioLibroDetalle;
import reports.models.ReportConfig;

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
    public void generar() throws Exception {
        if (!puedeGenerar()) {
            return;
        }

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
    }

    private void agregarResumen() {
        List<InventarioLibroDetalle> inventario = informesDAO.obtenerInventarioParaCompra(umbralStock);
        int totalTitulos = inventario.size();
        int totalUnidades = inventario.stream().mapToInt(InventarioLibroDetalle::getUnidades).sum();
        int totalPrestamosActivos = inventario.stream().mapToInt(InventarioLibroDetalle::getPrestamosActivos).sum();
        long librosSinUnidades = inventario.stream().filter(item -> item.getUnidades() == 0).count();
        int menorStock = inventario.stream().mapToInt(InventarioLibroDetalle::getUnidades).min().orElse(0);

        long librosParaComprar = inventario.stream()
                .filter(item -> item.getRecomendadasComprar() > 0)
                .count();

        int unidadesSugeridas = inventario.stream()
                .mapToInt(InventarioLibroDetalle::getRecomendadasComprar)
                .sum();

        String fechaCorte = LocalDate.now().toString();

        pdfBuilder
                .agregarSeccion("Descripcion del Informe")
                .agregarParrafoIndentado("Este informe consolida el estado actual del inventario bibliografico, identificando niveles de disponibilidad y recomendaciones de compra segun la rotacion por prestamos activos.")
                .agregarLineaDetalle("Fecha de corte", fechaCorte)
                .agregarLineaDetalle("Umbral base de stock minimo", String.valueOf(umbralStock))
                .agregarSeccion("Resumen General de Inventario")
                .agregarLineaDetalle("Total de titulos en catalogo", String.valueOf(totalTitulos))
                .agregarLineaDetalle("Total de unidades fisicas disponibles", String.valueOf(totalUnidades))
                .agregarLineaDetalle("Prestamos actualmente activos", String.valueOf(totalPrestamosActivos))
                .agregarLineaDetalle("Titulos agotados (0 unidades)", String.valueOf(librosSinUnidades))
                .agregarLineaDetalle("Menor stock registrado", String.valueOf(menorStock))
                .agregarSeccion("Diagnostico de Reposicion")
                .agregarLineaDetalle("Titulos sugeridos para compra", String.valueOf(librosParaComprar))
                .agregarLineaDetalle("Unidades totales sugeridas para compra", String.valueOf(unidadesSugeridas))
                .agregarEspacio(8);

        agregarTopCategoriasStock(inventario);
    }

    private void agregarTopCategoriasStock(List<InventarioLibroDetalle> inventario) {
        if (inventario == null || inventario.isEmpty()) {
            return;
        }

        Map<String, Integer> unidadesPorCategoria = new java.util.LinkedHashMap<>();
        for (InventarioLibroDetalle item : inventario) {
            String cat = valorSeguro(item.getCategoria());
            unidadesPorCategoria.put(cat, unidadesPorCategoria.getOrDefault(cat, 0) + item.getUnidades());
        }

        List<Map.Entry<String, Integer>> ordenadas = unidadesPorCategoria.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .toList();

        pdfBuilder.agregarSeccion("Top Categorias con Mayor Disponibilidad");
        Table tabla = pdfBuilder.crearTabla(new float[]{1.0f, 6.0f, 2.0f}, new String[]{"#", "Categoria", "Unidades"});
        int pos = 1;
        for (Map.Entry<String, Integer> entry : ordenadas) {
            tabla.addCell(String.valueOf(pos++));
            tabla.addCell(entry.getKey());
            tabla.addCell(String.valueOf(entry.getValue()));
        }
        pdfBuilder.agregarTabla(tabla);
        pdfBuilder.agregarEspacio(8);
    }

    private void agregarTablaDetalle() {
        List<InventarioLibroDetalle> inventario = informesDAO.obtenerInventarioParaCompra(umbralStock);

        pdfBuilder.agregarSeccion("Detalle Completo de Inventario");

        if (inventario.isEmpty()) {
            pdfBuilder.agregarParrafoIndentado("No se encontraron registros de inventario.");
            return;
        }

        inventario.sort(Comparator.comparing(InventarioLibroDetalle::getRecomendadasComprar).reversed()
                .thenComparing(InventarioLibroDetalle::getUnidades));

        String[] encabezados = {
                "Libro", "Categoria", "Autor", "Editorial", "Ubicacion",
                "Fisicas", "Prestadas", "Objetivo", "Comprar", "Estado"
        };
        float[] anchos = {2.2f, 1.4f, 1.4f, 1.2f, 1.0f, 0.7f, 0.8f, 0.8f, 0.8f, 1.1f};
        Table tabla = pdfBuilder.crearTabla(anchos, encabezados);

        for (InventarioLibroDetalle item : inventario) {
            String[] valores = {
                    valorSeguro(item.getTitulo()),
                    valorSeguro(item.getCategoria()),
                    valorSeguro(item.getAutor()),
                    valorSeguro(item.getEditorial()),
                    valorSeguro(item.getUbicacion()),
                    String.valueOf(item.getUnidades()),
                    String.valueOf(item.getPrestamosActivos()),
                    String.valueOf(item.getStockObjetivo()),
                    String.valueOf(item.getRecomendadasComprar()),
                    valorSeguro(item.getEstadoStock())
            };
            pdfBuilder.agregarFilaTabla(tabla, valores);
        }

        pdfBuilder.agregarTabla(tabla);
    }

    private String valorSeguro(String valor) {
        return (valor == null || valor.isBlank()) ? "--" : valor;
    }
}
