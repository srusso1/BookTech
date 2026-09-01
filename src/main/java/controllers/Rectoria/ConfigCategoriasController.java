package controllers.Rectoria;

import database.CategoriasDAO;
import model.Categoria;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.Alertas;
import java.util.List;

public class ConfigCategoriasController {
    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, String> colCategoriaId, colCategoriaNombre, colCategoriaEstado;
    @FXML private TextField txtBuscarCategoria, txtCategoriaNombre;
    @FXML private ComboBox<String> cbCategoriaEstado;
    @FXML private Label lblEstadoEdicionCategoria;

    private final CategoriasDAO categoriasDAO;
    private Categoria categoriaSeleccionada = null;

    public ConfigCategoriasController(CategoriasDAO categoriasDAO) { this.categoriasDAO = categoriasDAO; }

    @FXML void initialize() { configurarTablaCategorias(); cargarCategorias(); }

    private void configurarTablaCategorias() {
        colCategoriaId.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getId())));
        colCategoriaNombre.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombreCategoria));
        colCategoriaEstado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));
        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            if (nuevo != null) { categoriaSeleccionada = nuevo; txtCategoriaNombre.setText(nuevo.getNombreCategoria()); cbCategoriaEstado.setValue(nuevo.getEstado() == 1 ? "Activo" : "Inactivo"); lblEstadoEdicionCategoria.setText("Editando categoria seleccionada."); }
        });
        cbCategoriaEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbCategoriaEstado.setValue("Activo");
        txtBuscarCategoria.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) { cargarCategorias(); } else {
                String lowerCaseFilter = newVal.toLowerCase();
                List<Categoria> filtradas = categoriasDAO.obtenerTodas().stream().filter(c -> c.getNombreCategoria().toLowerCase().contains(lowerCaseFilter)).toList();
                tblCategorias.setItems(FXCollections.observableArrayList(filtradas));
            }
        });
    }

    private void cargarCategorias() { tblCategorias.setItems(FXCollections.observableArrayList(categoriasDAO.obtenerTodas())); }

    @FXML void limpiarFormularioCategoria() { categoriaSeleccionada = null; txtCategoriaNombre.clear(); cbCategoriaEstado.setValue("Activo"); lblEstadoEdicionCategoria.setText("Creando nueva categoria."); tblCategorias.getSelectionModel().clearSelection(); }

    @FXML void guardarCategoria() {
        String nombre = txtCategoriaNombre.getText();
        if (nombre == null || nombre.trim().isEmpty()) { Alertas.mostrarError("El nombre de la categoria es obligatorio."); return; }
        int estado = "Activo".equals(cbCategoriaEstado.getValue()) ? 1 : 0;
        if (categoriaSeleccionada == null) {
            Categoria nueva = new Categoria(0, nombre.trim().toUpperCase(), estado);
            if (categoriasDAO.insertarCategoria(nueva)) { Alertas.mostrarExito("Categoria creada exitosamente."); limpiarFormularioCategoria(); cargarCategorias(); } else { Alertas.mostrarError("No se pudo crear la categoria."); }
        } else {
            categoriaSeleccionada.setNombreCategoria(nombre.trim().toUpperCase()); categoriaSeleccionada.setEstado(estado);
            if (categoriasDAO.actualizarCategoria(categoriaSeleccionada)) { Alertas.mostrarExito("Categoria actualizada exitosamente."); limpiarFormularioCategoria(); cargarCategorias(); } else { Alertas.mostrarError("No se pudo actualizar la categoria."); }
        }
    }
}