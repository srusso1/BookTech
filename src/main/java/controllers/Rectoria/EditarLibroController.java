package controllers.Rectoria;

import database.CategoriasDAO;
import database.EditorialesDAO;
import database.LibrosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Categoria;
import model.Editorial;
import model.Libro;
import utils.Alertas;
import utils.Validaciones;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditarLibroController {

    private Libro libro;

    public void setLibro(Libro libro) {
        this.libro = libro;
        cargarDatos();
    }

    public EditarLibroController(LibrosDAO librosDAO, CategoriasDAO categoriasDAO, EditorialesDAO editorialesDAO) {
        this.librosDAO = librosDAO;
        this.categoriasDAO = categoriasDAO;
        this.editorialesDAO = editorialesDAO;
    }

    @FXML
    private ComboBox<String> comboBoxModificar;

    @FXML
    private ComboBox<Categoria> comboCategorias;

    @FXML
    private ComboBox<Editorial> comboEditoriales;

    @FXML
    private Label lblAutor;

    @FXML
    private Label lblCategoria;

    @FXML
    private Label lblEditorial;

    @FXML
    private Label lblLibro;

    @FXML
    private Label lblUbicacion;

    @FXML
    private Label lblUnidades;

    @FXML
    private TextField txtEditar;

    private final LibrosDAO librosDAO;
    private final CategoriasDAO categoriasDAO;
    private final EditorialesDAO editorialesDAO;

    @FXML
    void clickRegistrar(ActionEvent event) {
        String campo = comboBoxModificar.getSelectionModel().getSelectedItem();

        if(campo == null){
            Alertas.mostrarError("Seleccione el campo a editar");
            return;
        }

        String nuevoValor;

        if(campo.equals("Categoria")){
            Categoria categoriaSeleccionada = comboCategorias.getSelectionModel().getSelectedItem();
            if(categoriaSeleccionada == null){
                Alertas.mostrarError("Seleccione una categoria");
                return;
            }
            nuevoValor = String.valueOf(categoriaSeleccionada.getId());
        } else if(campo.equals("Editorial")) {
            Editorial editorialSeleccionada = comboEditoriales.getSelectionModel().getSelectedItem();
            if(editorialSeleccionada == null){
                Alertas.mostrarError("Seleccione una editorial");
                return;
            }
            nuevoValor = String.valueOf(editorialSeleccionada.getId());
        } else {
            if(txtEditar.getText().isEmpty()){
                Alertas.mostrarError("Ingrese el nuevo valor");
                return;
            }
            nuevoValor = txtEditar.getText();
        }

        if(campo.equals("Unidades")){
            if(!Validaciones.validarCampoNumerico(txtEditar)){
                Alertas.mostrarError("Solo se admiten valores numericos al intentar modificar el campo 'Unidades'");
                return;
            }else if(Integer.parseInt(nuevoValor) < 0){
                Alertas.mostrarError("El valor no puede ser negativo");
                return;
            }
        }

        String valorVisible = nuevoValor;
        if(campo.equals("Categoria")){
            Categoria categoriaSeleccionada = comboCategorias.getSelectionModel().getSelectedItem();
            valorVisible = categoriaSeleccionada != null ? categoriaSeleccionada.getNombreCategoria() : "";
        } else if(campo.equals("Editorial")){
            Editorial editorialSeleccionada = comboEditoriales.getSelectionModel().getSelectedItem();
            valorVisible = editorialSeleccionada != null ? editorialSeleccionada.getNombre() : "";
        }

        boolean ok = Alertas.mostrarConfirmacion("Estas seguro de modificar el " + campo + "? Se cambiara por '" + valorVisible + "'");
        if(ok){
            if(librosDAO.editarLibro(libro, campo, nuevoValor)){
                Alertas.mostrarExito("Se actualizo correctamente el libro");
            }
        }

        cerrar();
    }

    @FXML
    void initialize() {

        cargarCategorias();
        cargarEditoriales();
        ocultarElementos();

        comboBoxModificar.getItems().addAll(
                "Titulo", "Autor", "Editorial",
                "Ubicacion", "Categoria", "Unidades"
        );

        comboBoxModificar.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal == null) {
                        return;
                    }

                    if (newVal.equals("Categoria")) {
                        mostrarComboCategorias();
                        txtEditar.setPromptText("");
                    } else if (newVal.equals("Editorial")) {
                        mostrarComboEditoriales();
                        txtEditar.setPromptText("");
                    } else {
                        mostrarCampoTexto();

                        Map<String, String> prompts = Map.of(
                                "Titulo", "Ingrese el nuevo titulo",
                                "Autor", "Ingrese el nuevo autor",
                                "Ubicacion", "Ingrese la nueva ubicacion",
                                "Unidades", "Ingrese la nueva cantidad de unidades"
                        );

                        txtEditar.setPromptText(prompts.getOrDefault(
                                newVal,
                                "Ingrese el nuevo valor"
                        ));
                    }
                }
        );
    }

    private void cargarCategorias() {
        List<Categoria> categorias = categoriasDAO.obtenerCategorias();
        comboCategorias.getItems().setAll(categorias);
    }
    
    private void cargarEditoriales() {
        List<Editorial> editoriales = editorialesDAO.obtenerEditorialesActivas();
        comboEditoriales.getItems().setAll(editoriales);
    }

    private void ocultarElementos() {
        txtEditar.setVisible(false);
        txtEditar.setManaged(false);
        comboCategorias.setVisible(false);
        comboCategorias.setManaged(false);
        comboEditoriales.setVisible(false);
        comboEditoriales.setManaged(false);
    }

    private void mostrarCampoTexto(){
        txtEditar.setVisible(true);
        txtEditar.setManaged(true);
        comboCategorias.setVisible(false);
        comboCategorias.setManaged(false);
        comboEditoriales.setVisible(false);
        comboEditoriales.setManaged(false);
    }

    private void mostrarComboCategorias(){
        txtEditar.clear();
        txtEditar.setVisible(false);
        txtEditar.setManaged(false);
        comboEditoriales.setVisible(false);
        comboEditoriales.setManaged(false);
        comboCategorias.setVisible(true);
        comboCategorias.setManaged(true);
    }

    private void mostrarComboEditoriales(){
        txtEditar.clear();
        txtEditar.setVisible(false);
        txtEditar.setManaged(false);
        comboCategorias.setVisible(false);
        comboCategorias.setManaged(false);
        comboEditoriales.setVisible(true);
        comboEditoriales.setManaged(true);
    }

    private void cargarDatos() {
        if (libro == null) return;

        lblLibro.setText(libro.getTitulo());
        lblAutor.setText(libro.getAutor());
        lblEditorial.setText(libro.getEditorial());
        lblUbicacion.setText(libro.getUbicacion());
        lblCategoria.setText(libro.getCategoria());
        lblUnidades.setText(String.valueOf(libro.getUnidades()));
    }

    private void cerrar() {
        Stage stage = (Stage) lblLibro.getScene().getWindow();
        stage.close();
    }
}