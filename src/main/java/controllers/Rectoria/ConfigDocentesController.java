package controllers.Rectoria;

import database.DocentesDAO;
import model.Docente;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.Alertas;
import java.util.ArrayList;
import java.util.List;

public class ConfigDocentesController {
    @FXML private TableView<Docente> tblDocentes;
    @FXML private TableColumn<Docente, String> colDocId, colDocNombre1, colDocNombre2, colDocApellido1, colDocApellido2;
    @FXML private TextField txtBuscarDocente, txtDocNombre1, txtDocNombre2, txtDocApellido1, txtDocApellido2;
    @FXML private Label lblTituloFormDocente, lblEstadoEdicionDocente;
    @FXML private Button btnGuardarDocente, btnEliminarDocente, btnNuevoDocente;

    private final DocentesDAO docentesDAO;
    private final ArrayList<Docente> docentesBaseTabla = new ArrayList<>();
    private Docente docenteSeleccionado;

    public ConfigDocentesController(DocentesDAO docentesDAO) { this.docentesDAO = docentesDAO; }

    @FXML void initialize() { configurarTablaDocentes(); configurarFiltrosDocentes(); cargarDocentes(); }

    private void configurarTablaDocentes() {
        colDocId.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getId())));
        colDocNombre1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre_1));
        colDocNombre2.setCellValueFactory(cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getNombre_2())));
        colDocApellido1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getApellido_1));
        colDocApellido2.setCellValueFactory(cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getApellido_2())));
        tblDocentes.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            docenteSeleccionado = nuevo;
            if (nuevo == null) { limpiarFormularioDocente(); return; }
            txtDocNombre1.setText(valorSeguro(nuevo.getNombre_1()));
            txtDocNombre2.setText(valorSeguro(nuevo.getNombre_2()));
            txtDocApellido1.setText(valorSeguro(nuevo.getApellido_1()));
            txtDocApellido2.setText(valorSeguro(nuevo.getApellido_2()));
            if (lblTituloFormDocente != null) lblTituloFormDocente.setText("Editar Docente #" + nuevo.getId());
            if (lblEstadoEdicionDocente != null) lblEstadoEdicionDocente.setText("Modifique los datos y presione Actualizar.");
            if (btnGuardarDocente != null) btnGuardarDocente.setText("Actualizar docente");
            if (btnEliminarDocente != null) btnEliminarDocente.setDisable(false);
        });
    }

    private void configurarFiltrosDocentes() { if (txtBuscarDocente != null) txtBuscarDocente.textProperty().addListener((obs, oldText, newText) -> aplicarFiltroDocentes()); }

    private void cargarDocentes() { docentesBaseTabla.clear(); docentesBaseTabla.addAll(docentesDAO.obtenerDocentes()); aplicarFiltroDocentes(); }

    private void aplicarFiltroDocentes() {
        String busqueda = txtBuscarDocente == null || txtBuscarDocente.getText() == null ? "" : txtBuscarDocente.getText().trim().toLowerCase();
        List<Docente> filtrados = docentesBaseTabla.stream().filter(d -> busqueda.isEmpty() || d.getNombreCompleto().toLowerCase().contains(busqueda) || String.valueOf(d.getId()).contains(busqueda)).toList();
        tblDocentes.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML void clickGuardarDocente() {
        String n1 = txtDocNombre1.getText() != null ? txtDocNombre1.getText().trim().toUpperCase() : "";
        String n2 = txtDocNombre2.getText() != null ? txtDocNombre2.getText().trim().toUpperCase() : "";
        String a1 = txtDocApellido1.getText() != null ? txtDocApellido1.getText().trim().toUpperCase() : "";
        String a2 = txtDocApellido2.getText() != null ? txtDocApellido2.getText().trim().toUpperCase() : "";
        if (n1.isEmpty() || a1.isEmpty()) { Alertas.mostrarWarning("El primer nombre y el primer apellido son obligatorios."); return; }
        if (docenteSeleccionado == null) {
            Docente nuevo = new Docente(0, n1, n2, a1, a2);
            if (docentesDAO.insertarDocente(nuevo)) { Alertas.mostrarExito("Docente registrado exitosamente."); cargarDocentes(); limpiarFormularioDocente(); } else { Alertas.mostrarError("No se pudo registrar el docente."); }
        } else {
            docenteSeleccionado.setNombre_1(n1); docenteSeleccionado.setNombre_2(n2); docenteSeleccionado.setApellido_1(a1); docenteSeleccionado.setApellido_2(a2);
            if (docentesDAO.actualizarDocente(docenteSeleccionado)) { Alertas.mostrarExito("Docente actualizado exitosamente."); cargarDocentes(); limpiarFormularioDocente(); } else { Alertas.mostrarError("No se pudo actualizar el docente."); }
        }
    }

    @FXML void clickEliminarDocente() {
        if (docenteSeleccionado == null) { Alertas.mostrarWarning("Seleccione un docente de la tabla para eliminar."); return; }
        if (docentesDAO.docenteTieneRegistros(docenteSeleccionado.getId())) { Alertas.mostrarWarning("No es posible eliminar al docente '" + docenteSeleccionado.getNombreCompleto() + "' porque tiene prestamos o sesiones de biblioteca virtual vinculadas."); return; }
        boolean confirma = Alertas.mostrarConfirmacion("Esta seguro de eliminar al docente '" + docenteSeleccionado.getNombreCompleto() + "'?");
        if (!confirma) return;
        if (docentesDAO.eliminarDocente(docenteSeleccionado.getId())) { Alertas.mostrarExito("Docente eliminado exitosamente."); cargarDocentes(); limpiarFormularioDocente(); } else { Alertas.mostrarError("No se pudo eliminar el docente."); }
    }

    @FXML void clickNuevoDocente() { if (tblDocentes != null) tblDocentes.getSelectionModel().clearSelection(); limpiarFormularioDocente(); }

    private void limpiarFormularioDocente() {
        docenteSeleccionado = null;
        if (txtDocNombre1 != null) txtDocNombre1.clear(); if (txtDocNombre2 != null) txtDocNombre2.clear();
        if (txtDocApellido1 != null) txtDocApellido1.clear(); if (txtDocApellido2 != null) txtDocApellido2.clear();
        if (lblTituloFormDocente != null) lblTituloFormDocente.setText("Gestion de Docente");
        if (lblEstadoEdicionDocente != null) lblEstadoEdicionDocente.setText("Complete los datos para registrar o seleccione uno de la tabla para editar.");
        if (btnGuardarDocente != null) btnGuardarDocente.setText("Guardar nuevo docente");
        if (btnEliminarDocente != null) btnEliminarDocente.setDisable(true);
    }

    private String valorSeguro(String valor) { return valor == null ? "" : valor; }
}