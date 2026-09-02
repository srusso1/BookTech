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
import java.util.Optional;

public class ConfigEstudiantesController implements utils.Refrescable {

    @Override
    public void refresh() {
        cargarEstudiantes();
    }

    @FXML private TableView<Estudiante> tblEstudiantes;
    @FXML private TableColumn<Estudiante, String> colEstIdentificacion, colEstGrado, colEstApellido1, colEstApellido2, colEstNombre1, colEstNombre2;
    @FXML private ComboBox<String> cbFiltroGrado;
    @FXML private TextField txtBuscarEstudiante, txtIdentificacion, txtGrado, txtApellido1, txtApellido2, txtNombre1, txtNombre2, txtGenero;
    @FXML private Label lblResumenCsv, lblErrorIdentificacion;
    @FXML private Button btnGuardarCambiosEstudiante, btnDescartarCsv;


    // Phase 2: Create/Edit/Delete
    @FXML private Label lblTituloFormEstudiante;
    @FXML private Label lblEstadoEdicionEstudiante;
    @FXML private Button btnNuevoEstudiante;
    @FXML private Button btnCancelarEdicionEstudiante;
    @FXML private Button btnEliminarEstudiante;
    private boolean modoEdicion = false;

    // Phase 3: CSV Preview Workflow
    @FXML private javafx.scene.layout.HBox hboxContenidoPrincipal;
    @FXML private javafx.scene.layout.VBox vboxPreviewCsv;
    @FXML private Label lblResumenPreviewCsv;
    @FXML private TableView<Estudiante> tblPreviewCsv;
    @FXML private TableColumn<Estudiante, String> colPrevIdentificacion, colPrevGrado, colPrevApellido1, colPrevApellido2, colPrevNombre1, colPrevNombre2;
    @FXML private Button btnDescargarPlantillaCsv, btnCancelarImportacionCsv, btnConfirmarImportacionCsv;

    // Phase 1: Filter chips
    @FXML private javafx.scene.layout.HBox hboxChipsFiltro;
    @FXML private Label lblChipGrado;
    @FXML private Button btnLimpiarFiltros;

    // Pagination
    @FXML private ComboBox<Integer> cbTamanioPagina;
    @FXML private Label lblInfoPaginacion;
    @FXML private Button btnAnterior, btnSiguiente, btnPrimeraPagina, btnUltimaPagina;
    @FXML private TextField txtIrAPagina;

    private final EstudiantesDAO estudiantesDAO;
    private final ArrayList<Estudiante> estudiantesPendientesCsv = new ArrayList<>();
    private final ArrayList<Estudiante> estudiantesBaseTabla = new ArrayList<>();
    private Estudiante estudianteSeleccionado;
    private boolean hayCsvPendiente = false;
    private final utils.Debouncer debouncer = new utils.Debouncer(300);

    private int paginaActual = 1;
    private int totalPaginas = 1;
    private int registrosPorPagina = 50;

    public ConfigEstudiantesController(EstudiantesDAO estudiantesDAO) {
        this.estudiantesDAO = estudiantesDAO;
    }

    @FXML
    void initialize() {
        btnPrimeraPagina.setTooltip(new Tooltip("Primera página"));
        btnAnterior.setTooltip(new Tooltip("Página anterior"));
        btnSiguiente.setTooltip(new Tooltip("Página siguiente"));
        btnUltimaPagina.setTooltip(new Tooltip("Última página"));

        configurarTablaEstudiantes();
        configurarPaginacion();
        configurarFiltrosEstudiantes();
        cargarEstudiantes();
        actualizarEstadoBotonGuardado();
    }

    // ==================== PAGINATION ====================

    private void irAPagina(int pagina) {
        if (pagina >= 1 && pagina <= totalPaginas) {
            paginaActual = pagina;
            cargarEstudiantes();
        }
    }

    @FXML void clickPrimeraPagina() { irAPagina(1); }
    @FXML void clickUltimaPagina() { irAPagina(totalPaginas); }
    @FXML void clickPaginaAnterior() { irAPagina(paginaActual - 1); }
    @FXML void clickPaginaSiguiente() { irAPagina(paginaActual + 1); }

    @FXML void clickIrAPagina() {
        try {
            int pag = Integer.parseInt(txtIrAPagina.getText().trim());
            if (pag >= 1 && pag <= totalPaginas) {
                irAPagina(pag);
            } else {
                mostrarErrorPaginacion();
            }
        } catch (NumberFormatException ex) {
            mostrarErrorPaginacion();
        }
    }

