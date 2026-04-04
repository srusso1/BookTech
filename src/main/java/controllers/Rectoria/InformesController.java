package controllers.Rectoria;

import database.InformesDAO;
import database.EstudiantesDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.Estudiante;
import model.Prestamo;
import reports.generators.EstudianteReportGenerator;
import reports.generators.GeneralReportGenerator;
import reports.generators.PrestamosDevueltosTardeReportGenerator;
import reports.models.ReportConfig;
import utils.Alertas;
import utils.BusquedaSugerencias;
import utils.Fechas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para el módulo de Informes
 * Permite generar reportes PDF de préstamos con filtros por fecha
 */
public class InformesController {

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblDescripcion;

    @FXML
    private HBox filtrosHBox;

    @FXML
    private DatePicker dpFechaInicio;

    @FXML
    private DatePicker dpFechaFin;

    @FXML
    private ComboBox<String> cbTipoReporte;

    @FXML
    private TextField txtEstudiante;

    @FXML
    private Label lblGrado;

    @FXML
    private ComboBox<String> cbGrado;

    @FXML
    private Button btnFiltrar;

    @FXML
    private Button btnLimpiarFiltro;

    @FXML
    private Button btnGenerarReporte;

    @FXML
    private TableView<Prestamo> tblPrestamos;

    @FXML
    private TableColumn<Prestamo, String> colLibro;

    @FXML
    private TableColumn<Prestamo, String> colEstudiante;

    @FXML
    private TableColumn<Prestamo, String> colDocente;

    @FXML
    private TableColumn<Prestamo, String> colFechaPrestamo;

    @FXML
    private TableColumn<Prestamo, String> colFechaLimite;

    @FXML
    private TableColumn<Prestamo, String> colFechaDevolucion;

    @FXML
    private TableColumn<Prestamo, String> colEstado;

    @FXML
    private TableColumn<Prestamo, String> colRegresadoTarde;

    @FXML
    private TableColumn<Prestamo, String> colDiasTardanza;

    private final InformesDAO informesDAO = new InformesDAO();
    private final EstudiantesDAO estudiantesDAO = new EstudiantesDAO();
    private final ContextMenu sugerenciasEstudiante = new ContextMenu();
    private ArrayList<Estudiante> listaEstudiantes = new ArrayList<>();
    private Estudiante estudianteSeleccionado;

    private String fechaFiltroInicio = null;
    private String fechaFiltroFin = null;

    @FXML
    public void initialize() {
        // Establecer fechas por defecto (mes actual)
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.withDayOfMonth(1));
        dpFechaFin.setValue(hoy);

        // Configurar ComboBox de tipos de reporte
        ObservableList<String> tiposReporte = FXCollections.observableArrayList(
                "Reporte General",
                "Reporte por Estudiante",
                "Reporte préstamos devueltos tarde"
        );
        cbTipoReporte.setItems(tiposReporte);
        cbTipoReporte.setValue("Reporte General");
        cbTipoReporte.setOnAction(e -> actualizarVistaPorTipoReporte());

        listaEstudiantes = estudiantesDAO.obtenerEstudiantes();
        configurarBusquedaEstudiante();
        cargarGrados();

        // Configurar tabla
        configurarTabla();

