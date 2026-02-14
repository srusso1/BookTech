package controllers.Rectoria;


import database.LibrosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Libro;
import utils.Alertas;
import utils.Validaciones;

public class RegistrarLibroController {

    @FXML
    private Label lblLibro;

    @FXML
    private TextField txtAutor;

    @FXML
    private TextField txtCategoria;

    @FXML
    private TextField txtEditorial;

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextField txtUbicacion;

    @FXML
    private TextField txtUnidades;

    LibrosDAO librosDAO = new LibrosDAO();

    @FXML
    void clickRegistrar(ActionEvent event) {

        ocultarAlertas();

        if(!Validaciones.campoRequerido(txtTitulo)){
            return;
        }

        if(!Validaciones.campoRequerido(txtUbicacion)){
            return;
        }

        if(!Validaciones.campoRequerido(txtCategoria)){
            return;
        }
        if(!Validaciones.campoRequerido(txtEditorial)){
            return;
        }

        if(!Validaciones.campoRequerido(txtAutor)){
            return;
        }

        if(!Validaciones.campoRequerido(txtUnidades)){
            return;
        }

        if(!Validaciones.validarCampoNumerico(txtUnidades)){
            Alertas.mostrarError("Únicamente se permiten valores numéricos en el campo de unidades");
            return;
        }

        String titulo = txtTitulo.getText().toUpperCase();
        String autor = txtAutor.getText().toUpperCase();
        String editorial = txtEditorial.getText().toUpperCase();
        String categoria = txtCategoria.getText().toUpperCase();
        String ubicacion = txtUbicacion.getText().toUpperCase();
        int unidades = Integer.parseInt(txtUnidades.getText());

        Libro libro = new Libro(titulo, ubicacion, categoria, editorial, autor, unidades);
        if(librosDAO.registrarLibro(libro)){
            Alertas.mostrarExito("Se registro correctamente el libro");
        }

        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) lblLibro.getScene().getWindow();
        stage.close();
    }

    private void ocultarAlertas(){
        Validaciones.ocultarPopOver(txtTitulo);
        Validaciones.ocultarPopOver(txtAutor);
        Validaciones.ocultarPopOver(txtEditorial);
        Validaciones.ocultarPopOver(txtCategoria);
        Validaciones.ocultarPopOver(txtUbicacion);
        Validaciones.ocultarPopOver(txtUnidades);
    }

}