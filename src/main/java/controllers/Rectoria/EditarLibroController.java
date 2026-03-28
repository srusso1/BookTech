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
import utils.Validaciones;

import java.util.Map;

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

        if(comboBoxModificar.getSelectionModel().getSelectedItem().equals("Unidades")){
            if(!Validaciones.validarCampoNumerico(txtEditar)){
                Alertas.mostrarError("Sólo se admiten valores númericos al intentar modificar el campo 'Unidades'");
                return;
            }else if(Integer.parseInt(nuevoValor) < 0){
                Alertas.mostrarError("El valor no puede ser negativo");
                return;
            }
        }

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

                        Map<String, String> prompts = Map.of(
                                "Titulo", "Ingrese el nuevo título",
                                "Autor", "Ingrese el nuevo autor",
                                "Editorial", "Ingrese la nueva editorial",
                                "Ubicacion", "Ingrese la nueva ubicación",
                                "Categoria", "Ingrese la nueva categoría",
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
