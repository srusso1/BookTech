package controllers.Bibliotecario;

import database.PrestamosDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Prestamo;
import utils.Fechas;

import utils.Refrescable;

public class PrestamosActivosController implements Refrescable {

    public PrestamosActivosController(PrestamosDAO prestamosDAO) {
        this.prestamosDAO = prestamosDAO;
    }

    @FXML
    private TableView<Prestamo> tabla;

    @FXML
    private TableColumn<Prestamo, String> tbMotivo;

    @FXML
    private TableColumn<Prestamo, String> tbDocente;

    @FXML
    private TableColumn<Prestamo, String> tbTituloLibro;

    @FXML
    private TableColumn<Prestamo, String> tbEstudiante;

    @FXML
    private TableColumn<Prestamo, String> tbGrado;

    @FXML
    private TableColumn<Prestamo, String> tbFPrestamo;

    @FXML
    private TableColumn<Prestamo, String> tbFLimite;

    @FXML
    private TableColumn<Prestamo, String> tbEstado;

    private final PrestamosDAO prestamosDAO;

    @FXML
    public void initialize() {

        tbTituloLibro.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTituloLibro())
        );

        tbEstudiante.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEstudiante())
        );

        tbGrado.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getGrado()))
        );

        tbFPrestamo.setCellValueFactory(d ->
                new SimpleStringProperty(formatearFechaUI(d.getValue().getFecha_prestamo()))
        );

        tbFLimite.setCellValueFactory(d ->
                new SimpleStringProperty(formatearFechaUI(d.getValue().getFecha_limite()))
        );

        tbEstado.setCellValueFactory(d -> {
            int estado = d.getValue().getEstado();
            return new SimpleStringProperty(
                    estado == 0 ? "Prestado" : "Pendiente"
            );
        });

        tbMotivo.setCellValueFactory(d -> {
            var motivo = d.getValue().getMotivoPrestamo();
            return new SimpleStringProperty(motivo != null ? motivo.getNombre() : "");
        });

        tbDocente.setCellValueFactory(d -> {
            var docente = d.getValue().getDocente();
            return new SimpleStringProperty(docente != null ? docente.getNombreCompleto() : "");
        });

        tabla.setEditable(false);
        tabla.setPlaceholder(new Label("No hay prestamos activos"));
        tabla.setSelectionModel(null);
        tabla.getColumns().forEach(col -> col.setReorderable(false));



        cargarPrestamos();
    }

    private void cargarPrestamos() {
        tabla.setPlaceholder(new Label("Cargando préstamos..."));
        java.util.concurrent.CompletableFuture.supplyAsync(prestamosDAO::buscarPrestamosActivos)
                .thenAcceptAsync(prestamos -> {
                    tabla.getItems().setAll(prestamos);
                    tabla.setPlaceholder(new Label("No hay prestamos activos"));
                }, javafx.application.Platform::runLater);
    }

    private String formatearFechaUI(String fechaBD) {
        String fechaUI = Fechas.convertirAUI(fechaBD);
        return fechaUI != null ? fechaUI : fechaBD;
    }

    @Override
    public void refresh() {
        cargarPrestamos();
    }
}
