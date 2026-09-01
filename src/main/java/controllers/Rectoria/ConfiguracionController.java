package controllers.Rectoria;

import database.EditorialesDAO;
import model.Editorial;
import database.CategoriasDAO;
import model.Categoria;
import database.DocentesDAO;
import database.EstudiantesDAO;
import database.MotivosPlataformaDAO;
import database.MotivosPrestamoDAO;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import model.Docente;
import model.Estudiante;
import model.MotivoPlataforma;
import model.MotivoPrestamo;
import utils.Alertas;
import services.EstudianteService;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfiguracionController {

    public ConfiguracionController(EstudiantesDAO estudiantesDAO, DocentesDAO docentesDAO, MotivosPrestamoDAO motivosPrestamoDAO, MotivosPlataformaDAO motivosPlataformaDAO, EditorialesDAO editorialesDAO, CategoriasDAO categoriasDAO) {
        this.estudiantesDAO = estudiantesDAO;
        this.docentesDAO = docentesDAO;
        this.motivosPrestamoDAO = motivosPrestamoDAO;
        this.motivosPlataformaDAO = motivosPlataformaDAO;
        this.editorialesDAO = editorialesDAO;
        this.categoriasDAO = categoriasDAO;
    }

    @FXML
    private TableView<Estudiante> tblEstudiantes;
    @FXML
    private TableColumn<Estudiante, String> colEstIdentificacion;
    @FXML
    private TableColumn<Estudiante, String> colEstGrado;
    @FXML
    private TableColumn<Estudiante, String> colEstApellido1;
    @FXML
    private TableColumn<Estudiante, String> colEstApellido2;
    @FXML
    private TableColumn<Estudiante, String> colEstNombre1;
    @FXML
    private TableColumn<Estudiante, String> colEstNombre2;
    @FXML
    private TableColumn<Estudiante, String> colEstGenero;

    @FXML
    private ComboBox<String> cbFiltroGrado;
    @FXML
    private TextField txtBuscarEstudiante;

    @FXML
    private TextField txtIdentificacion;
    @FXML
    private TextField txtGrado;
    @FXML
    private TextField txtApellido1;
    @FXML
    private TextField txtApellido2;
    @FXML
    private TextField txtNombre1;
    @FXML
    private TextField txtNombre2;
    @FXML
    private TextField txtGenero;
    @FXML
    private Label lblResumenCsv;
    @FXML
    private Button btnGuardarCambiosEstudiante;
    @FXML
    private Button btnDescartarCsv;

    @FXML
    private TableView<Docente> tblDocentes;
    @FXML
    private TableColumn<Docente, String> colDocId;
    @FXML
    private TableColumn<Docente, String> colDocNombre1;
    @FXML
    private TableColumn<Docente, String> colDocNombre2;
    @FXML
    private TableColumn<Docente, String> colDocApellido1;
    @FXML
    private TableColumn<Docente, String> colDocApellido2;

    @FXML
    private TextField txtBuscarDocente;
    @FXML
    private TextField txtDocNombre1;
    @FXML
    private TextField txtDocNombre2;
    @FXML
    private TextField txtDocApellido1;
    @FXML
    private TextField txtDocApellido2;
    @FXML
    private Label lblTituloFormDocente;
    @FXML
    private Label lblEstadoEdicionDocente;
    @FXML
    private Button btnGuardarDocente;
    @FXML
    private Button btnEliminarDocente;
    @FXML
    private Button btnNuevoDocente;

    @FXML
    private TableView<MotivoPrestamo> tblMotivosPrestamo;
    @FXML
    private TableColumn<MotivoPrestamo, String> colPrestamoNombre;
    @FXML
    private TableColumn<MotivoPrestamo, String> colPrestamoEstado;
    @FXML
    private TextField txtNuevoMotivoPrestamo;

    @FXML
    private TableView<MotivoPlataforma> tblMotivosPlataforma;
    @FXML
    private TableColumn<MotivoPlataforma, String> colPlataformaNombre;
    @FXML
    private TableColumn<MotivoPlataforma, String> colPlataformaEstado;
    @FXML
    private TextField txtNuevoMotivoPlataforma;

    private final EstudiantesDAO estudiantesDAO;
    private final DocentesDAO docentesDAO;
    private final MotivosPrestamoDAO motivosPrestamoDAO;
    private final MotivosPlataformaDAO motivosPlataformaDAO;
    private final EditorialesDAO editorialesDAO;
    private final CategoriasDAO categoriasDAO;

    @FXML private TableView<Editorial> tblEditoriales;
    @FXML private TableColumn<Editorial, String> colEditorialId;
    @FXML private TableColumn<Editorial, String> colEditorialNombre;
    @FXML private TableColumn<Editorial, String> colEditorialEstado;
    @FXML private TextField txtBuscarEditorial;
    @FXML private TextField txtEditorialNombre;
    @FXML private ComboBox<String> cbEditorialEstado;
    @FXML private Label lblEstadoEdicionEditorial;
    private Editorial editorialSeleccionada = null;

    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, String> colCategoriaId;
    @FXML private TableColumn<Categoria, String> colCategoriaNombre;
    @FXML private TableColumn<Categoria, String> colCategoriaEstado;
    @FXML private TextField txtBuscarCategoria;
    @FXML private TextField txtCategoriaNombre;
    @FXML private ComboBox<String> cbCategoriaEstado;
    @FXML private Label lblEstadoEdicionCategoria;
    private Categoria categoriaSeleccionada = null;

    private final ArrayList<Estudiante> estudiantesPendientesCsv = new ArrayList<>();
    private final ArrayList<Estudiante> estudiantesBaseTabla = new ArrayList<>();
    private final ArrayList<Docente> docentesBaseTabla = new ArrayList<>();

    private Estudiante estudianteSeleccionado;
    private Docente docenteSeleccionado;
    private MotivoPrestamo motivoPrestamoSeleccionado;
    private MotivoPlataforma motivoPlataformaSeleccionado;
    private boolean hayCsvPendiente = false;

    @FXML
    void initialize() {
        configurarTablaEstudiantes();
        configurarFiltrosEstudiantes();
        configurarTablaDocentes();
        configurarFiltrosDocentes();
        configurarTablaMotivos();
        cargarEstudiantes();
        cargarDocentes();
        cargarMotivosPrestamo();
        cargarMotivosPlataforma();
        if (tblEditoriales != null) {
            configurarTablaEditoriales();
            cargarEditoriales();
        }
        if (tblCategorias != null) {
            configurarTablaCategorias();
            cargarCategorias();
        }
        actualizarEstadoBotonGuardado();
    }

    private void configurarTablaEstudiantes() {
        colEstIdentificacion.setCellValueFactory(
                cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getIdentificacion())));
        colEstGrado.setCellValueFactory(
                cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getGrado())));
        colEstApellido1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getApellido_1));
        colEstApellido2.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getApellido_2));
        colEstNombre1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre_1));
        colEstNombre2.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre_2));
        colEstGenero.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getGenero));

        tblEstudiantes.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            estudianteSeleccionado = nuevo;
            if (nuevo == null) {
                limpiarFormularioEstudiante();
                return;
            }
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

    private void configurarTablaMotivos() {
        colPrestamoNombre.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre));
        colPrestamoEstado.setCellValueFactory(
                cd -> Bindings.createStringBinding(() -> cd.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));

        colPlataformaNombre.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre));
        colPlataformaEstado.setCellValueFactory(
                cd -> Bindings.createStringBinding(() -> cd.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));

        tblMotivosPrestamo.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, nuevo) -> motivoPrestamoSeleccionado = nuevo);
        tblMotivosPlataforma.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, nuevo) -> motivoPlataformaSeleccionado = nuevo);
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
        estudiantesBaseTabla.stream().map(Estudiante::getGrado).distinct().sorted()
                .forEach(g -> grados.add(String.valueOf(g)));
        cbFiltroGrado.setItems(FXCollections.observableArrayList(grados));
        if (actual != null && grados.contains(actual)) {
            cbFiltroGrado.setValue(actual);
        } else {
            cbFiltroGrado.setValue("Todos");
        }
    }

    private void aplicarFiltrosTablaEstudiantes() {
        String filtroNombre = txtBuscarEstudiante.getText() == null ? ""
                : txtBuscarEstudiante.getText().trim().toUpperCase();
        String filtroGrado = cbFiltroGrado.getValue();

        List<Estudiante> filtrados = estudiantesBaseTabla.stream()
                .filter(est -> {
                    if (filtroGrado == null || "Todos".equalsIgnoreCase(filtroGrado)) {
                        return true;
                    }
                    try {
                        return est.getGrado() == Integer.parseInt(filtroGrado);
                    } catch (NumberFormatException e) {
                        return true;
                    }
                })
                .filter(est -> filtroNombre.isEmpty() || est.getNombreCompleto().toUpperCase().contains(filtroNombre))
                .toList();

        tblEstudiantes.setItems(FXCollections.observableArrayList(filtrados));
    }

    private void cargarMotivosPrestamo() {
        tblMotivosPrestamo
                .setItems(FXCollections.observableArrayList(motivosPrestamoDAO.obtenerTodosMotivosPrestamo()));
    }

    private void cargarMotivosPlataforma() {
        tblMotivosPlataforma
                .setItems(FXCollections.observableArrayList(motivosPlataformaDAO.obtenerTodosMotivosPlataforma()));
    }

    @FXML
    void clickGuardarCambiosEstudiante() {
        if (hayCsvPendiente) {
            aplicarCambiosCsvPendientes();
            return;
        }

        if (estudianteSeleccionado == null) {
            Alertas.mostrarError("Debe seleccionar un estudiante de la tabla");
            return;
        }

        try {
            long identificacion = Long.parseLong(txtIdentificacion.getText().trim());
            int grado = Integer.parseInt(txtGrado.getText().trim());
            if (grado <= 0) {
                Alertas.mostrarError("El grado debe ser mayor que 0");
                return;
            }

            if (estudiantesDAO.existeIdentificacionEnOtroRegistro(identificacion, estudianteSeleccionado.getId())) {
                Alertas.mostrarError("Ya existe otro estudiante con esa identificaciÃƒÂ³n");
                return;
            }

            Estudiante actualizado = new Estudiante(
                    estudianteSeleccionado.getId(),
                    identificacion,
                    grado,
                    txtApellido1.getText(),
                    txtApellido2.getText(),
                    txtNombre1.getText(),
                    txtNombre2.getText(),
                    txtGenero.getText());

            if (estudiantesDAO.actualizarEstudiante(actualizado)) {
                Alertas.mostrarExito("Estudiante actualizado correctamente");
                cargarEstudiantes();
                lblResumenCsv.setText("Cambios manuales aplicados correctamente.");
            }
        } catch (NumberFormatException e) {
            Alertas.mostrarError("IdentificaciÃƒÂ³n y grado deben ser numÃƒÂ©ricos");
        }
    }

    @FXML
    void clickCargarCsvEstudiantes() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar archivo CSV de estudiantes");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File file = chooser.showOpenDialog(tblEstudiantes.getScene().getWindow());

        if (file == null) {
            return;
        }

        procesarCsvEstudiantes(file);
    }

    @FXML
    void clickDescartarCsv() {
        if (!hayCsvPendiente) {
            Alertas.mostrarError("No hay una vista previa CSV cargada para descartar");
            return;
        }

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

            if (estudiantesPendientesCsv.isEmpty()) {
                hayCsvPendiente = false;
                actualizarEstadoBotonGuardado();
                Alertas.mostrarError("No se encontraron filas vÃ¡lidas para vista previa.");
                return;
            }

            hayCsvPendiente = true;
            actualizarEstadoBotonGuardado();
            txtBuscarEstudiante.clear();
            cbFiltroGrado.setValue("Todos");
            actualizarBaseTablaEstudiantes(estudiantesPendientesCsv);

            String resumen = "Vista previa CSV cargada: " + estudiantesPendientesCsv.size()
                    + " filas vÃ¡lidas, " + result.errores + " errores. Revise/filtre la tabla y pulse Guardar registro CSV.";
            lblResumenCsv.setText(resumen);
            Alertas.mostrarExito("Vista previa cargada. Confirme con Guardar registro CSV.");

        } catch (IllegalArgumentException e) {
            Alertas.mostrarError(e.getMessage());
        } catch (Exception e) {
            Alertas.mostrarError("Error al leer el CSV: " + e.getMessage());
        }
    }

    private void aplicarCambiosCsvPendientes() {
        btnGuardarCambiosEstudiante.setDisable(true);
        btnDescartarCsv.setDisable(true);
        lblResumenCsv.setText("Guardando... por favor espere.");

        Task<EstudianteService.CsvResult> tarea = new Task<>() {
            @Override
            protected EstudianteService.CsvResult call() throws Exception {
                EstudianteService service = new EstudianteService();
                return service.procesarYGuardarLote(estudiantesPendientesCsv);
            }
        };

        tarea.setOnSucceeded(e -> {
            btnGuardarCambiosEstudiante.setDisable(false);
            btnDescartarCsv.setDisable(false);
            EstudianteService.CsvResult res = tarea.getValue();
            hayCsvPendiente = false;
            estudiantesPendientesCsv.clear();
            actualizarEstadoBotonGuardado();
            txtBuscarEstudiante.clear();
            cbFiltroGrado.setValue("Todos");
            cargarEstudiantes();

            if (res.exito) {
                String resumen = "CSV guardado. Insertados: " + res.insertados
                        + ", Actualizados: " + res.actualizados
                        + ", Sin cambios: " + res.sinCambios
                        + ", Errores: " + res.errores;
                lblResumenCsv.setText(resumen);
                Alertas.mostrarExito(resumen);
            } else {
                lblResumenCsv.setText("Error al guardar CSV.");
                Alertas.mostrarError("OcurriÃƒÂ³ un error grave al guardar el CSV. Se cancelÃƒÂ³ la operaciÃƒÂ³n (Rollback).");
            }
        });

        tarea.setOnFailed(e -> {
            btnGuardarCambiosEstudiante.setDisable(false);
            btnDescartarCsv.setDisable(false);
            actualizarEstadoBotonGuardado();
            lblResumenCsv.setText("Fallo crÃƒÂ­tico en el proceso.");
            Alertas.mostrarError("Error crÃƒÂ­tico al procesar el lote: " + tarea.getException().getMessage());
        });

        new Thread(tarea).start();
    }

    @FXML
    void clickAgregarMotivoPrestamo() {
        String nombre = txtNuevoMotivoPrestamo.getText();
        if (nombre == null || nombre.trim().isEmpty()) {
            Alertas.mostrarError("Debe ingresar el nombre del motivo de prÃƒÂ©stamo");
            return;
        }
        if (motivosPrestamoDAO.agregarMotivoPrestamo(nombre)) {
            txtNuevoMotivoPrestamo.clear();
            cargarMotivosPrestamo();
            Alertas.mostrarExito("Motivo de prÃƒÂ©stamo registrado");
        }
    }

    @FXML
    void clickAlternarEstadoMotivoPrestamo() {
        if (motivoPrestamoSeleccionado == null) {
            Alertas.mostrarError("Seleccione un motivo de prÃƒÂ©stamo");
            return;
        }

        int nuevoEstado = motivoPrestamoSeleccionado.getEstado() == 1 ? 0 : 1;
        if (motivosPrestamoDAO.actualizarEstadoMotivoPrestamo(motivoPrestamoSeleccionado.getId(), nuevoEstado)) {
            cargarMotivosPrestamo();
            Alertas.mostrarExito("Estado actualizado correctamente");
        }
    }

    @FXML
    void clickAgregarMotivoPlataforma() {
        String nombre = txtNuevoMotivoPlataforma.getText();
        if (nombre == null || nombre.trim().isEmpty()) {
            Alertas.mostrarError("Debe ingresar el nombre del motivo de plataforma");
            return;
        }
        if (motivosPlataformaDAO.agregarMotivoPlataforma(nombre)) {
            txtNuevoMotivoPlataforma.clear();
            cargarMotivosPlataforma();
            Alertas.mostrarExito("Motivo de plataforma registrado");
        }
    }

    @FXML
    void clickAlternarEstadoMotivoPlataforma() {
        if (motivoPlataformaSeleccionado == null) {
            Alertas.mostrarError("Seleccione un motivo de plataforma");
            return;
        }

        int nuevoEstado = motivoPlataformaSeleccionado.getEstado() == 1 ? 0 : 1;
        if (motivosPlataformaDAO.actualizarEstadoMotivoPlataforma(motivoPlataformaSeleccionado.getId(), nuevoEstado)) {
            cargarMotivosPlataforma();
            Alertas.mostrarExito("Estado actualizado correctamente");
        }
    }

    private void limpiarFormularioEstudiante() {
        txtIdentificacion.clear();
        txtGrado.clear();
        txtApellido1.clear();
        txtApellido2.clear();
        txtNombre1.clear();
        txtNombre2.clear();
        txtGenero.clear();
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    private void actualizarEstadoBotonGuardado() {
        if (btnGuardarCambiosEstudiante != null) {
            btnGuardarCambiosEstudiante.setText(hayCsvPendiente ? "Guardar registro CSV" : "Guardar cambios");
        }
        if (btnDescartarCsv != null) {
            btnDescartarCsv.setDisable(!hayCsvPendiente);
        }
    }

    private void configurarTablaDocentes() {
        if (tblDocentes == null)
            return;
        colDocId.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getId())));
        colDocNombre1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre_1));
        colDocNombre2.setCellValueFactory(
                cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getNombre_2())));
        colDocApellido1.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getApellido_1));
        colDocApellido2.setCellValueFactory(
                cd -> Bindings.createStringBinding(() -> valorSeguro(cd.getValue().getApellido_2())));

        tblDocentes.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            docenteSeleccionado = nuevo;
            if (nuevo == null) {
                limpiarFormularioDocente();
                return;
            }
            txtDocNombre1.setText(valorSeguro(nuevo.getNombre_1()));
            txtDocNombre2.setText(valorSeguro(nuevo.getNombre_2()));
            txtDocApellido1.setText(valorSeguro(nuevo.getApellido_1()));
            txtDocApellido2.setText(valorSeguro(nuevo.getApellido_2()));
            if (lblTituloFormDocente != null)
                lblTituloFormDocente.setText("Editar Docente #" + nuevo.getId());
            if (lblEstadoEdicionDocente != null)
                lblEstadoEdicionDocente.setText("Modifique los datos y presione Actualizar.");
            if (btnGuardarDocente != null)
                btnGuardarDocente.setText("Actualizar docente");
            if (btnEliminarDocente != null)
                btnEliminarDocente.setDisable(false);
        });
    }

    private void configurarFiltrosDocentes() {
        if (txtBuscarDocente != null) {
            txtBuscarDocente.textProperty().addListener((obs, oldText, newText) -> aplicarFiltroDocentes());
        }
    }

    private void cargarDocentes() {
        docentesBaseTabla.clear();
        docentesBaseTabla.addAll(docentesDAO.obtenerDocentes());
        aplicarFiltroDocentes();
    }

    private void aplicarFiltroDocentes() {
        if (tblDocentes == null)
            return;
        String busqueda = txtBuscarDocente == null || txtBuscarDocente.getText() == null
                ? ""
                : txtBuscarDocente.getText().trim().toLowerCase();

        List<Docente> filtrados = docentesBaseTabla.stream()
                .filter(d -> busqueda.isEmpty()
                        || d.getNombreCompleto().toLowerCase().contains(busqueda)
                        || String.valueOf(d.getId()).contains(busqueda))
                .toList();

        tblDocentes.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    void clickGuardarDocente() {
        String n1 = txtDocNombre1.getText() != null ? txtDocNombre1.getText().trim().toUpperCase() : "";
        String n2 = txtDocNombre2.getText() != null ? txtDocNombre2.getText().trim().toUpperCase() : "";
        String a1 = txtDocApellido1.getText() != null ? txtDocApellido1.getText().trim().toUpperCase() : "";
        String a2 = txtDocApellido2.getText() != null ? txtDocApellido2.getText().trim().toUpperCase() : "";

        if (n1.isEmpty() || a1.isEmpty()) {
            Alertas.mostrarWarning("El primer nombre y el primer apellido son obligatorios.");
            return;
        }

        if (docenteSeleccionado == null) {
            Docente nuevo = new Docente(0, n1, n2, a1, a2);
            boolean ok = docentesDAO.insertarDocente(nuevo);
            if (ok) {
                Alertas.mostrarExito("Docente registrado exitosamente.");
                cargarDocentes();
                limpiarFormularioDocente();
            } else {
                Alertas.mostrarError("No se pudo registrar el docente.");
            }
        } else {
            docenteSeleccionado.setNombre_1(n1);
            docenteSeleccionado.setNombre_2(n2);
            docenteSeleccionado.setApellido_1(a1);
            docenteSeleccionado.setApellido_2(a2);

            boolean ok = docentesDAO.actualizarDocente(docenteSeleccionado);
            if (ok) {
                Alertas.mostrarExito("Docente actualizado exitosamente.");
                cargarDocentes();
                limpiarFormularioDocente();
            } else {
                Alertas.mostrarError("No se pudo actualizar el docente.");
            }
        }
    }

    @FXML
    void clickEliminarDocente() {
        if (docenteSeleccionado == null) {
            Alertas.mostrarWarning("Seleccione un docente de la tabla para eliminar.");
            return;
        }

        if (docentesDAO.docenteTieneRegistros(docenteSeleccionado.getId())) {
            Alertas.mostrarWarning("No es posible eliminar al docente '" + docenteSeleccionado.getNombreCompleto() +
                    "' porque tiene prÃƒÂ©stamos o sesiones de biblioteca virtual vinculadas.");
            return;
        }

        boolean confirma = Alertas.mostrarConfirmacion("Ã‚Â¿EstÃƒÂ¡ seguro de eliminar al docente '" +
                docenteSeleccionado.getNombreCompleto() + "'?");
        if (!confirma)
            return;

        boolean ok = docentesDAO.eliminarDocente(docenteSeleccionado.getId());
        if (ok) {
            Alertas.mostrarExito("Docente eliminado exitosamente.");
            cargarDocentes();
            limpiarFormularioDocente();
        } else {
            Alertas.mostrarError("No se pudo eliminar el docente.");
        }
    }

    @FXML
    void clickNuevoDocente() {
        if (tblDocentes != null) {
            tblDocentes.getSelectionModel().clearSelection();
        }
        limpiarFormularioDocente();
    }

    private void limpiarFormularioDocente() {
        docenteSeleccionado = null;
        if (txtDocNombre1 != null)
            txtDocNombre1.clear();
        if (txtDocNombre2 != null)
            txtDocNombre2.clear();
        if (txtDocApellido1 != null)
            txtDocApellido1.clear();
        if (txtDocApellido2 != null)
            txtDocApellido2.clear();
        if (lblTituloFormDocente != null)
            lblTituloFormDocente.setText("GestiÃƒÂ³n de Docente");
        if (lblEstadoEdicionDocente != null)
            lblEstadoEdicionDocente
                    .setText("Complete los datos para registrar o seleccione uno de la tabla para editar.");
        if (btnGuardarDocente != null)
            btnGuardarDocente.setText("Guardar nuevo docente");
        if (btnEliminarDocente != null)
            btnEliminarDocente.setDisable(true);
    }

    private void configurarTablaEditoriales() {
        colEditorialId.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getId())));
        colEditorialNombre.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombre));
        colEditorialEstado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));

        tblEditoriales.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            if (nuevo != null) {
                editorialSeleccionada = nuevo;
                txtEditorialNombre.setText(nuevo.getNombre());
                cbEditorialEstado.setValue(nuevo.getEstado() == 1 ? "Activo" : "Inactivo");
                lblEstadoEdicionEditorial.setText("Editando editorial seleccionada.");
            }
        });

        cbEditorialEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEditorialEstado.setValue("Activo");

        txtBuscarEditorial.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                cargarEditoriales();
            } else {
                String lowerCaseFilter = newVal.toLowerCase();
                java.util.List<Editorial> filtradas = editorialesDAO.obtenerTodas().stream()
                        .filter(e -> e.getNombre().toLowerCase().contains(lowerCaseFilter))
                        .toList();
                tblEditoriales.setItems(FXCollections.observableArrayList(filtradas));
            }
        });
    }

    private void cargarEditoriales() {
        tblEditoriales.setItems(FXCollections.observableArrayList(editorialesDAO.obtenerTodas()));
    }

    @FXML
    void limpiarFormularioEditorial() {
        editorialSeleccionada = null;
        txtEditorialNombre.clear();
        cbEditorialEstado.setValue("Activo");
        lblEstadoEdicionEditorial.setText("Creando nueva editorial.");
        tblEditoriales.getSelectionModel().clearSelection();
    }

    @FXML
    void guardarEditorial() {
        String nombre = txtEditorialNombre.getText();
        if (nombre == null || nombre.trim().isEmpty()) {
            Alertas.mostrarError("El nombre de la editorial es obligatorio.");
            return;
        }
        int estado = "Activo".equals(cbEditorialEstado.getValue()) ? 1 : 0;

        if (editorialSeleccionada == null) {
            Editorial nueva = new Editorial(0, nombre.trim().toUpperCase(), estado);
            if (editorialesDAO.insertarEditorial(nueva)) {
                Alertas.mostrarExito("Editorial creada exitosamente.");
                limpiarFormularioEditorial();
                cargarEditoriales();
            } else {
                Alertas.mostrarError("No se pudo crear la editorial (puede que ya exista).");
            }
        } else {
            editorialSeleccionada.setNombre(nombre.trim().toUpperCase());
            editorialSeleccionada.setEstado(estado);
            if (editorialesDAO.actualizarEditorial(editorialSeleccionada)) {
                Alertas.mostrarExito("Editorial actualizada exitosamente.");
                limpiarFormularioEditorial();
                cargarEditoriales();
            } else {
                Alertas.mostrarError("No se pudo actualizar la editorial.");
            }
        }
    }

    private void configurarTablaCategorias() {
        colCategoriaId.setCellValueFactory(cd -> Bindings.createStringBinding(() -> String.valueOf(cd.getValue().getId())));
        colCategoriaNombre.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getNombreCategoria));
        colCategoriaEstado.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));

        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, nuevo) -> {
            if (nuevo != null) {
                categoriaSeleccionada = nuevo;
                txtCategoriaNombre.setText(nuevo.getNombreCategoria());
                cbCategoriaEstado.setValue(nuevo.getEstado() == 1 ? "Activo" : "Inactivo");
                lblEstadoEdicionCategoria.setText("Editando categoria seleccionada.");
            }
        });

        cbCategoriaEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbCategoriaEstado.setValue("Activo");

        txtBuscarCategoria.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                cargarCategorias();
            } else {
                String lowerCaseFilter = newVal.toLowerCase();
                java.util.List<Categoria> filtradas = categoriasDAO.obtenerTodas().stream()
                        .filter(c -> c.getNombreCategoria().toLowerCase().contains(lowerCaseFilter))
                        .toList();
                tblCategorias.setItems(FXCollections.observableArrayList(filtradas));
            }
        });
    }

    private void cargarCategorias() {
        tblCategorias.setItems(FXCollections.observableArrayList(categoriasDAO.obtenerTodas()));
    }

    @FXML
    void limpiarFormularioCategoria() {
        categoriaSeleccionada = null;
        txtCategoriaNombre.clear();
        cbCategoriaEstado.setValue("Activo");
        lblEstadoEdicionCategoria.setText("Creando nueva categoria.");
        tblCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    void guardarCategoria() {
        String nombre = txtCategoriaNombre.getText();
        if (nombre == null || nombre.trim().isEmpty()) {
            Alertas.mostrarError("El nombre de la categoria es obligatorio.");
            return;
        }
        int estado = "Activo".equals(cbCategoriaEstado.getValue()) ? 1 : 0;

        if (categoriaSeleccionada == null) {
            Categoria nueva = new Categoria(0, nombre.trim().toUpperCase(), estado);
            if (categoriasDAO.insertarCategoria(nueva)) {
                Alertas.mostrarExito("Categoria creada exitosamente.");
                limpiarFormularioCategoria();
                cargarCategorias();
            } else {
                Alertas.mostrarError("No se pudo crear la categoria.");
            }
        } else {
            categoriaSeleccionada.setNombreCategoria(nombre.trim().toUpperCase());
            categoriaSeleccionada.setEstado(estado);
            if (categoriasDAO.actualizarCategoria(categoriaSeleccionada)) {
                Alertas.mostrarExito("Categoria actualizada exitosamente.");
                limpiarFormularioCategoria();
                cargarCategorias();
            } else {
                Alertas.mostrarError("No se pudo actualizar la categoria.");
            }
        }
    }


}