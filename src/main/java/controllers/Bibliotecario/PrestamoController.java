package controllers.Bibliotecario;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Libro;
import utils.Validaciones;

public class PrestamoController {

    @FXML private Label lblLibro;
    @FXML private TextField txtEstudiante;
    @FXML private TextField txtDocumento;
    @FXML private DatePicker dpFechaDevolucion;

    private Libro libro;

    // 🔹 método para recibir el libro
    public void setLibro(Libro libro) {
        this.libro = libro;
        lblLibro.setText(libro.getTitulo() + " — " + libro.getAutor());
    }

    @FXML
    void clickRegistrar() {
        Validaciones.ocultarPopOver(txtEstudiante);
        Validaciones.ocultarPopOver(txtDocumento);
        Validaciones.ocultarPopOver(dpFechaDevolucion.getEditor());

        if(!Validaciones.campoRequerido(txtEstudiante)){
            return;
        }
        if(!Validaciones.campoRequerido(txtDocumento)){
            return;
        }
        if(dpFechaDevolucion.getValue() == null){
            Validaciones.agregarPopOver(dpFechaDevolucion.getEditor(), "Es necesario establecer una fecha");
            return;
        }


        System.out.println(dpFechaDevolucion.getValue());

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
}
