package controllers.Rectoria;

import database.MotivosPrestamoDAO;
import database.MotivosPlataformaDAO;
import model.MotivoPrestamo;
import model.MotivoPlataforma;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.Alertas;

public class ConfigMotivosController implements utils.Refrescable {
    
    @Override
    public void refresh() {
        cargarMotivosPrestamo();
        cargarMotivosPlataforma();
    }

    @FXML private TableView<MotivoPrestamo> tblMotivosPrestamo;
    @FXML private TableColumn<MotivoPrestamo, String> colPrestamoNombre, colPrestamoEstado;
    @FXML private TextField txtNuevoMotivoPrestamo;
    @FXML private TableView<MotivoPlataforma> tblMotivosPlataforma;
    @FXML private TableColumn<MotivoPlataforma, String> colPlataformaNombre, colPlataformaEstado;
    @FXML private TextField txtNuevoMotivoPlataforma;

    private final MotivosPrestamoDAO motivosPrestamoDAO;
    private final MotivosPlataformaDAO motivosPlataformaDAO;
    private MotivoPrestamo motivoPrestamoSeleccionado;
    private MotivoPlataforma motivoPlataformaSeleccionado;

    public ConfigMotivosController(MotivosPrestamoDAO motivosPrestamoDAO, MotivosPlataformaDAO motivosPlataformaDAO) {
        this.motivosPrestamoDAO = motivosPrestamoDAO;
        this.motivosPlataformaDAO = motivosPlataformaDAO;
    }

    @FXML void initialize() { configurarTablaMotivos(); cargarMotivosPrestamo(); cargarMotivosPlataforma(); }

    private void configurarTablaMotivos() {
        colPrestamoNombre.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre));
        colPrestamoEstado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));
        colPlataformaNombre.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre));
        colPlataformaEstado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));
        tblMotivosPrestamo.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> motivoPrestamoSeleccionado = nuevo);
        tblMotivosPlataforma.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> motivoPlataformaSeleccionado = nuevo);
    }

    private void cargarMotivosPrestamo() {
        tblMotivosPrestamo.setPlaceholder(new Label("Cargando..."));
        java.util.concurrent.CompletableFuture.supplyAsync(motivosPrestamoDAO::obtenerTodosMotivosPrestamo)
            .thenAcceptAsync(lista -> {
                tblMotivosPrestamo.setItems(FXCollections.observableArrayList(lista));
                if (lista.isEmpty()) tblMotivosPrestamo.setPlaceholder(new Label("No hay motivos"));
            }, javafx.application.Platform::runLater);
    }
    
    private void cargarMotivosPlataforma() {
        tblMotivosPlataforma.setPlaceholder(new Label("Cargando..."));
        java.util.concurrent.CompletableFuture.supplyAsync(motivosPlataformaDAO::obtenerTodosMotivosPlataforma)
            .thenAcceptAsync(lista -> {
                tblMotivosPlataforma.setItems(FXCollections.observableArrayList(lista));
                if (lista.isEmpty()) tblMotivosPlataforma.setPlaceholder(new Label("No hay motivos"));
            }, javafx.application.Platform::runLater);
    }

    @FXML void clickAgregarMotivoPrestamo() {
        String nombre = txtNuevoMotivoPrestamo.getText();
        if (nombre == null || nombre.trim().isEmpty()) { Alertas.mostrarError("Debe ingresar el nombre del motivo de prestamo"); return; }
        if (motivosPrestamoDAO.agregarMotivoPrestamo(nombre)) { txtNuevoMotivoPrestamo.clear(); cargarMotivosPrestamo(); Alertas.mostrarExito("Motivo de prestamo registrado"); }
    }

    @FXML void clickAlternarEstadoMotivoPrestamo() {
        if (motivoPrestamoSeleccionado == null) { Alertas.mostrarError("Seleccione un motivo de prestamo"); return; }
        int nuevoEstado = motivoPrestamoSeleccionado.getEstado() == 1 ? 0 : 1;
        if (motivosPrestamoDAO.actualizarEstadoMotivoPrestamo(motivoPrestamoSeleccionado.getId(), nuevoEstado)) { cargarMotivosPrestamo(); Alertas.mostrarExito("Estado actualizado correctamente"); }
    }

    @FXML void clickAgregarMotivoPlataforma() {
        String nombre = txtNuevoMotivoPlataforma.getText();
        if (nombre == null || nombre.trim().isEmpty()) { Alertas.mostrarError("Debe ingresar el nombre del motivo de plataforma"); return; }
        if (motivosPlataformaDAO.agregarMotivoPlataforma(nombre)) { txtNuevoMotivoPlataforma.clear(); cargarMotivosPlataforma(); Alertas.mostrarExito("Motivo de plataforma registrado"); }
    }

    @FXML void clickAlternarEstadoMotivoPlataforma() {
        if (motivoPlataformaSeleccionado == null) { Alertas.mostrarError("Seleccione un motivo de plataforma"); return; }
        int nuevoEstado = motivoPlataformaSeleccionado.getEstado() == 1 ? 0 : 1;
        if (motivosPlataformaDAO.actualizarEstadoMotivoPlataforma(motivoPlataformaSeleccionado.getId(), nuevoEstado)) { cargarMotivosPlataforma(); Alertas.mostrarExito("Estado actualizado correctamente"); }
    }
}