package controllers.Rectoria;

import database.DocentesDAO;
import database.EstudiantesDAO;
import database.InformesDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.Docente;
import model.Estudiante;
import model.InventarioLibroDetalle;
import model.Prestamo;
import model.RegistroPlataformaDetalle;
import reports.generators.DocentePlataformaReportGenerator;
import reports.generators.EstudianteReportGenerator;
import reports.generators.GeneralPlataformaReportGenerator;
import reports.generators.GeneralReportGenerator;
import reports.generators.InventarioReportGenerator;
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
 */
public class InformesController {

    private static final String REPORTE_GENERAL_PRESTAMOS = "Reporte general prestamos de libros";
    private static final String REPORTE_ESTUDIANTE_PRESTAMOS = "Reporte por estudiante prestamos de libros";
    private static final String REPORTE_DEVUELTOS_TARDE = "Reporte prestamos devueltos tarde";
    private static final String REPORTE_PLATAFORMA_GENERAL = "Reporte plataforma virtual general";
    private static final String REPORTE_PLATAFORMA_DOCENTE = "Reporte plataforma virtual por docente";
    private static final String REPORTE_INVENTARIO = "Reporte de inventario";
    private static final int UMBRAL_STOCK_BAJO = 3;

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
    private TextField txtDocente;

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
    private Label lblDatosTabla;

    @FXML
    private CheckBox chkIncluirTablas;

    @FXML
    private TableView<Object> tblPrestamos;

    @FXML
    private TableColumn<Object, String> colLibro;

    @FXML
    private TableColumn<Object, String> colEstudiante;

    @FXML
    private TableColumn<Object, String> colDocente;

    @FXML
    private TableColumn<Object, String> colMotivo;

    @FXML
    private TableColumn<Object, String> colFechaPrestamo;

    @FXML
    private TableColumn<Object, String> colFechaLimite;

    @FXML
    private TableColumn<Object, String> colFechaDevolucion;

    @FXML
    private TableColumn<Object, String> colEstado;

    @FXML
    private TableColumn<Object, String> colRegresadoTarde;

    @FXML
    private TableColumn<Object, String> colDiasTardanza;

    private final InformesDAO informesDAO = new InformesDAO();
    private final EstudiantesDAO estudiantesDAO = new EstudiantesDAO();
    private final DocentesDAO docentesDAO = new DocentesDAO();

    private final ContextMenu sugerenciasEstudiante = new ContextMenu();
    private final ContextMenu sugerenciasDocente = new ContextMenu();

    private ArrayList<Estudiante> listaEstudiantes = new ArrayList<>();
    private ArrayList<Docente> listaDocentes = new ArrayList<>();

    private Estudiante estudianteSeleccionado;
    private Docente docenteSeleccionado;

    private String fechaFiltroInicio = null;
    private String fechaFiltroFin = null;

    @FXML
    public void initialize() {
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.withDayOfMonth(1));
        dpFechaFin.setValue(hoy);

        ObservableList<String> tiposReporte = FXCollections.observableArrayList(
                REPORTE_GENERAL_PRESTAMOS,
                REPORTE_ESTUDIANTE_PRESTAMOS,
                REPORTE_DEVUELTOS_TARDE,
                REPORTE_PLATAFORMA_GENERAL,
                REPORTE_PLATAFORMA_DOCENTE,
                REPORTE_INVENTARIO
        );
        cbTipoReporte.setItems(tiposReporte);
        cbTipoReporte.setValue(REPORTE_GENERAL_PRESTAMOS);
        cbTipoReporte.setOnAction(e -> actualizarVistaPorTipoReporte());

        listaEstudiantes = estudiantesDAO.obtenerEstudiantes();
        listaDocentes = docentesDAO.obtenerDocentes();

        configurarBusquedaEstudiante();
        configurarBusquedaDocente();
        cargarGrados();

