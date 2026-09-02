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

public class ConfigDocentesController implements utils.Refrescable {
    
    @Override
    public void refresh() {
        cargarDocentes();
    }
    
    @FXML private TableView<Docente> tblDocentes;
    @FXML private TableColumn<Docente, String> colDocId, colDocNombre1, colDocNombre2, colDocApellido1, colDocApellido2;
    @FXML private TextField txtBuscarDocente, txtDocNombre1, txtDocNombre2, txtDocApellido1, txtDocApellido2;
    @FXML private Label lblTituloFormDocente, lblEstadoEdicionDocente;
    @FXML private Button btnGuardarDocente, btnEliminarDocente, btnNuevoDocente;

    private final DocentesDAO docentesDAO;

    @FXML private ComboBox<Integer> cbTamanioPagina;
    @FXML private Label lblInfoPaginacion;
    @FXML private Button btnAnterior, btnSiguiente;
    
    private int paginaActual = 1;
    private int totalPaginas = 1;
    private int registrosPorPagina = 20;

    private final ArrayList<Docente> docentesBaseTabla = new ArrayList<>();
    private Docente docenteSeleccionado;
    private final utils.Debouncer debouncer = new utils.Debouncer(300);

    public ConfigDocentesController(DocentesDAO docentesDAO) { this.docentesDAO = docentesDAO; }

    @FXML void initialize() { 
        if (btnAnterior != null) btnAnterior.setTooltip(new Tooltip("Página anterior"));
        if (btnSiguiente != null) btnSiguiente.setTooltip(new Tooltip("Página siguiente"));
        configurarTablaDocentes();
        configurarPaginacion(); configurarFiltrosDocentes(); cargarDocentes(); 
    }

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

    private void configurarFiltrosDocentes() { if (txtBuscarDocente != null) txtBuscarDocente.textProperty().addListener((obs, oldText, newText) -> { paginaActual = 1; debouncer.debounce(this::cargarDocentes); }); }

    
    
    @FXML
    void clickPaginaAnterior() {
        if (paginaActual > 1) {
            paginaActual--;
            cargarDocentes();
        }
    }

    @FXML
    void clickPaginaSiguiente() {
        if (paginaActual < totalPaginas) {
            paginaActual++;
            cargarDocentes();
        }
    }

    private void configurarPaginacion() {
        cbTamanioPagina.setItems(javafx.collections.FXCollections.observableArrayList(20, 50, 100));
        cbTamanioPagina.setValue(registrosPorPagina);
        cbTamanioPagina.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                registrosPorPagina = newVal;
                paginaActual = 1;
                cargarDocentes();
            }
        });
    }

    private void cargarDocentes() {
        String busqueda = txtBuscarDocente.getText();
        int offset = (paginaActual - 1) * registrosPorPagina;
        int registros = registrosPorPagina;
        
        tblDocentes.setPlaceholder(new Label("Cargando..."));
        
        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            java.util.List<model.Docente> docentes = docentesDAO.obtenerPaginados(registros, offset, busqueda);
            int totalRegistros = docentesDAO.contarTotal(busqueda);
            return new Object[]{docentes, totalRegistros};
        }).thenAcceptAsync(result -> {
            @SuppressWarnings("unchecked")
            java.util.List<model.Docente> docentes = (java.util.List<model.Docente>) result[0];
            int totalRegistros = (int) result[1];
            
            totalPaginas = (int) Math.ceil((double) totalRegistros / registros);
            if (totalPaginas == 0) totalPaginas = 1;
            
            lblInfoPaginacion.setText("Página " + paginaActual + " de " + totalPaginas + " (Total: " + totalRegistros + ")");
            btnAnterior.setDisable(paginaActual <= 1);
            btnSiguiente.setDisable(paginaActual >= totalPaginas);

            tblDocentes.setItems(javafx.collections.FXCollections.observableArrayList(docentes));
            
            if (docentes.isEmpty()) {
                tblDocentes.setPlaceholder(new Label("No hay docentes que coincidan"));
            }
        }, javafx.application.Platform::runLater);
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