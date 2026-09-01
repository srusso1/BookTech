package controllers.Rectoria;


import database.CategoriasDAO;
import database.LibrosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Categoria;
import model.Libro;
import model.Editorial;
import database.EditorialesDAO;
import utils.Alertas;
import utils.Validaciones;

import java.util.ArrayList;
import java.util.List;

public class RegistrarLibroController {

    @FXML
    private Label lblLibro;

    @FXML
    private TextField txtAutor;

    @FXML
    private ComboBox<Categoria> comboBoxCategoria;

    @FXML
    private TextField txtCategoria;

    @FXML
    private ComboBox<Editorial> comboBoxEditorial;

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextField txtUbicacion;

    @FXML
    private TextField txtUnidades;

    LibrosDAO librosDAO = new LibrosDAO();
    CategoriasDAO categoriasDAO = new CategoriasDAO();
    EditorialesDAO editorialesDAO = new EditorialesDAO();

    @FXML
    void clickRegistrar(ActionEvent event) {

        ocultarAlertas();

        if(!Validaciones.campoRequerido(txtTitulo)){
            return;
        }

        if(!Validaciones.campoRequerido(txtUbicacion)){
            return;
        }

        if(comboBoxCategoria.getSelectionModel().getSelectedItem() == null){
            Alertas.mostrarError("Es obligatorio elegir una categoria");
            return;
        }

        if(comboBoxEditorial.getSelectionModel().getSelectedItem() == null){
            Alertas.mostrarError("Es obligatorio elegir una editorial");
            return;
        }

        if(!Validaciones.campoRequerido(txtAutor)){
            return;
        }

        if(!Validaciones.campoRequerido(txtUnidades)){
            return;
        }

        if(!Validaciones.validarCampoNumerico(txtUnidades)){
            Alertas.mostrarError("Ãšnicamente se permiten valores numÃ©ricos en el campo de unidades");
            return;
        }

        String titulo = txtTitulo.getText().toUpperCase();
        String autor = txtAutor.getText().toUpperCase();
        Editorial editorialObj = comboBoxEditorial.getSelectionModel().getSelectedItem();
        Categoria categoria = comboBoxCategoria.getSelectionModel().getSelectedItem();
        int id_categoria = categoria.getId();
        String ubicacion = txtUbicacion.getText().toUpperCase();
        int unidades = Integer.parseInt(txtUnidades.getText());

        Libro libro = new Libro(titulo, ubicacion, id_categoria, editorialObj.getId(), autor, unidades);
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
        Validaciones.ocultarPopOver(txtCategoria);
        Validaciones.ocultarPopOver(txtUbicacion);
        Validaciones.ocultarPopOver(txtUnidades);
    }

    @FXML
    void initialize() {
        List<Categoria> listaCategorias = categoriasDAO.obtenerCategorias();
        comboBoxCategoria.getItems().addAll(listaCategorias);
        
        List<Editorial> listaEditoriales = editorialesDAO.obtenerEditorialesActivas();
        comboBoxEditorial.getItems().addAll(listaEditoriales);
    }

}