        configurarTablaPrestamos();
        actualizarTituloTabla();
        cargarDatos();
    }

    private void actualizarVistaPorTipoReporte() {
        String tipo = cbTipoReporte.getValue();

        boolean porEstudiante = REPORTE_ESTUDIANTE_PRESTAMOS.equals(tipo);
        boolean porGradoTarde = REPORTE_DEVUELTOS_TARDE.equals(tipo);
        boolean porDocentePlataforma = REPORTE_PLATAFORMA_DOCENTE.equals(tipo);
        boolean reporteInventario = REPORTE_INVENTARIO.equals(tipo);

        txtEstudiante.setVisible(porEstudiante);
        txtEstudiante.setManaged(porEstudiante);
        if (!porEstudiante) {
            txtEstudiante.clear();
            estudianteSeleccionado = null;
        }

        txtDocente.setVisible(porDocentePlataforma);
        txtDocente.setManaged(porDocentePlataforma);
        if (!porDocentePlataforma) {
            txtDocente.clear();
            docenteSeleccionado = null;
        }

        lblGrado.setVisible(porGradoTarde);
        lblGrado.setManaged(porGradoTarde);
        cbGrado.setVisible(porGradoTarde);
        cbGrado.setManaged(porGradoTarde);

        dpFechaInicio.setDisable(reporteInventario);
        dpFechaFin.setDisable(reporteInventario);
        btnFiltrar.setDisable(reporteInventario);
        btnLimpiarFiltro.setDisable(reporteInventario);

        if (reporteInventario) {
            fechaFiltroInicio = null;
            fechaFiltroFin = null;
        }

        actualizarTituloTabla();

        cargarDatos();
    }

    private void actualizarTituloTabla() {
        String tipo = cbTipoReporte.getValue();
        if (lblDatosTabla == null) {
            return;
        }

        if (REPORTE_PLATAFORMA_GENERAL.equals(tipo) || REPORTE_PLATAFORMA_DOCENTE.equals(tipo)) {
            lblDatosTabla.setText("Datos de Plataforma Virtual");
        } else if (REPORTE_INVENTARIO.equals(tipo)) {
            lblDatosTabla.setText("Datos de Inventario");
        } else {
            lblDatosTabla.setText("Datos de Préstamos");
        }
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
                est -> {
                    estudianteSeleccionado = est;
                    cargarDatos();
                },
                () -> {
                    estudianteSeleccionado = null;
                    cargarDatos();
                }
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

    private void configurarBusquedaDocente() {
        BusquedaSugerencias.configurar(
                txtDocente,
                sugerenciasDocente,
                listaDocentes,
                2,
                6,
                Docente::getNombreCompleto,
                Docente::getNombreCompleto,
                Docente::getNombreCompleto,
                doc -> {
                    docenteSeleccionado = doc;
                    cargarDatos();
                },
                () -> {
                    docenteSeleccionado = null;
                    cargarDatos();
                }
        );

        txtDocente.textProperty().addListener((obs, oldText, newText) -> {
            if (docenteSeleccionado == null) {
                return;
            }
            String actual = newText == null ? "" : newText.trim();
            if (!actual.equalsIgnoreCase(docenteSeleccionado.getNombreCompleto())) {
                docenteSeleccionado = null;
            }
        });
    }

    private void configurarTablaPrestamos() {
        colLibro.setText("Libro");
        colEstudiante.setText("Estudiante");
        colDocente.setText("Docente");
        colMotivo.setText("Motivo");
        colFechaPrestamo.setText("Fecha Préstamo");
        colFechaLimite.setText("Fecha Límite");
        colFechaDevolucion.setText("Fecha Devolución");
        colEstado.setText("Estado");
        colRegresadoTarde.setText("Regresado tarde");
        colDiasTardanza.setText("Días de tardanza");

        colLibro.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return "N/A";
            return p.getTituloLibro() != null ? p.getTituloLibro() : "N/A";
        }));

        colEstudiante.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return "N/A";
            return p.getEstudiante() != null ? p.getEstudiante() : "N/A";
        }));

        colDocente.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return "N/A";
            return p.getDocente() != null ? p.getDocente().getNombreCompleto() : "N/A";
        }));

        colMotivo.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return "N/A";
            return p.getMotivoPrestamo() != null ? p.getMotivoPrestamo().getNombre() : "N/A";
        }));

        colFechaPrestamo.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return "N/A";
            return p.getFecha_prestamo() != null ? p.getFecha_prestamo() : "N/A";
        }));

        colFechaLimite.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return "N/A";
            return p.getFecha_limite() != null ? p.getFecha_limite() : "N/A";
        }));

        colFechaDevolucion.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return "Pendiente";
            return p.getFecha_devolucion() != null ? p.getFecha_devolucion() : "Pendiente";
        }));

        colEstado.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return "--";
            return obtenerNombreEstado(p.getEstado());
        }));

        colRegresadoTarde.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return "--";
            return p.getEstado() == ReportConfig.ESTADO_DEVUELTO ? (p.getDevuelto_tarde() == 1 ? "Sí" : "No") : "--";
        }));

        colDiasTardanza.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return "--";
            return p.getEstado() == ReportConfig.ESTADO_DEVUELTO ? String.valueOf(p.getDias_atraso()) : "--";
        }));
    }

    private void configurarTablaPlataforma() {
        colLibro.setText("Docente");
        colEstudiante.setText("Motivo de uso");
        colDocente.setText("Fecha");
        colMotivo.setText("Hora inicio");
        colFechaPrestamo.setText("Hora fin");
        colFechaLimite.setText("Duración");
        colFechaDevolucion.setText("Grado");
        colEstado.setText("Tipo");
        colRegresadoTarde.setText("--");
        colDiasTardanza.setText("--");

        colLibro.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return "N/A";
            return r.getDocente() != null ? r.getDocente() : "N/A";
        }));

        colEstudiante.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return "N/A";
            return r.getMotivoUso() != null ? r.getMotivoUso() : "N/A";
        }));

        colDocente.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return "N/A";
            return r.getFecha() != null ? r.getFecha() : "N/A";
        }));

        colMotivo.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return "--";
            return r.getHoraInicio() != null ? r.getHoraInicio() : "--";
        }));

        colFechaPrestamo.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return "--";
            return r.getHoraFin() != null ? r.getHoraFin() : "--";
        }));

        colFechaLimite.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return "0h 0m";
            return formatearMinutos(r.getTotalMinutos());
        }));

        colFechaDevolucion.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return "--";
            return r.getGrado() > 0 ? String.valueOf(r.getGrado()) : "--";
        }));

        colEstado.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> "Uso plataforma"));
        colRegresadoTarde.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> "--"));
        colDiasTardanza.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> "--"));
    }

    private void configurarTablaInventario() {
        colLibro.setText("Libro");
        colEstudiante.setText("Categoría");
        colDocente.setText("Autor");
        colMotivo.setText("Editorial");
        colFechaPrestamo.setText("Ubicación");
        colFechaLimite.setText("Unidades");
        colFechaDevolucion.setText("Préstamos activos");
        colEstado.setText("Stock objetivo");
        colRegresadoTarde.setText("Comprar");
        colDiasTardanza.setText("Estado");

        colLibro.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return "N/A";
            return i.getTitulo() != null ? i.getTitulo() : "N/A";
        }));

        colEstudiante.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return "N/A";
            return i.getCategoria() != null ? i.getCategoria() : "N/A";
        }));

        colDocente.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return "N/A";
            return i.getAutor() != null ? i.getAutor() : "N/A";
        }));

        colMotivo.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return "N/A";
            return i.getEditorial() != null ? i.getEditorial() : "N/A";
        }));

        colFechaPrestamo.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return "N/A";
            return i.getUbicacion() != null ? i.getUbicacion() : "N/A";
        }));

        colFechaLimite.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return "0";
            return String.valueOf(i.getUnidades());
        }));

        colFechaDevolucion.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return "0";
            return String.valueOf(i.getPrestamosActivos());
        }));

        colEstado.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return "0";
            return String.valueOf(i.getStockObjetivo());
        }));

        colRegresadoTarde.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return "0";
            return String.valueOf(i.getRecomendadasComprar());
        }));

        colDiasTardanza.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return "--";
            return i.getEstadoStock();
        }));
    }

    private void cargarDatos() {
        String tipoReporte = cbTipoReporte.getValue();
        List<?> datos;

        if (REPORTE_ESTUDIANTE_PRESTAMOS.equals(tipoReporte)) {
            configurarTablaPrestamos();
            if (estudianteSeleccionado != null) {
                int idEstudiante = estudianteSeleccionado.getId();
                datos = (fechaFiltroInicio != null && fechaFiltroFin != null)
                        ? informesDAO.obtenerHistorialEstudiante(idEstudiante, fechaFiltroInicio, fechaFiltroFin)
                        : informesDAO.obtenerHistorialEstudiante(idEstudiante);
            } else {
                datos = List.of();
            }
        } else if (REPORTE_DEVUELTOS_TARDE.equals(tipoReporte)) {
            configurarTablaPrestamos();
            Integer grado = obtenerGradoSeleccionado();
            if (fechaFiltroInicio != null && fechaFiltroFin != null) {
                datos = informesDAO.obtenerPrestamosDevueltosTarde(fechaFiltroInicio, fechaFiltroFin, grado);
            } else if (grado != null) {
                datos = informesDAO.obtenerPrestamosDevueltosTarde(grado);
            } else {
                datos = informesDAO.obtenerPrestamosDevueltosTarde();
            }
        } else if (REPORTE_PLATAFORMA_GENERAL.equals(tipoReporte)) {
            configurarTablaPlataforma();
            datos = informesDAO.obtenerRegistrosPlataforma(fechaFiltroInicio, fechaFiltroFin);
        } else if (REPORTE_PLATAFORMA_DOCENTE.equals(tipoReporte)) {
            configurarTablaPlataforma();
            if (docenteSeleccionado == null) {
                datos = List.of();
            } else {
                datos = informesDAO.obtenerRegistrosPlataformaPorDocente(
                        docenteSeleccionado.getId(),
                        fechaFiltroInicio,
                        fechaFiltroFin
                );
            }
        } else if (REPORTE_INVENTARIO.equals(tipoReporte)) {
            configurarTablaInventario();
            datos = informesDAO.obtenerInventarioParaCompra(UMBRAL_STOCK_BAJO);
        } else {
            configurarTablaPrestamos();
            datos = (fechaFiltroInicio != null && fechaFiltroFin != null)
                    ? informesDAO.obtenerTodosPrestamos(fechaFiltroInicio, fechaFiltroFin)
                    : informesDAO.obtenerTodosPrestamos();
        }

        tblPrestamos.setItems(FXCollections.observableArrayList(datos));
    }

    @FXML
    void clickFiltrar() {
        if (REPORTE_INVENTARIO.equals(cbTipoReporte.getValue())) {
            Alertas.mostrarExito("El reporte de inventario no usa filtro por fechas.");
            return;
        }

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

    @FXML
    void clickLimpiarFiltro() {
        if (REPORTE_INVENTARIO.equals(cbTipoReporte.getValue())) {
            cargarDatos();
            Alertas.mostrarExito("Inventario actualizado sin filtro de fechas.");
            return;
        }

        this.fechaFiltroInicio = null;
        this.fechaFiltroFin = null;

        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy.withDayOfMonth(1));
        dpFechaFin.setValue(hoy);

        cargarDatos();
        Alertas.mostrarExito("Filtro limpiado, mostrando datos sin restricción de fechas");
    }

    @FXML
    void clickGenerarReporte() {
        String tipoReporte = cbTipoReporte.getValue();

        try {
            ReportConfig config = new ReportConfig();
            config.setIncluirTablas(chkIncluirTablas == null || chkIncluirTablas.isSelected());
            if (fechaFiltroInicio != null && fechaFiltroFin != null) {
                config.setFechaInicio(dpFechaInicio.getValue());
                config.setFechaFin(dpFechaFin.getValue());
            }

            if (REPORTE_ESTUDIANTE_PRESTAMOS.equals(tipoReporte)) {
                if (estudianteSeleccionado == null) {
                    Alertas.mostrarError("Debe seleccionar un estudiante");
                    return;
                }
                new EstudianteReportGenerator(config, estudianteSeleccionado.getId()).generar();

            } else if (REPORTE_DEVUELTOS_TARDE.equals(tipoReporte)) {
                Integer grado = obtenerGradoSeleccionado();
                if (grado != null) {
                    config.setGrado(grado);
                }
                new PrestamosDevueltosTardeReportGenerator(config, grado).generar();

            } else if (REPORTE_PLATAFORMA_GENERAL.equals(tipoReporte)) {
                new GeneralPlataformaReportGenerator(config).generar();

            } else if (REPORTE_PLATAFORMA_DOCENTE.equals(tipoReporte)) {
                if (docenteSeleccionado == null) {
                    Alertas.mostrarError("Debe seleccionar un docente");
                    return;
                }
                new DocentePlataformaReportGenerator(
                        config,
                        docenteSeleccionado.getId(),
                        docenteSeleccionado.getNombreCompleto()
                ).generar();

            } else if (REPORTE_INVENTARIO.equals(tipoReporte)) {
                new InventarioReportGenerator(config, UMBRAL_STOCK_BAJO).generar();

            } else {
                new GeneralReportGenerator(config).generar();
            }

        } catch (Exception e) {
            Alertas.mostrarError("Error al generar el reporte: " + e.getMessage());
        }
    }

    private String obtenerNombreEstado(int estado) {
        return switch (estado) {
            case 0 -> "Prestado";
            case 1 -> "Devuelto";
            case 2 -> "Pendiente";
            default -> "Desconocido";
        };
    }

    private String formatearMinutos(int minutos) {
        int horas = minutos / 60;
        int restantes = minutos % 60;
        return horas + "h " + restantes + "m";
    }
}

