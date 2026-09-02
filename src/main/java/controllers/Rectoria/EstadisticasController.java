package controllers.Rectoria;


import database.PrestamosDAO;
import database.RegistroPlataformaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import utils.Alertas;
import utils.Fechas;
import utils.GeneradorHoras;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

public class EstadisticasController {

    public EstadisticasController(PrestamosDAO prestamosDAO, RegistroPlataformaDAO registroPlataformaDAO) {
        this.prestamosDAO = prestamosDAO;
        this.registroPlataformaDAO = registroPlataformaDAO;
    }

    @FXML
    private BarChart<String, Number> gfBarraGenero;
    @FXML
    private BarChart<Number, String> gfBarraCategoria;

    @FXML
    private BarChart<Number, String> gfBarraDocentes;

    @FXML
    private BarChart<Number, String> gfBarraPlataformaVirtual;

    @FXML
    private PieChart gfTortaGradosPlataforma;

    @FXML
    private PieChart gfTortaPrestamosGrado;

    @FXML
    private DatePicker dpFechaInicio;

    @FXML
    private DatePicker dpFechaFin;

    @FXML
    private Button btnFiltrar;

    @FXML
    private Button btnLimpiarFiltro;

    private final PrestamosDAO prestamosDAO;
    private final RegistroPlataformaDAO registroPlataformaDAO;

    private String fechaFiltroInicio = null;
    private String fechaFiltroFin = null;

    @FXML
    public void initialize() {
        // Establecer fechas por defecto (mes actual)
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.withDayOfMonth(1));
        dpFechaFin.setValue(hoy);
        
