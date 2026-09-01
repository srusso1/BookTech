package controllers.Bibliotecario;

import database.LibrosDAO;
import database.PrestamosDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.Libro;
import model.Prestamo;
import utils.Alertas;
import utils.Fechas;

import java.util.ArrayList;
import java.util.List;

public class DevolucionController {

    private Libro libro;
    private List<Prestamo> prestamos;
    private final PrestamosDAO prestamosDAO;
    private final LibrosDAO librosDAO;

    // Ã°Å¸â€Â¹ mÃƒÂ©todo para recibir el libro
    public void setLibro(Libro libro) {
        this.libro = libro;
        lblLibro.setText(libro.getTitulo() + " Ã¢â‚¬â€ " + libro.getAutor());
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
        cargarPrestamos();
    }

    public DevolucionController(PrestamosDAO prestamosDAO, LibrosDAO librosDAO) {
        this.prestamosDAO = prestamosDAO;
        this.librosDAO = librosDAO;
    }

    @FXML
    private Label lblLibro;

    @FXML
    private Label lbUnidadesPrestadas;

    @FXML
    private TableColumn<Prestamo, String> tbDocente;

    @FXML
    private TableColumn<Prestamo, String> tbMotivo;

    @FXML
    private TableView<Prestamo> tabla;

    @FXML
    private TableColumn<Prestamo, String> tbPrestamo;

    @FXML
    private TableColumn<Prestamo, String> tbLimite;

    @FXML
    private TableColumn<Prestamo, String> tbEstado;

    @FXML
    private TableColumn<Prestamo, String> tbEstudiante;

    @FXML
    private TableColumn<Prestamo, String> tbGrado;

    Prestamo prestamoSeleccionado;

    @FXML
    void clickDevolucion(ActionEvent event) {
        registrarDevolucion();
    }

    @FXML
    public void initialize() {
        configurarTabla();
    }

    private void cargarPrestamos(){
        if(libro == null){
            return;
        }

        tabla.getItems().setAll(prestamos);
        lbUnidadesPrestadas.setText("Unidades prestadas: " + prestamos.size());
    }

    private void registrarDevolucion(){
        if(tabla.getSelectionModel().getSelectedItem() == null){
            Alertas.mostrarError("Seleccione un prestamo para registrar su devoluciÃƒÂ³n");
            return;
        }

        prestamoSeleccionado = tabla.getSelectionModel().getSelectedItem();
        services.PrestamoService prestamoService = new services.PrestamoService();

        if(prestamoService.registrarDevolucion(prestamoSeleccionado, libro.getId())){
            if (Fechas.esDespues(Fechas.fechaActualISO(), prestamoSeleccionado.getFecha_limite())) {
                String fechaLimiteUI = Fechas.convertirAUI(prestamoSeleccionado.getFecha_limite());
                Alertas.mostrarInfo("Se registro la devoluciÃƒÂ³n correctamente. Sin embargo, fue devuelto fuera de tiempo, la fecha lÃƒÂ­mite era hasta: " + (fechaLimiteUI != null ? fechaLimiteUI : prestamoSeleccionado.getFecha_limite()));
            }else{
                Alertas.mostrarExito("Se registro correctamente la devoluciÃƒÂ³n y fue dentro de la fecha establecida.");
            }
            cerrar();
        } else {
            Alertas.mostrarError("OcurriÃƒÂ³ un error al registrar la devoluciÃƒÂ³n.");
        }
    }

    private void configurarTabla(){
        tbPrestamo.setCellValueFactory(data ->
                new SimpleStringProperty(formatearFechaUI(data.getValue().getFecha_prestamo()))
        );

        tbLimite.setCellValueFactory(data ->
                new SimpleStringProperty(formatearFechaUI(data.getValue().getFecha_limite()))
        );

        tbEstado.setCellValueFactory(data ->
                new SimpleStringProperty(("Prestado"))
        );

        tbEstudiante.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEstudiante())
        );

        tbGrado.setCellValueFactory(data ->
                new SimpleStringProperty(Integer.toString(data.getValue().getGrado()))
        );

        tbMotivo.setCellValueFactory(data -> {
            var motivo = data.getValue().getMotivoPrestamo();
            return new SimpleStringProperty(motivo != null ? motivo.getNombre() : "");
        });

        tbDocente.setCellValueFactory(data -> {
            var docente = data.getValue().getDocente();
            return new SimpleStringProperty(docente != null ? docente.getNombreCompleto() : "");
        });

        tabla.getColumns().forEach(col -> col.setReorderable(false));
    }

    private String formatearFechaUI(String fechaBD) {
        String fechaUI = Fechas.convertirAUI(fechaBD);
        return fechaUI != null ? fechaUI : fechaBD;
    }

    private void cerrar() {
        Stage stage = (Stage) lblLibro.getScene().getWindow();
        stage.close();
    }


}
