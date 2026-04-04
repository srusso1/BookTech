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

public class EstadisticasController {

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

    private final PrestamosDAO prestamosDAO = new PrestamosDAO();
    private final RegistroPlataformaDAO registroPlataformaDAO = new RegistroPlataformaDAO();

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

    private void cargarGraficaGenero() {
        gfBarraGenero.setTitle("Préstamos por género");
        gfBarraGenero.setAnimated(false);

        Map<String, Integer> datos = (fechaFiltroInicio != null && fechaFiltroFin != null) ?
                prestamosDAO.obtenerPrestamosPorGenero(fechaFiltroInicio, fechaFiltroFin) :
                prestamosDAO.obtenerPrestamosPorGenero();

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

                // 🔥 Tooltip
                data.nodeProperty().addListener((obs, oldNode, node) -> {
                    if (node != null) {
                        Tooltip.install(node, new Tooltip("Total: " + total));
                    }
                });

            } else if (genero.equals("FEMENINO")) {

                XYChart.Data<String, Number> data = new XYChart.Data<>("Mujeres", total);
                mujeres.getData().add(data);

                // 🔥 Tooltip
                data.nodeProperty().addListener((obs, oldNode, node) -> {
                    if (node != null) {
                        Tooltip.install(node, new Tooltip("Total: " + total));
                    }
                });
            }
        }

        gfBarraGenero.getData().clear();
        gfBarraGenero.getData().add(hombres);
        gfBarraGenero.getData().add(mujeres);
    }

    private void cargarGraficaCategoria() {

        gfBarraCategoria.setAnimated(true);
        gfBarraCategoria.setLegendVisible(false);
        gfBarraCategoria.setTitle("Préstamos por categoría");

        Map<String, Integer> datos = (fechaFiltroInicio != null && fechaFiltroFin != null) ?
                prestamosDAO.obtenerPrestamosPorCategoria(fechaFiltroInicio, fechaFiltroFin) :
                prestamosDAO.obtenerPrestamosPorCategoria();

        XYChart.Series<Number, String> serie = new XYChart.Series<>();
        serie.setName("Categorías");

        for (Map.Entry<String, Integer> entry : datos.entrySet()) {

            String categoria = entry.getKey();
            Integer total = entry.getValue();

            XYChart.Data<Number, String> data = new XYChart.Data<>(total, categoria);
            serie.getData().add(data);

            // 🔥 Tooltip + Color dinámico
            data.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {

                    // Tooltip
                    Tooltip.install(node, new Tooltip(categoria + ": " + total));

                    // 🎨 Color según cantidad (puedes ajustar rangos)
                    String color;

                    if (total == 0) {
                        color = "#9ca3af"; // gris
                    } else if (total < 20) {
                        color = "#60a5fa"; // azul claro
                    } else if (total < 60) {
                        color = "#34d399"; // verde
                    } else {
                        color = "#f59e0b"; // naranja fuerte
                    }

                    node.setStyle("-fx-bar-fill: " + color + ";");
                }
            });
        }

        gfBarraCategoria.getData().clear();
        gfBarraCategoria.getData().add(serie);
    }

    private void cargarGraficaGrados() {

        gfTortaPrestamosGrado.setTitle("Top 5 grados con más préstamos");

        Map<String, Integer> datos = (fechaFiltroInicio != null && fechaFiltroFin != null) ?
                prestamosDAO.obtenerPrestamosPorGradoTop(5, fechaFiltroInicio, fechaFiltroFin) :
                prestamosDAO.obtenerPrestamosPorGradoTop(5);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        // 🔥 Calcular total general
        int totalGeneral = datos.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : datos.entrySet()) {

            String grado = entry.getKey();
            int total = entry.getValue();

            PieChart.Data data = new PieChart.Data(grado, total);
            pieData.add(data);

            // 🔥 Tooltip con valor + porcentaje
            data.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {

                    double porcentaje = (totalGeneral == 0) ? 0 :
                            (total * 100.0) / totalGeneral;

                    String texto = total + " - " + String.format("%.1f", porcentaje) + "%";

                    Tooltip.install(node, new Tooltip(grado + ": " + texto));
                }
            });
        }

        gfTortaPrestamosGrado.setData(pieData);
    }

    private void cargarGraficaDocentes() {

        gfBarraDocentes.setAnimated(true);
        gfBarraDocentes.setLegendVisible(false);
        gfBarraDocentes.setTitle("Top 5 docentes que envian más estudiantes a la biblioteca");

        Map<String, Integer> datos = (fechaFiltroInicio != null && fechaFiltroFin != null) ?
                prestamosDAO.obtenerPrestamosPorDocenteTop(5, fechaFiltroInicio, fechaFiltroFin) :
                prestamosDAO.obtenerPrestamosPorDocenteTop(5);

        XYChart.Series<Number, String> serie = new XYChart.Series<>();
        serie.setName("Docentes");

        for (Map.Entry<String, Integer> entry : datos.entrySet()) {
            String docente = entry.getKey();
            Integer total = entry.getValue();

            XYChart.Data<Number, String> data = new XYChart.Data<>(total, docente);
            serie.getData().add(data);

            data.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {
                    Tooltip.install(node, new Tooltip(docente + ": " + total));

                    String color;
                    if (total == 0) {
                        color = "#9ca3af";
                    } else if (total < 20) {
                        color = "#60a5fa";
                    } else if (total < 60) {
                        color = "#34d399";
                    } else {
                        color = "#f59e0b";
                    }

                    node.setStyle("-fx-bar-fill: " + color + ";");
                }
            });
        }

        gfBarraDocentes.getData().clear();
        gfBarraDocentes.getData().add(serie);
    }

    private void cargarGraficaPlataformaVirtual() {

        gfBarraPlataformaVirtual.setAnimated(true);
        gfBarraPlataformaVirtual.setLegendVisible(false);
        gfBarraPlataformaVirtual.setTitle("Top 5 docentes con más uso de la plataforma");
        if (gfBarraPlataformaVirtual.getXAxis() instanceof NumberAxis numberAxis) {
            numberAxis.setLabel("Horas acumuladas");
        }

        Map<String, Integer> datos = (fechaFiltroInicio != null && fechaFiltroFin != null) ?
                registroPlataformaDAO.obtenerTopDocentesUsoPlataforma(fechaFiltroInicio, fechaFiltroFin, 5) :
                registroPlataformaDAO.obtenerTopDocentesUsoPlataforma(5);

        XYChart.Series<Number, String> serie = new XYChart.Series<>();
        serie.setName("Plataforma virtual");

        for (Map.Entry<String, Integer> entry : datos.entrySet()) {
            String docente = entry.getKey();
            Integer totalMinutos = entry.getValue();
            double totalHoras = totalMinutos / 60.0;

            XYChart.Data<Number, String> data = new XYChart.Data<>(totalHoras, docente);
            serie.getData().add(data);

            data.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {
                    Tooltip.install(node, new Tooltip(docente + ": " + GeneradorHoras.formatearMinutos(totalMinutos)));

                    String color;
                    if (totalHoras == 0) {
                        color = "#9ca3af";
                    } else if (totalHoras < 2) {
                        color = "#60a5fa";
                    } else if (totalHoras < 5) {
                        color = "#34d399";
                    } else {
                        color = "#f59e0b";
                    }

                    node.setStyle("-fx-bar-fill: " + color + ";");
                }
            });
        }

        gfBarraPlataformaVirtual.getData().clear();
        gfBarraPlataformaVirtual.getData().add(serie);
    }

    private void cargarGraficaGradosPlataforma() {

        gfTortaGradosPlataforma.setTitle("Top 5 grados con más uso de plataforma");

        Map<String, Integer> datos = (fechaFiltroInicio != null && fechaFiltroFin != null) ?
                registroPlataformaDAO.obtenerTopGradosUsoPlataforma(fechaFiltroInicio, fechaFiltroFin, 5) :
                registroPlataformaDAO.obtenerTopGradosUsoPlataforma(5);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        // 🔥 Calcular total general
        int totalGeneral = datos.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : datos.entrySet()) {

            String grado = entry.getKey();
            int totalMinutos = entry.getValue();

            PieChart.Data data = new PieChart.Data(grado, totalMinutos);
            pieData.add(data);

            // 🔥 Tooltip con valor + porcentaje + formato de horas
            data.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {

                    double porcentaje = (totalGeneral == 0) ? 0 :
                            (totalMinutos * 100.0) / totalGeneral;

                    String horasFormato = GeneradorHoras.formatearMinutos(totalMinutos);
                    String texto = horasFormato + " - " + String.format("%.1f", porcentaje) + "%";

                    Tooltip.install(node, new Tooltip(grado + ": " + texto));
                }
            });
        }

        gfTortaGradosPlataforma.setData(pieData);
    }
}
