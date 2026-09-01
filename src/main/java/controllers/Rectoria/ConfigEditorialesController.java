package controllers.Rectoria;

import database.EditorialesDAO;
import model.Editorial;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.Alertas;
import java.util.List;

public class ConfigEditorialesController {
    @FXML private TableView<Editorial> tblEditoriales;
    @FXML private TableColumn<Editorial, String> colEditorialId, colEditorialNombre, colEditorialEstado;
    @FXML private TextField txtBuscarEditorial, txtEditorialNombre;
    @FXML private ComboBox<String> cbEditorialEstado;
    @FXML private Label lblEstadoEdicionEditorial;

    private final EditorialesDAO editorialesDAO;
    private Editorial editorialSeleccionada = null;

    public ConfigEditorialesController(EditorialesDAO editorialesDAO) { this.editorialesDAO = editorialesDAO; }

    @FXML void initialize() { configurarTablaEditoriales(); cargarEditoriales(); }

    private void configurarTablaEditoriales() {
        colEditorialId.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getId())));
        colEditorialNombre.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre));
        colEditorialEstado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));
        tblEditoriales.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            if (nuevo != null) { editorialSeleccionada = nuevo; txtEditorialNombre.setText(nuevo.getNombre()); cbEditorialEstado.setValue(nuevo.getEstado() == 1 ? "Activo" : "Inactivo"); lblEstadoEdicionEditorial.setText("Editando editorial seleccionada."); }
        });
        cbEditorialEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEditorialEstado.setValue("Activo");
        txtBuscarEditorial.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) { cargarEditoriales(); } else {
                String lowerCaseFilter = newVal.toLowerCase();
                List<Editorial> filtradas = editorialesDAO.obtenerTodas().stream().filter(e -> e.getNombre().toLowerCase().contains(lowerCaseFilter)).toList();
                tblEditoriales.setItems(FXCollections.observableArrayList(filtradas));
            }
        });
    }

    private void cargarEditoriales() { tblEditoriales.setItems(FXCollections.observableArrayList(editorialesDAO.obtenerTodas())); }

    @FXML void limpiarFormularioEditorial() { editorialSeleccionada = null; txtEditorialNombre.clear(); cbEditorialEstado.setValue("Activo"); lblEstadoEdicionEditorial.setText("Creando nueva editorial."); tblEditoriales.getSelectionModel().clearSelection(); }

    @FXML void guardarEditorial() {
        String nombre = txtEditorialNombre.getText();
        if (nombre == null || nombre.trim().isEmpty()) { Alertas.mostrarError("El nombre de la editorial es obligatorio."); return; }
        int estado = "Activo".equals(cbEditorialEstado.getValue()) ? 1 : 0;
        if (editorialSeleccionada == null) {
            Editorial nueva = new Editorial(0, nombre.trim().toUpperCase(), estado);
            if (editorialesDAO.insertarEditorial(nueva)) { Alertas.mostrarExito("Editorial creada exitosamente."); limpiarFormularioEditorial(); cargarEditoriales(); } else { Alertas.mostrarError("No se pudo crear la editorial (puede que ya exista)."); }
        } else {
            editorialSeleccionada.setNombre(nombre.trim().toUpperCase()); editorialSeleccionada.setEstado(estado);
            if (editorialesDAO.actualizarEditorial(editorialSeleccionada)) { Alertas.mostrarExito("Editorial actualizada exitosamente."); limpiarFormularioEditorial(); cargarEditoriales(); } else { Alertas.mostrarError("No se pudo actualizar la editorial."); }
        }
    }
}