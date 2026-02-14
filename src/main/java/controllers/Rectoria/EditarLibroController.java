package controllers.Rectoria;

import database.LibrosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Libro;
import utils.Alertas;

public class EditarLibroController {

    private Libro libro;

    public void setLibro(Libro libro) {
        this.libro = libro;
        cargarDatos();
    }

    @FXML
    private ComboBox<String> comboBoxModificar;

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

    LibrosDAO librosDAO = new LibrosDAO();

    @FXML
    void clickRegistrar(ActionEvent event) {
        if(comboBoxModificar.getSelectionModel().getSelectedItem() == null){
            Alertas.mostrarError("Seleccione el campo a editar");
            return;
        }
        if(txtEditar.getText().isEmpty()){
            Alertas.mostrarError("Ingrese el nuevo valor");
            return;
        }

        String campo = comboBoxModificar.getSelectionModel().getSelectedItem();
        String nuevoValor = txtEditar.getText();

        boolean ok = Alertas.mostrarConfirmacion("¿Estas seguro de modificar el " + campo + "? Se cambiará por '" + nuevoValor + "'");
        if(ok){
            if(librosDAO.editarLibro(libro, campo, nuevoValor)){
                Alertas.mostrarExito("Se actualizó correctamente el libro");
            }

        }

        cerrar();

    }

    @FXML
    void initialize() {

        ocultarElementos();

        comboBoxModificar.getItems().addAll(
                "Titulo", "Autor", "Editorial",
                "Ubicacion", "Categoria", "Unidades"
        );

        comboBoxModificar.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        mostrarElementos();
                        txtEditar.setPromptText("Ingrese el nuevo " + newVal);
                    }
                }
        );
    }

    private void ocultarElementos() {
        txtEditar.setVisible(false);
        txtEditar.setManaged(false);
    }

    private void mostrarElementos(){
        txtEditar.setVisible(true);
        txtEditar.setManaged(true);
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
