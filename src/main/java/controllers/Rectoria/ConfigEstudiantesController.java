package controllers.Rectoria;

import database.EstudiantesDAO;
import model.Estudiante;
import services.EstudianteService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import utils.Alertas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigEstudiantesController {

    @FXML private TableView<Estudiante> tblEstudiantes;
    @FXML private TableColumn<Estudiante, String> colEstIdentificacion, colEstGrado, colEstApellido1, colEstApellido2, colEstNombre1, colEstNombre2, colEstGenero;
    @FXML private ComboBox<String> cbFiltroGrado;
    @FXML private TextField txtBuscarEstudiante, txtIdentificacion, txtGrado, txtApellido1, txtApellido2, txtNombre1, txtNombre2, txtGenero;
    @FXML private Label lblResumenCsv;
    @FXML private Button btnGuardarCambiosEstudiante, btnDescartarCsv;

    private final EstudiantesDAO estudiantesDAO;
    private final ArrayList<Estudiante> estudiantesPendientesCsv = new ArrayList<>();
    private final ArrayList<Estudiante> estudiantesBaseTabla = new ArrayList<>();
    private Estudiante estudianteSeleccionado;
    private boolean hayCsvPendiente = false;

    public ConfigEstudiantesController(EstudiantesDAO estudiantesDAO) {
        this.estudiantesDAO = estudiantesDAO;
    }

    @FXML
    void initialize() {
        configurarTablaEstudiantes();
        configurarFiltrosEstudiantes();
        cargarEstudiantes();
        actualizarEstadoBotonGuardado();
    }

    private void configurarTablaEstudiantes() {
        colEstIdentificacion.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getIdentificacion())));
        colEstGrado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getGrado())));
        colEstApellido1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getApellido_1));
        colEstApellido2.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getApellido_2));
        colEstNombre1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre_1));
        colEstNombre2.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre_2));
        colEstGenero.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getGenero));

        tblEstudiantes.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            estudianteSeleccionado = nuevo;
            if (nuevo == null) { limpiarFormularioEstudiante(); return; }
            txtIdentificacion.setText(String.valueOf(nuevo.getIdentificacion()));
            txtGrado.setText(String.valueOf(nuevo.getGrado()));
            txtApellido1.setText(valorSeguro(nuevo.getApellido_1()));
            txtApellido2.setText(valorSeguro(nuevo.getApellido_2()));
            txtNombre1.setText(valorSeguro(nuevo.getNombre_1()));
            txtNombre2.setText(valorSeguro(nuevo.getNombre_2()));
            txtGenero.setText(valorSeguro(nuevo.getGenero()));
        });
    }

    private void configurarFiltrosEstudiantes() {
        cbFiltroGrado.valueProperty().addListener((obs, oldValue, newValue) -> aplicarFiltrosTablaEstudiantes());
        txtBuscarEstudiante.textProperty().addListener((obs, oldText, newText) -> aplicarFiltrosTablaEstudiantes());
    }

    private void cargarEstudiantes() {
        actualizarBaseTablaEstudiantes(estudiantesDAO.obtenerEstudiantes());
    }

    private void actualizarBaseTablaEstudiantes(List<Estudiante> estudiantes) {
        estudiantesBaseTabla.clear();
        estudiantesBaseTabla.addAll(estudiantes);
        actualizarOpcionesFiltroGrado();
        aplicarFiltrosTablaEstudiantes();
    }

    private void actualizarOpcionesFiltroGrado() {
        String actual = cbFiltroGrado.getValue();
        ArrayList<String> grados = new ArrayList<>();
        grados.add("Todos");
        estudiantesBaseTabla.stream().map(Estudiante::getGrado).distinct().sorted().forEach(g -> grados.add(String.valueOf(g)));
        cbFiltroGrado.setItems(FXCollections.observableArrayList(grados));
        if (actual != null && grados.contains(actual)) { cbFiltroGrado.setValue(actual); } else { cbFiltroGrado.setValue("Todos"); }
    }

    private void aplicarFiltrosTablaEstudiantes() {
        String filtroNombre = txtBuscarEstudiante.getText() == null ? "" : txtBuscarEstudiante.getText().trim().toUpperCase();
        String filtroGrado = cbFiltroGrado.getValue();
        List<Estudiante> filtrados = estudiantesBaseTabla.stream()
                .filter(est -> { if (filtroGrado == null || "Todos".equalsIgnoreCase(filtroGrado)) return true; try { return est.getGrado() == Integer.parseInt(filtroGrado); } catch (NumberFormatException e) { return true; } })
                .filter(est -> filtroNombre.isEmpty() || est.getNombreCompleto().toUpperCase().contains(filtroNombre))
                .toList();
        tblEstudiantes.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML void clickGuardarCambiosEstudiante() {
        if (hayCsvPendiente) { aplicarCambiosCsvPendientes(); return; }
        if (estudianteSeleccionado == null) { Alertas.mostrarError("Debe seleccionar un estudiante de la tabla"); return; }
        try {
            long identificacion = Long.parseLong(txtIdentificacion.getText().trim());
            int grado = Integer.parseInt(txtGrado.getText().trim());
            if (grado <= 0) { Alertas.mostrarError("El grado debe ser mayor que 0"); return; }
            if (estudiantesDAO.existeIdentificacionEnOtroRegistro(identificacion, estudianteSeleccionado.getId())) { Alertas.mostrarError("Ya existe otro estudiante con esa identificacion"); return; }
            Estudiante actualizado = new Estudiante(estudianteSeleccionado.getId(), identificacion, grado, txtApellido1.getText(), txtApellido2.getText(), txtNombre1.getText(), txtNombre2.getText(), txtGenero.getText());
            if (estudiantesDAO.actualizarEstudiante(actualizado)) { Alertas.mostrarExito("Estudiante actualizado correctamente"); cargarEstudiantes(); lblResumenCsv.setText("Cambios manuales aplicados correctamente."); }
        } catch (NumberFormatException e) { Alertas.mostrarError("Identificacion y grado deben ser numericos"); }
    }

    @FXML void clickCargarCsvEstudiantes() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar archivo CSV de estudiantes");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File file = chooser.showOpenDialog(tblEstudiantes.getScene().getWindow());
        if (file == null) return;
        procesarCsvEstudiantes(file);
    }

    @FXML void clickDescartarCsv() {
        if (!hayCsvPendiente) { Alertas.mostrarError("No hay una vista previa CSV cargada para descartar"); return; }
        hayCsvPendiente = false;
        estudiantesPendientesCsv.clear();
        actualizarEstadoBotonGuardado();
        txtBuscarEstudiante.clear();
        cbFiltroGrado.setValue("Todos");
        cargarEstudiantes();
        lblResumenCsv.setText("Vista previa CSV descartada. Mostrando datos actuales.");
        Alertas.mostrarExito("Vista previa CSV descartada correctamente");
    }

    private void procesarCsvEstudiantes(File file) {
        try {
            EstudianteService service = new EstudianteService();
            EstudianteService.ParseResult result = service.parsearArchivoCsv(file);
            estudiantesPendientesCsv.clear();
            estudiantesPendientesCsv.addAll(result.estudiantes);
            if (estudiantesPendientesCsv.isEmpty()) { hayCsvPendiente = false; actualizarEstadoBotonGuardado(); Alertas.mostrarError("No se encontraron filas validas para vista previa."); return; }
            hayCsvPendiente = true;
            actualizarEstadoBotonGuardado();
            txtBuscarEstudiante.clear();
            cbFiltroGrado.setValue("Todos");
            actualizarBaseTablaEstudiantes(estudiantesPendientesCsv);
            String resumen = "Vista previa CSV cargada: " + estudiantesPendientesCsv.size() + " filas validas, " + result.errores + " errores. Revise/filtre la tabla y pulse Guardar registro CSV.";
            lblResumenCsv.setText(resumen);
            Alertas.mostrarExito("Vista previa cargada. Confirme con Guardar registro CSV.");
        } catch (IllegalArgumentException e) { Alertas.mostrarError(e.getMessage()); } catch (Exception e) { Alertas.mostrarError("Error al leer el CSV: " + e.getMessage()); }
    }

    private void aplicarCambiosCsvPendientes() {
        btnGuardarCambiosEstudiante.setDisable(true);
        btnDescartarCsv.setDisable(true);
        lblResumenCsv.setText("Guardando... por favor espere.");
        Task<EstudianteService.CsvResult> tarea = new Task<>() { @Override protected EstudianteService.CsvResult call() { EstudianteService service = new EstudianteService(); return service.procesarYGuardarLote(estudiantesPendientesCsv); } };
        tarea.setOnSucceeded(e -> { btnGuardarCambiosEstudiante.setDisable(false); btnDescartarCsv.setDisable(false); EstudianteService.CsvResult res = tarea.getValue(); hayCsvPendiente = false; estudiantesPendientesCsv.clear(); actualizarEstadoBotonGuardado(); txtBuscarEstudiante.clear(); cbFiltroGrado.setValue("Todos"); cargarEstudiantes(); if (res.exito) { String resumen = "CSV guardado. Insertados: " + res.insertados + ", Actualizados: " + res.actualizados + ", Sin cambios: " + res.sinCambios + ", Errores: " + res.errores; lblResumenCsv.setText(resumen); Alertas.mostrarExito(resumen); } else { lblResumenCsv.setText("Error al guardar CSV."); Alertas.mostrarError("Ocurrio un error grave al guardar el CSV. Se cancelo la operacion (Rollback)."); } });
        tarea.setOnFailed(e -> { btnGuardarCambiosEstudiante.setDisable(false); btnDescartarCsv.setDisable(false); actualizarEstadoBotonGuardado(); lblResumenCsv.setText("Fallo critico en el proceso."); Alertas.mostrarError("Error critico al procesar el lote: " + tarea.getException().getMessage()); });
        new Thread(tarea).start();
    }

    private void limpiarFormularioEstudiante() { txtIdentificacion.clear(); txtGrado.clear(); txtApellido1.clear(); txtApellido2.clear(); txtNombre1.clear(); txtNombre2.clear(); txtGenero.clear(); }
    private String valorSeguro(String valor) { return valor == null ? "" : valor; }
    private void actualizarEstadoBotonGuardado() { if (btnGuardarCambiosEstudiante != null) btnGuardarCambiosEstudiante.setText(hayCsvPendiente ? "Guardar registro CSV" : "Guardar cambios"); if (btnDescartarCsv != null) btnDescartarCsv.setDisable(!hayCsvPendiente); }
}