        cargarGraficaGenero();
        cargarGraficaCategoria();
        cargarGraficaGrados();
        cargarGraficaDocentes();
        cargarGraficaPlataformaVirtual();
        cargarGraficaGradosPlataforma();
    }

    @FXML
    void clickFiltrar() {
        LocalDate fechaInicio = dpFechaInicio.getValue();
        LocalDate fechaFin = dpFechaFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            Alertas.mostrarError("Debe seleccionar ambas fechas");
            return;
        }

        if (fechaInicio.isAfter(fechaFin)) {
            Alertas.mostrarError("La fecha inicio no puede ser mayor que la fecha fin");
            return;
        }

        this.fechaFiltroInicio = Fechas.convertirAISO(fechaInicio);
        this.fechaFiltroFin = Fechas.convertirAISO(fechaFin);

        recargarGraficas();
    }

    @FXML
    void clickLimpiarFiltro() {
        this.fechaFiltroInicio = null;
        this.fechaFiltroFin = null;
        
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.withDayOfMonth(1));
        dpFechaFin.setValue(hoy);
        
        recargarGraficas();
        Alertas.mostrarExito("Filtro limpiado, mostrando datos sin restricción de fechas");
    }

    private void recargarGraficas() {
        cargarGraficaGenero();
        cargarGraficaCategoria();
        cargarGraficaGrados();
        cargarGraficaDocentes();
        cargarGraficaPlataformaVirtual();
        cargarGraficaGradosPlataforma();
    }

    private void aplicarEstiloBarra(javafx.scene.Node node, String tooltipText, double valor, double maximo) {
        if (node != null) {
            Tooltip.install(node, new Tooltip(tooltipText));
            String color;
            if (valor == 0) {
                color = "#9ca3af";
            } else if (maximo > 0) {
                double pct = valor / maximo;
                if (pct < 0.33) color = "#60a5fa";
                else if (pct < 0.66) color = "#34d399";
                else color = "#f59e0b";
            } else {
                color = "#60a5fa";
            }
            node.setStyle("-fx-bar-fill: " + color + ";");
        }
    }

    private void cargarGraficaGenero() {
        gfBarraGenero.setTitle("Préstamos por género (Cargando...)");
        gfBarraGenero.setAnimated(false);
        final String fInicio = this.fechaFiltroInicio;
        final String fFin = this.fechaFiltroFin;

        CompletableFuture.supplyAsync(() -> prestamosDAO.obtenerPrestamosPorGenero(fInicio, fFin))
            .thenAcceptAsync(datos -> {
                XYChart.Series<String, Number> hombres = new XYChart.Series<>();
                hombres.setName("Masculino");

                XYChart.Series<String, Number> mujeres = new XYChart.Series<>();
                mujeres.setName("Femenino");

                for (Map.Entry<String, Integer> entry : datos.entrySet()) {
                    String genero = entry.getKey().toUpperCase();
                    Integer total = entry.getValue();

                    if (genero.equals("MASCULINO")) {
                        XYChart.Data<String, Number> data = new XYChart.Data<>("Hombres", total);
                        hombres.getData().add(data);
                        data.nodeProperty().addListener((obs, oldNode, node) -> {
                            if (node != null) Tooltip.install(node, new Tooltip("Total: " + total));
                        });
                    } else if (genero.equals("FEMENINO")) {
                        XYChart.Data<String, Number> data = new XYChart.Data<>("Mujeres", total);
                        mujeres.getData().add(data);
                        data.nodeProperty().addListener((obs, oldNode, node) -> {
                            if (node != null) Tooltip.install(node, new Tooltip("Total: " + total));
                        });
                    }
                }

                gfBarraGenero.getData().clear();
                gfBarraGenero.getData().add(hombres);
                gfBarraGenero.getData().add(mujeres);
                gfBarraGenero.setTitle("Préstamos por género");
            }, Platform::runLater);
    }

    private void cargarGraficaCategoria() {
        gfBarraCategoria.setAnimated(false);
        gfBarraCategoria.setLegendVisible(false);
        gfBarraCategoria.setTitle("Préstamos por categoría (Cargando...)");
        final String fInicio = this.fechaFiltroInicio;
        final String fFin = this.fechaFiltroFin;

        CompletableFuture.supplyAsync(() -> prestamosDAO.obtenerPrestamosPorCategoria(fInicio, fFin))
            .thenAcceptAsync(datos -> {
                XYChart.Series<Number, String> serie = new XYChart.Series<>();
                serie.setName("Categorías");

                double maximo = datos.values().stream().mapToInt(Integer::intValue).max().orElse(0);

                for (Map.Entry<String, Integer> entry : datos.entrySet()) {
                    String categoria = entry.getKey();
                    Integer total = entry.getValue();

                    XYChart.Data<Number, String> data = new XYChart.Data<>(total, categoria);
                    serie.getData().add(data);

                    data.nodeProperty().addListener((obs, oldNode, node) -> {
                        aplicarEstiloBarra(node, categoria + ": " + total, total, maximo);
                    });
                }

                gfBarraCategoria.getData().clear();
                gfBarraCategoria.getData().add(serie);
                gfBarraCategoria.setTitle("Préstamos por categoría");
            }, Platform::runLater);
    }

    private void cargarGraficaGrados() {
        gfTortaPrestamosGrado.setTitle("Top 5 grados con más préstamos (Cargando...)");
        final String fInicio = this.fechaFiltroInicio;
        final String fFin = this.fechaFiltroFin;

        CompletableFuture.supplyAsync(() -> prestamosDAO.obtenerPrestamosPorGradoTop(5, fInicio, fFin))
            .thenAcceptAsync(datos -> {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                int totalGeneral = datos.values().stream().mapToInt(Integer::intValue).sum();

                for (Map.Entry<String, Integer> entry : datos.entrySet()) {
                    String grado = entry.getKey();
                    int total = entry.getValue();

                    PieChart.Data data = new PieChart.Data(grado, total);
                    pieData.add(data);

                    data.nodeProperty().addListener((obs, oldNode, node) -> {
                        if (node != null) {
                            double porcentaje = (totalGeneral == 0) ? 0 : (total * 100.0) / totalGeneral;
                            String texto = total + " - " + String.format("%.1f", porcentaje) + "%";
                            Tooltip.install(node, new Tooltip(grado + ": " + texto));
                        }
                    });
                }

                gfTortaPrestamosGrado.setData(pieData);
                gfTortaPrestamosGrado.setTitle("Top 5 grados con más préstamos");
            }, Platform::runLater);
    }

    private void cargarGraficaDocentes() {
        gfBarraDocentes.setAnimated(false);
        gfBarraDocentes.setLegendVisible(false);
        gfBarraDocentes.setTitle("Top 5 docentes (Cargando...)");
        final String fInicio = this.fechaFiltroInicio;
        final String fFin = this.fechaFiltroFin;

        CompletableFuture.supplyAsync(() -> prestamosDAO.obtenerPrestamosPorDocenteTop(5, fInicio, fFin))
            .thenAcceptAsync(datos -> {
                XYChart.Series<Number, String> serie = new XYChart.Series<>();
                serie.setName("Docentes");
                double maximo = datos.values().stream().mapToInt(Integer::intValue).max().orElse(0);

                for (Map.Entry<String, Integer> entry : datos.entrySet()) {
                    String docente = entry.getKey();
                    Integer total = entry.getValue();

                    XYChart.Data<Number, String> data = new XYChart.Data<>(total, docente);
                    serie.getData().add(data);

                    data.nodeProperty().addListener((obs, oldNode, node) -> {
                        aplicarEstiloBarra(node, docente + ": " + total, total, maximo);
                    });
                }

                gfBarraDocentes.getData().clear();
                gfBarraDocentes.getData().add(serie);
                gfBarraDocentes.setTitle("Top 5 docentes que envían más estudiantes");
            }, Platform::runLater);
    }

    private void cargarGraficaPlataformaVirtual() {
        gfBarraPlataformaVirtual.setAnimated(true);
        gfBarraPlataformaVirtual.setLegendVisible(false);
        gfBarraPlataformaVirtual.setTitle("Top 5 docentes en plataforma (Cargando...)");
        if (gfBarraPlataformaVirtual.getXAxis() instanceof NumberAxis numberAxis) {
            numberAxis.setLabel("Horas acumuladas");
        }
        final String fInicio = this.fechaFiltroInicio;
        final String fFin = this.fechaFiltroFin;

        CompletableFuture.supplyAsync(() -> registroPlataformaDAO.obtenerTopDocentesUsoPlataforma(fInicio, fFin, 5))
            .thenAcceptAsync(datos -> {
                XYChart.Series<Number, String> serie = new XYChart.Series<>();
                serie.setName("Plataforma virtual");
                double maximoHoras = datos.values().stream().mapToInt(Integer::intValue).max().orElse(0) / 60.0;

                for (Map.Entry<String, Integer> entry : datos.entrySet()) {
                    String docente = entry.getKey();
                    Integer totalMinutos = entry.getValue();
                    double totalHoras = totalMinutos / 60.0;

                    XYChart.Data<Number, String> data = new XYChart.Data<>(totalHoras, docente);
                    serie.getData().add(data);

                    data.nodeProperty().addListener((obs, oldNode, node) -> {
                        aplicarEstiloBarra(node, docente + ": " + GeneradorHoras.formatearMinutos(totalMinutos), totalHoras, maximoHoras);
                    });
                }

                gfBarraPlataformaVirtual.getData().clear();
                gfBarraPlataformaVirtual.getData().add(serie);
                gfBarraPlataformaVirtual.setTitle("Top 5 docentes con más uso de la plataforma");
            }, Platform::runLater);
    }

    private void cargarGraficaGradosPlataforma() {
        gfTortaGradosPlataforma.setTitle("Top 5 grados en plataforma (Cargando...)");
        final String fInicio = this.fechaFiltroInicio;
        final String fFin = this.fechaFiltroFin;

        CompletableFuture.supplyAsync(() -> registroPlataformaDAO.obtenerTopGradosUsoPlataforma(fInicio, fFin, 5))
            .thenAcceptAsync(datos -> {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                int totalGeneral = datos.values().stream().mapToInt(Integer::intValue).sum();

                for (Map.Entry<String, Integer> entry : datos.entrySet()) {
                    String grado = entry.getKey();
                    int totalMinutos = entry.getValue();

                    PieChart.Data data = new PieChart.Data(grado, totalMinutos);
                    pieData.add(data);

                    data.nodeProperty().addListener((obs, oldNode, node) -> {
                        if (node != null) {
                            double porcentaje = (totalGeneral == 0) ? 0 : (totalMinutos * 100.0) / totalGeneral;
                            String horasFormato = GeneradorHoras.formatearMinutos(totalMinutos);
                            String texto = horasFormato + " - " + String.format("%.1f", porcentaje) + "%";
                            Tooltip.install(node, new Tooltip(grado + ": " + texto));
                        }
                    });
                }

                gfTortaGradosPlataforma.setData(pieData);
                gfTortaGradosPlataforma.setTitle("Top 5 grados con más uso de plataforma");
            }, Platform::runLater);
    }
}