        // Cargar datos iniciales
        cargarDatos();
    }

    /**
     * Actualiza la vista según el tipo de reporte seleccionado
     */
    private void actualizarVistaPorTipoReporte() {
        String tipoSeleccionado = cbTipoReporte.getValue();

        if ("Reporte por Estudiante".equals(tipoSeleccionado)) {
            txtEstudiante.setVisible(true);
            txtEstudiante.setManaged(true);
            lblGrado.setVisible(false);
            lblGrado.setManaged(false);
            cbGrado.setVisible(false);
            cbGrado.setManaged(false);
        } else if ("Reporte préstamos devueltos tarde".equals(tipoSeleccionado)) {
            txtEstudiante.clear();
            txtEstudiante.setVisible(false);
            txtEstudiante.setManaged(false);
            estudianteSeleccionado = null;

            lblGrado.setVisible(true);
            lblGrado.setManaged(true);
            cbGrado.setVisible(true);
            cbGrado.setManaged(true);
        } else {
            txtEstudiante.clear();
            txtEstudiante.setVisible(false);
            txtEstudiante.setManaged(false);
            estudianteSeleccionado = null;
            lblGrado.setVisible(false);
            lblGrado.setManaged(false);
            cbGrado.setVisible(false);
            cbGrado.setManaged(false);
        }

        cargarDatos();
    }

    private void cargarGrados() {
        List<String> grados = new ArrayList<>();
        grados.add("Todos");
        estudiantesDAO.obtenerGrados().forEach(g -> grados.add(String.valueOf(g)));
        cbGrado.setItems(FXCollections.observableArrayList(grados));
        cbGrado.setValue("Todos");
    }

    private Integer obtenerGradoSeleccionado() {
        String grado = cbGrado.getValue();
        if (grado == null || "Todos".equalsIgnoreCase(grado)) {
            return null;
        }
        try {
            return Integer.parseInt(grado);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void configurarBusquedaEstudiante() {
        BusquedaSugerencias.configurar(
                txtEstudiante,
                sugerenciasEstudiante,
                listaEstudiantes,
                2,
                6,
                Estudiante::getNombreCompleto,
                Estudiante::getNombreCompletoYGrado,
                Estudiante::getNombreCompleto,
                est -> estudianteSeleccionado = est,
                () -> estudianteSeleccionado = null
        );

        txtEstudiante.textProperty().addListener((obs, oldText, newText) -> {
            if (estudianteSeleccionado == null) {
                return;
            }
            String actual = newText == null ? "" : newText.trim();
            if (!actual.equalsIgnoreCase(estudianteSeleccionado.getNombreCompleto())) {
                estudianteSeleccionado = null;
            }
        });
    }

    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colLibro.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getTituloLibro() != null
                        ? cellData.getValue().getTituloLibro()
                        : "N/A"
        ));

        colEstudiante.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getEstudiante() != null
                        ? cellData.getValue().getEstudiante()
                        : "N/A"
        ));

        colDocente.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getDocente() != null
                        ? cellData.getValue().getDocente().getNombreCompleto()
                        : "N/A"
        ));

        colFechaPrestamo.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getFecha_prestamo() != null
                        ? cellData.getValue().getFecha_prestamo()
                        : "N/A"
        ));

        colFechaLimite.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getFecha_limite() != null
                        ? cellData.getValue().getFecha_limite()
                        : "N/A"
        ));

        colFechaDevolucion.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getFecha_devolucion() != null
                        ? cellData.getValue().getFecha_devolucion()
                        : "Pendiente"
        ));

        colEstado.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> obtenerNombreEstado(cellData.getValue().getEstado())
        ));

        colRegresadoTarde.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getEstado() == ReportConfig.ESTADO_DEVUELTO
                        ? (cellData.getValue().getDevuelto_tarde() == 1 ? "Sí" : "No")
                        : "--"
        ));

        colDiasTardanza.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getEstado() == ReportConfig.ESTADO_DEVUELTO
                        ? String.valueOf(cellData.getValue().getDias_atraso())
                        : "--"
        ));
    }

    /**
     * Carga los datos de préstamos en la tabla
     */
    private void cargarDatos() {
        List<Prestamo> prestamos;

        String tipoReporte = cbTipoReporte.getValue();

        if ("Reporte por Estudiante".equals(tipoReporte)) {
            if (estudianteSeleccionado != null) {
                int idEstudiante = estudianteSeleccionado.getId();
                prestamos = (fechaFiltroInicio != null && fechaFiltroFin != null) ?
                        informesDAO.obtenerHistorialEstudiante(idEstudiante, fechaFiltroInicio, fechaFiltroFin) :
                        informesDAO.obtenerHistorialEstudiante(idEstudiante);
            } else {
                prestamos = List.of();
            }
        } else if ("Reporte préstamos devueltos tarde".equals(tipoReporte)) {
            Integer grado = obtenerGradoSeleccionado();
            if (fechaFiltroInicio != null && fechaFiltroFin != null) {
                prestamos = informesDAO.obtenerPrestamosDevueltosTarde(fechaFiltroInicio, fechaFiltroFin, grado);
            } else if (grado != null) {
                prestamos = informesDAO.obtenerPrestamosDevueltosTarde(grado);
            } else {
                prestamos = informesDAO.obtenerPrestamosDevueltosTarde();
            }
        } else {
            prestamos = (fechaFiltroInicio != null && fechaFiltroFin != null) ?
                    informesDAO.obtenerTodosPrestamos(fechaFiltroInicio, fechaFiltroFin) :
                    informesDAO.obtenerTodosPrestamos();
        }

        ObservableList<Prestamo> datos = FXCollections.observableArrayList(prestamos);
        tblPrestamos.setItems(datos);
    }

    /**
     * Acción del botón Filtrar
     */
    @FXML
    void clickFiltrar() {
        LocalDate fechaInicio = dpFechaInicio.getValue();
        LocalDate fechaFin = dpFechaFin.getValue();

        if (fechaInicio == null || fechaFin == null) {
            Alertas.mostrarError("Debe seleccionar ambas fechas");
            return;
        }

        if (fechaInicio.isAfter(fechaFin)) {
            Alertas.mostrarError("La fecha inicio no puede ser mayor que la fecha fin");
            return;
        }

        this.fechaFiltroInicio = Fechas.convertirAISO(fechaInicio);
        this.fechaFiltroFin = Fechas.convertirAISO(fechaFin);

        cargarDatos();
        Alertas.mostrarExito("Filtro aplicado correctamente");
    }

    /**
     * Acción del botón Limpiar Filtro
     */
    @FXML
    void clickLimpiarFiltro() {
        this.fechaFiltroInicio = null;
        this.fechaFiltroFin = null;

        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.withDayOfMonth(1));
        dpFechaFin.setValue(hoy);

        cargarDatos();
        Alertas.mostrarExito("Filtro limpiado, mostrando datos sin restricción de fechas");
    }

    /**
     * Acción del botón Generar Reporte
     */
    @FXML
    void clickGenerarReporte() {
        String tipoReporte = cbTipoReporte.getValue();

        try {
            ReportConfig config = new ReportConfig();
            if (fechaFiltroInicio != null && fechaFiltroFin != null) {
                config.setFechaInicio(dpFechaInicio.getValue());
                config.setFechaFin(dpFechaFin.getValue());
            }

            if ("Reporte por Estudiante".equals(tipoReporte)) {
                if (estudianteSeleccionado == null) {
                    Alertas.mostrarError("Debe seleccionar un estudiante");
                    return;
                }

                EstudianteReportGenerator generador = new EstudianteReportGenerator(config, estudianteSeleccionado.getId());
                generador.generar();

            } else if ("Reporte préstamos devueltos tarde".equals(tipoReporte)) {
                Integer grado = obtenerGradoSeleccionado();
                if (grado != null) {
                    config.setGrado(grado);
                }
                PrestamosDevueltosTardeReportGenerator generador = new PrestamosDevueltosTardeReportGenerator(config, grado);
                generador.generar();

            } else {
                GeneralReportGenerator generador = new GeneralReportGenerator(config);
                generador.generar();
            }

        } catch (Exception e) {
            Alertas.mostrarError("Error al generar el reporte: " + e.getMessage());
        }
    }

    /**
     * Obtiene el nombre legible del estado del préstamo
     */
    private String obtenerNombreEstado(int estado) {
        return switch (estado) {
            case 0 -> "Prestado";
            case 1 -> "Devuelto";
            case 2 -> "Pendiente";
            default -> "Desconocido";
        };
    }
}



