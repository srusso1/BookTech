package controllers.Bibliotecario;

import database.LibrosDAO;
import database.PrestamosDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import model.Libro;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Prestamo;
import utils.Alertas;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static utils.Fechas.esDespues;
import static utils.Fechas.fechaActual;

public class DevolucionController {

    private Libro libro;
    private ArrayList<Prestamo> prestamos;
    PrestamosDAO prestamosDAO = new PrestamosDAO();
    LibrosDAO librosDAO = new LibrosDAO();

    // 🔹 método para recibir el libro
    public void setLibro(Libro libro) {
        this.libro = libro;
        lblLibro.setText(libro.getTitulo() + " — " + libro.getAutor());
    }

    public void setPrestamos(ArrayList<Prestamo> prestamos) {
        this.prestamos = prestamos;
        cargarPrestamos();
    }

    @FXML
    private Label lblLibro;

    @FXML
    private Label lbUnidadesPrestadas;

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
            Alertas.mostrarError("Seleccione un prestamo para registrar su devolución");
            return;
        }

        prestamoSeleccionado = tabla.getSelectionModel().getSelectedItem();
        if(prestamosDAO.registrarDevolucion(prestamoSeleccionado)){
            if (esDespues(fechaActual(), prestamoSeleccionado.getFecha_limite())) {
                Alertas.mostrarInfo("Se registro la devolución correctamente. Sin embargo, fue devuelto fuera de tiempo, la fecha límite era hasta: " + prestamoSeleccionado.getFecha_limite());
            }else{
                Alertas.mostrarExito("Se registro correctamente la devolución y fue dentro de la fecha establecida.");
            }
            librosDAO.aumentarUnidadLibro(libro.getId());
            cerrar();
        }
    }

    private void configurarTabla(){
        tbPrestamo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFecha_prestamo())
        );

        tbLimite.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFecha_limite())
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

        tabla.getColumns().forEach(col -> col.setReorderable(false));
    }

    private void cerrar() {
        Stage stage = (Stage) lblLibro.getScene().getWindow();
        stage.close();
    }


}
