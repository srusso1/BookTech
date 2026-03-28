package controllers.Rectoria;


import database.PrestamosDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;

import java.util.Map;

public class EstadisticasController {

    @FXML
    private BarChart<String, Number> gfBarraGenero;
    @FXML
    private BarChart<Number, String> gfBarraCategoria;

    @FXML
    private PieChart gfTortaPrestamosGrado;

    private final PrestamosDAO prestamosDAO = new PrestamosDAO();

    @FXML
    public void initialize() {
        cargarGraficaGenero();
        cargarGraficaCategoria();
        cargarGraficaGrados();
    }

    private void cargarGraficaGenero() {
        gfBarraGenero.setTitle("Préstamos por género");
        gfBarraGenero.setAnimated(false);

        Map<String, Integer> datos = prestamosDAO.obtenerPrestamosPorGenero();

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
        gfBarraGenero.getData().addAll(hombres, mujeres);
    }

    private void cargarGraficaCategoria() {

        gfBarraCategoria.setAnimated(false);
        gfBarraCategoria.setLegendVisible(false);
        gfBarraCategoria.setTitle("Préstamos por categoría");

        Map<String, Integer> datos = prestamosDAO.obtenerPrestamosPorCategoria();

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
                    } else if (total < 3) {
                        color = "#60a5fa"; // azul claro
                    } else if (total < 6) {
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

        Map<String, Integer> datos = prestamosDAO.obtenerPrestamosPorGradoTop(5);

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
}
