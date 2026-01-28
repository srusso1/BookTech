package controllers.Bibliotecario;

import database.LibrosDAO;
import database.PrestamosDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Libro;
import utils.Alertas;
import utils.Fechas;
import utils.Validaciones;


public class PrestamoController {

    @FXML private Label lblLibro;
    @FXML private TextField txtEstudiante;
    @FXML private DatePicker dpFechaDevolucion;
    @FXML private ComboBox<Integer> comboBoxGrados;

    private Libro libro;
    PrestamosDAO prestamosDAO = new PrestamosDAO();
    LibrosDAO librosDAO = new LibrosDAO();

    // 🔹 método para recibir el libro
    public void setLibro(Libro libro) {
        this.libro = libro;
        lblLibro.setText(libro.getTitulo() + " — " + libro.getAutor());
    }

    @FXML
    void clickRegistrar() {
        Validaciones.ocultarPopOver(txtEstudiante);
        Validaciones.ocultarPopOver(dpFechaDevolucion.getEditor());

        if(!Validaciones.campoRequerido(txtEstudiante)){
            return;
        }

        if(comboBoxGrados.getValue() == null){
            Alertas.mostrarError("Es necesario establecer un grado");
            return;
        }

        if(dpFechaDevolucion.getValue() == null){
            Alertas.mostrarError("Es necesario establecer una fecha límite de devolución");
            return;
        }

        String fechaDevolucion = dpFechaDevolucion.getValue().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String fechaHoy = Fechas.fechaActual();
        int idLibro = libro.getId();
        String estudiante = txtEstudiante.getText().toUpperCase();
        int grado = comboBoxGrados.getValue();


        if(prestamosDAO.registrarPrestamo(idLibro, estudiante, grado, fechaHoy, fechaDevolucion)){
            librosDAO.disminuirUnidadLibro(idLibro);
            Alertas.mostrarExito("Se registro correctamente el prestamo");
        }else{
            Alertas.mostrarError("Error al registrar el prestamo");
        }

        System.out.println("Préstamo registrado de: " + libro.getTitulo());
        cerrar();
    }

    @FXML
    void clickCancelar() {
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) lblLibro.getScene().getWindow();
        stage.close();
    }

    @FXML
    void initialize() {
        dpFechaDevolucion.setEditable(false);
        comboBoxGrados.getItems().addAll(6, 7, 8, 9, 10);
    }
}
