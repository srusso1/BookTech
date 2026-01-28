package controllers.Bibliotecario;

import database.LibrosDAO;
import database.PrestamosDAO;
import javafx.beans.property.SimpleStringProperty;
import model.Libro;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Prestamo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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


    @FXML
    void clickCancelar(ActionEvent event) {

    }

    @FXML
    void clickDevolucion(ActionEvent event) {

    }

    @FXML
    public void initialize() {

        tbPrestamo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFecha_prestamo())
        );

        tbLimite.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFecha_limite())
        );

        tbEstado.setCellValueFactory(data ->
            new SimpleStringProperty(Integer.toString(data.getValue().getEstado()))
        );

        tbEstudiante.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEstudiante())
        );

        tbGrado.setCellValueFactory(data ->
                new SimpleStringProperty(Integer.toString(data.getValue().getGrado()))
        );
    }

    private void cargarPrestamos(){
        if(libro == null){
            return;
        }

        tabla.getItems().setAll(prestamos);
    }


}