    private void mostrarErrorPaginacion() {
        txtIrAPagina.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 4px;");
        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
        pt.setOnFinished(e -> txtIrAPagina.setStyle(""));
        pt.play();
    }

    private void configurarPaginacion() {
        cbTamanioPagina.setItems(FXCollections.observableArrayList(20, 50, 100));
        cbTamanioPagina.setValue(registrosPorPagina);
        cbTamanioPagina.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                registrosPorPagina = newVal;
                paginaActual = 1;
                cargarEstudiantes();
            }
        });
    }

    // ==================== TABLE ====================

    private void configurarTablaEstudiantes() {
        colEstIdentificacion.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getIdentificacion())));
        colEstGrado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getGrado())));
        colEstApellido1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getApellido_1));
        colEstApellido2.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getApellido_2));
        colEstNombre1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre_1));
        colEstNombre2.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre_2));
        
        // Preview table columns
        colPrevIdentificacion.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getIdentificacion())));
        colPrevGrado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getGrado())));
        colPrevApellido1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getApellido_1));
        colPrevApellido2.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getApellido_2));
        colPrevNombre1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre_1));
        colPrevNombre2.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre_2));

        tblEstudiantes.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            estudianteSeleccionado = nuevo;
            if (nuevo == null) {
                return;
            }
            txtIdentificacion.setText(String.valueOf(nuevo.getIdentificacion()));
            txtGrado.setText(String.valueOf(nuevo.getGrado()));
            txtApellido1.setText(valorSeguro(nuevo.getApellido_1()));
            txtApellido2.setText(valorSeguro(nuevo.getApellido_2()));
            txtNombre1.setText(valorSeguro(nuevo.getNombre_1()));
            txtNombre2.setText(valorSeguro(nuevo.getNombre_2()));
            txtGenero.setText(valorSeguro(nuevo.getGenero()));
            
            modoEdicion = true;
            lblTituloFormEstudiante.setText("Editando estudiante");
            lblEstadoEdicionEstudiante.setText(nuevo.getNombreCompleto());
            btnEliminarEstudiante.setVisible(true);
            btnEliminarEstudiante.setManaged(true);
            btnEliminarEstudiante.setDisable(false);
            limpiarErrorIdentificacion();
        });
    }

    // ==================== FILTERS ====================

    private void configurarFiltrosEstudiantes() {
        cbFiltroGrado.valueProperty().addListener((obs, oldValue, newValue) -> {
            paginaActual = 1;
            actualizarChipsFiltro();
            cargarEstudiantes();
        });
        txtBuscarEstudiante.textProperty().addListener((obs, oldText, newText) -> {
            paginaActual = 1;
            actualizarChipsFiltro();
            debouncer.debounce(this::cargarEstudiantes);
        });
    }

    private void actualizarChipsFiltro() {
        String grado = cbFiltroGrado.getValue();
        String busqueda = txtBuscarEstudiante.getText();
        boolean hayFiltroGrado = grado != null && !"Todos".equalsIgnoreCase(grado);
        boolean hayFiltros = hayFiltroGrado || (busqueda != null && !busqueda.isBlank());

        if (hayFiltroGrado) {
            lblChipGrado.setText("Grado: " + grado + "  \u2715");
            lblChipGrado.setOnMouseClicked(e -> cbFiltroGrado.setValue("Todos"));
        }

        hboxChipsFiltro.setVisible(hayFiltroGrado);
        hboxChipsFiltro.setManaged(hayFiltroGrado);
        lblChipGrado.setVisible(hayFiltroGrado);
        lblChipGrado.setManaged(hayFiltroGrado);
        btnLimpiarFiltros.setVisible(hayFiltros);
        btnLimpiarFiltros.setManaged(hayFiltros);
    }

    @FXML
    void clickLimpiarFiltros() {
        cbFiltroGrado.setValue("Todos");
        txtBuscarEstudiante.clear();
    }

    // ==================== DATA LOADING ====================

    private void cargarEstudiantes() {
        String busqueda = txtBuscarEstudiante.getText();
        String filtroGrado = cbFiltroGrado.getValue();
        int offset = (paginaActual - 1) * registrosPorPagina;
        int registros = registrosPorPagina;

        tblEstudiantes.setPlaceholder(new Label("Cargando..."));

        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            List<Estudiante> estudiantes = estudiantesDAO.obtenerPaginados(registros, offset, busqueda);
            int totalRegistros = estudiantesDAO.contarTotal(busqueda);
            return new Object[]{estudiantes, totalRegistros};
        }).thenAcceptAsync(result -> {
            @SuppressWarnings("unchecked")
            List<Estudiante> estudiantes = (List<Estudiante>) result[0];
            int totalRegistros = (int) result[1];

            totalPaginas = (int) Math.ceil((double) totalRegistros / registros);
            if (totalPaginas == 0) totalPaginas = 1;

            lblInfoPaginacion.setText("Página " + paginaActual + " de " + totalPaginas + " (Total: " + totalRegistros + ")");
            btnAnterior.setDisable(paginaActual <= 1);
            btnPrimeraPagina.setDisable(paginaActual <= 1);
            btnSiguiente.setDisable(paginaActual >= totalPaginas);
            btnUltimaPagina.setDisable(paginaActual >= totalPaginas);

            if (filtroGrado != null && !"Todos".equalsIgnoreCase(filtroGrado)) {
                estudiantes = estudiantes.stream().filter(e -> String.valueOf(e.getGrado()).equals(filtroGrado)).toList();
            }

            tblEstudiantes.setItems(FXCollections.observableArrayList(estudiantes));

            if (cbFiltroGrado.getItems().isEmpty()) {
                cbFiltroGrado.getItems().addAll("Todos", "6", "7", "8", "9", "10", "11");
                cbFiltroGrado.setValue("Todos");
            }

            if (estudiantes.isEmpty()) {
                tblEstudiantes.setPlaceholder(new Label("No hay estudiantes que coincidan"));
            }
        }, javafx.application.Platform::runLater);
    }

    // ==================== EDIT ACTIONS ====================

    @FXML void clickNuevoEstudiante() {
        if (tblEstudiantes != null) tblEstudiantes.getSelectionModel().clearSelection();
        limpiarFormularioEstudiante();
        modoEdicion = false;
        estudianteSeleccionado = null;
        lblTituloFormEstudiante.setText("Nuevo estudiante");
        lblEstadoEdicionEstudiante.setText("Ingresa los datos del nuevo estudiante.");
        btnEliminarEstudiante.setVisible(false);
        btnEliminarEstudiante.setManaged(false);
        btnEliminarEstudiante.setDisable(true);
        limpiarErrorIdentificacion();
    }

    @FXML void clickCancelarEdicionEstudiante() {
        if (tblEstudiantes != null) tblEstudiantes.getSelectionModel().clearSelection();
        limpiarFormularioEstudiante();
        modoEdicion = false;
        estudianteSeleccionado = null;
        lblTituloFormEstudiante.setText("Detalle y Edicion");
        lblEstadoEdicionEstudiante.setText("Selecciona un estudiante de la tabla para modificar sus datos.");
        btnEliminarEstudiante.setVisible(false);
        btnEliminarEstudiante.setManaged(false);
        btnEliminarEstudiante.setDisable(true);
        limpiarErrorIdentificacion();
    }

    @FXML void clickEliminarEstudiante() {
        if (estudianteSeleccionado == null) return;
        boolean confirma = Alertas.mostrarConfirmacion("Esta seguro de eliminar al estudiante `" + estudianteSeleccionado.getNombreCompleto() + "`?");
        if (confirma) {
            if (estudiantesDAO.eliminarEstudiante(estudianteSeleccionado.getId())) {
                Alertas.mostrarExito("Estudiante eliminado exitosamente.");
                cargarEstudiantes();
                clickCancelarEdicionEstudiante();
            } else {
                Alertas.mostrarError("No se pudo eliminar el estudiante. Es posible que tenga prestamos activos.");
            }
        }
    }


    @FXML void clickGuardarCambiosEstudiante() {
        if (hayCsvPendiente) { aplicarCambiosCsvPendientes(); return; }
        
        limpiarErrorIdentificacion();
        
        try {
            long identificacion = Long.parseLong(txtIdentificacion.getText().trim());
            int grado = Integer.parseInt(txtGrado.getText().trim());
            if (grado <= 0) { Alertas.mostrarError("El grado debe ser mayor que 0"); return; }
            
            if (modoEdicion) {
                if (estudianteSeleccionado == null) return;
                if (estudiantesDAO.existeIdentificacionEnOtroRegistro(identificacion, estudianteSeleccionado.getId())) { 
                    mostrarErrorIdentificacion("Ya existe un estudiante con esta identificación."); 
                    return; 
                }
                
                // NOTA SOBRE INTEGRIDAD: La tabla `prestamos` usa `id_estudiante` (clave primaria sustituta autoincremental de la BD)
                // como FK, por lo tanto, modificar la `identificacion` (clave de negocio) no rompe las referencias 
                // ni genera préstamos huérfanos. No se requiere UPDATE en cascada para los préstamos en este caso.
                
                Estudiante actualizado = new Estudiante(estudianteSeleccionado.getId(), identificacion, grado, txtApellido1.getText(), txtApellido2.getText(), txtNombre1.getText(), txtNombre2.getText(), txtGenero.getText());
                if (estudiantesDAO.actualizarEstudiante(actualizado)) { 
                    Alertas.mostrarExito("Estudiante actualizado correctamente"); 
                    cargarEstudiantes(); 
                    lblResumenCsv.setText("Cambios manuales aplicados correctamente."); 
                }
            } else {
                // Modo crear
                if (estudiantesDAO.obtenerEstudiantePorIdentificacion(identificacion) != null) { 
                    mostrarErrorIdentificacion("Ya existe un estudiante con esta identificación."); 
                    return; 
                }
                Estudiante nuevo = new Estudiante(0, identificacion, grado, txtApellido1.getText(), txtApellido2.getText(), txtNombre1.getText(), txtNombre2.getText(), txtGenero.getText());
                if (estudiantesDAO.insertarEstudiante(nuevo)) {
                    Alertas.mostrarExito("Estudiante creado correctamente");
                    cargarEstudiantes();
                    clickCancelarEdicionEstudiante();
                    lblResumenCsv.setText("Estudiante creado exitosamente.");
                } else {
                    Alertas.mostrarError("Error al crear el estudiante.");
                }
            }
        } catch (NumberFormatException e) { 
            Alertas.mostrarError("Identificacion y grado deben ser numericos y obligatorios"); 
            mostrarErrorIdentificacion("Identificación inválida");
        }
    }
    
    private void mostrarErrorIdentificacion(String mensaje) {
        lblErrorIdentificacion.setText(mensaje);
        lblErrorIdentificacion.setVisible(true);
        lblErrorIdentificacion.setManaged(true);
        txtIdentificacion.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 4px;");
    }
    
    private void limpiarErrorIdentificacion() {
        if (lblErrorIdentificacion != null) {
            lblErrorIdentificacion.setVisible(false);
            lblErrorIdentificacion.setManaged(false);
            txtIdentificacion.setStyle("");
        }
    }

    // ==================== CSV ====================

    @FXML void clickDescargarPlantillaCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar plantilla CSV de estudiantes");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        chooser.setInitialFileName("plantilla_estudiantes.csv");
        File file = chooser.showSaveDialog(tblEstudiantes.getScene().getWindow());
        if (file != null) {
            try {
                String headers = "identificacion,grado,apellido_1,apellido_2,nombre_1,nombre_2,genero\n";
                java.nio.file.Files.writeString(file.toPath(), headers);
                Alertas.mostrarExito("Plantilla descargada correctamente");
            } catch (Exception e) {
                Alertas.mostrarError("Error al guardar la plantilla: " + e.getMessage());
            }
        }
    }

    @FXML void clickCargarCsvEstudiantes() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar archivo CSV de estudiantes");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File file = chooser.showOpenDialog(tblEstudiantes.getScene().getWindow());
        if (file == null) return;
        procesarCsvEstudiantes(file);
    }

    @FXML void clickDescartarCsv() { clickCancelarImportacionCsv(); }
    
    @FXML void clickCancelarImportacionCsv() {
        hayCsvPendiente = false;
        estudiantesPendientesCsv.clear();
        vboxPreviewCsv.setVisible(false);
        vboxPreviewCsv.setManaged(false);
        hboxContenidoPrincipal.setVisible(true);
        hboxContenidoPrincipal.setManaged(true);
        lblResumenCsv.setText("Vista previa CSV descartada. Mostrando datos actuales.");
        actualizarEstadoBotonGuardado();
    }
    
    @FXML void clickConfirmarImportacionCsv() {
        aplicarCambiosCsvPendientes();
    }

    private void procesarCsvEstudiantes(File file) {
        try {
            EstudianteService service = new EstudianteService();
            EstudianteService.ParseResult result = service.parsearArchivoCsv(file);
            estudiantesPendientesCsv.clear();
            estudiantesPendientesCsv.addAll(result.estudiantes);
            if (estudiantesPendientesCsv.isEmpty()) { 
                hayCsvPendiente = false; 
                actualizarEstadoBotonGuardado(); 
                Alertas.mostrarError("No se encontraron filas validas para vista previa."); 
                return; 
            }
            
            hayCsvPendiente = true;
            actualizarEstadoBotonGuardado();
            
            // Show preview box, hide main content
            hboxContenidoPrincipal.setVisible(false);
            hboxContenidoPrincipal.setManaged(false);
            vboxPreviewCsv.setVisible(true);
            vboxPreviewCsv.setManaged(true);
            
            // Limit preview to first 10 for performance visually
            List<Estudiante> previewList = estudiantesPendientesCsv.stream().limit(10).toList();
            tblPreviewCsv.setItems(FXCollections.observableArrayList(previewList));
            
            String resumen = "Se detectaron " + estudiantesPendientesCsv.size() + " filas validas y " + result.errores + " errores de formato.\nMostrando las primeras 10 filas de preview. ¿Desea confirmar la importación a la base de datos?";
            lblResumenPreviewCsv.setText(resumen);
            lblResumenCsv.setText("Vista previa en curso...");
            
        } catch (IllegalArgumentException e) { 
            Alertas.mostrarError(e.getMessage()); 
        } catch (Exception e) { 
            Alertas.mostrarError("Error al leer el CSV: " + e.getMessage()); 
        }
    }

    private void aplicarCambiosCsvPendientes() {
        btnConfirmarImportacionCsv.setDisable(true);
        btnCancelarImportacionCsv.setDisable(true);
        lblResumenPreviewCsv.setText("Guardando... por favor espere.");
        
        Task<EstudianteService.CsvResult> tarea = new Task<>() { 
            @Override protected EstudianteService.CsvResult call() { 
                EstudianteService service = new EstudianteService(); 
                return service.procesarYGuardarLote(estudiantesPendientesCsv); 
            } 
        };
        tarea.setOnSucceeded(e -> { 
            btnConfirmarImportacionCsv.setDisable(false); 
            btnCancelarImportacionCsv.setDisable(false); 
            
            EstudianteService.CsvResult res = tarea.getValue(); 
            hayCsvPendiente = false; 
            estudiantesPendientesCsv.clear(); 
            actualizarEstadoBotonGuardado(); 
            
            // Hide preview, show main
            vboxPreviewCsv.setVisible(false);
            vboxPreviewCsv.setManaged(false);
            hboxContenidoPrincipal.setVisible(true);
            hboxContenidoPrincipal.setManaged(true);
            
            cargarEstudiantes(); 
            
            if (res.exito) { 
                String resumen = "CSV guardado. Insertados: " + res.insertados + ", Actualizados: " + res.actualizados + ", Sin cambios: " + res.sinCambios + ", Errores: " + res.errores; 
                lblResumenCsv.setText(resumen); 
                Alertas.mostrarExito(resumen); 
            } else { 
                lblResumenCsv.setText("Error al guardar CSV."); 
                Alertas.mostrarError("Ocurrio un error grave al guardar el CSV. Se cancelo la operacion (Rollback)."); 
            } 
        });
        tarea.setOnFailed(e -> { 
            btnConfirmarImportacionCsv.setDisable(false); 
            btnCancelarImportacionCsv.setDisable(false); 
            actualizarEstadoBotonGuardado(); 
            lblResumenPreviewCsv.setText("Fallo critico en el proceso."); 
            Alertas.mostrarError("Error critico al procesar el lote: " + tarea.getException().getMessage()); 
        });
        new Thread(tarea).start();
    }

    // ==================== UTILS ====================

    private void limpiarFormularioEstudiante() { txtIdentificacion.clear(); txtGrado.clear(); txtApellido1.clear(); txtApellido2.clear(); txtNombre1.clear(); txtNombre2.clear(); txtGenero.clear(); }
    private String valorSeguro(String valor) { return valor == null ? "" : valor; }
    private void actualizarEstadoBotonGuardado() { if (btnGuardarCambiosEstudiante != null) btnGuardarCambiosEstudiante.setText(hayCsvPendiente ? "Guardar registro CSV" : "Guardar cambios"); if (btnDescartarCsv != null) btnDescartarCsv.setDisable(!hayCsvPendiente); }
}