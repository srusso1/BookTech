package controllers.Rectoria;

import database.DocentesDAO;
import database.EstudiantesDAO;
import database.InformesDAO;
import javafx.beans.property.SimpleStringProperty;
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
import reports.generators.BaseReportGenerator;
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
import javafx.concurrent.Task;

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
    private Button btnExportarExcel;

    private final reports.generators.ExcelExportManager excelExportManager = new reports.generators.ExcelExportManager();

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

        colLibro.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(p.getTituloLibro() != null ? p.getTituloLibro() : "N/A");
        });

        colEstudiante.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(p.getEstudiante() != null ? p.getEstudiante() : "N/A");
        });

        colDocente.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(p.getDocente() != null ? p.getDocente().getNombreCompleto() : "N/A");
        });

        colMotivo.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(p.getMotivoPrestamo() != null ? p.getMotivoPrestamo().getNombre() : "N/A");
        });

        colFechaPrestamo.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(p.getFecha_prestamo() != null ? p.getFecha_prestamo() : "N/A");
        });

        colFechaLimite.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(p.getFecha_limite() != null ? p.getFecha_limite() : "N/A");
        });

        colFechaDevolucion.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return new SimpleStringProperty("Pendiente");
            return new SimpleStringProperty(p.getFecha_devolucion() != null ? p.getFecha_devolucion() : "Pendiente");
        });

        colEstado.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return new SimpleStringProperty("--");
            return new SimpleStringProperty(obtenerNombreEstado(p.getEstado()));
        });

        colRegresadoTarde.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return new SimpleStringProperty("--");
            return new SimpleStringProperty(p.getEstado() == ReportConfig.ESTADO_DEVUELTO ? (p.getDevuelto_tarde() == 1 ? "Sí" : "No") : "--");
        });

        colDiasTardanza.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof Prestamo p)) return new SimpleStringProperty("--");
            return new SimpleStringProperty(p.getEstado() == ReportConfig.ESTADO_DEVUELTO ? String.valueOf(p.getDias_atraso()) : "--");
        });
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

        colLibro.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(r.getDocente() != null ? r.getDocente() : "N/A");
        });

        colEstudiante.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(r.getMotivoUso() != null ? r.getMotivoUso() : "N/A");
        });

        colDocente.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(r.getFecha() != null ? r.getFecha() : "N/A");
        });

        colMotivo.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return new SimpleStringProperty("--");
            return new SimpleStringProperty(r.getHoraInicio() != null ? r.getHoraInicio() : "--");
        });

        colFechaPrestamo.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return new SimpleStringProperty("--");
            return new SimpleStringProperty(r.getHoraFin() != null ? r.getHoraFin() : "--");
        });

        colFechaLimite.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return new SimpleStringProperty("0h 0m");
            return new SimpleStringProperty(formatearMinutos(r.getTotalMinutos()));
        });

        colFechaDevolucion.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof RegistroPlataformaDetalle r)) return new SimpleStringProperty("--");
            return new SimpleStringProperty(r.getGrado() > 0 ? String.valueOf(r.getGrado()) : "--");
        });

        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty("Uso plataforma"));
        colRegresadoTarde.setCellValueFactory(cellData -> new SimpleStringProperty("--"));
        colDiasTardanza.setCellValueFactory(cellData -> new SimpleStringProperty("--"));
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

        colLibro.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(i.getTitulo() != null ? i.getTitulo() : "N/A");
        });

        colEstudiante.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(i.getCategoria() != null ? i.getCategoria() : "N/A");
        });

        colDocente.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(i.getAutor() != null ? i.getAutor() : "N/A");
        });

        colMotivo.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(i.getEditorial() != null ? i.getEditorial() : "N/A");
        });

        colFechaPrestamo.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(i.getUbicacion() != null ? i.getUbicacion() : "N/A");
        });

        colFechaLimite.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return new SimpleStringProperty("0");
            return new SimpleStringProperty(String.valueOf(i.getUnidades()));
        });

        colFechaDevolucion.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return new SimpleStringProperty("0");
            return new SimpleStringProperty(String.valueOf(i.getPrestamosActivos()));
        });

        colEstado.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return new SimpleStringProperty("0");
            return new SimpleStringProperty(String.valueOf(i.getStockObjetivo()));
        });

        colRegresadoTarde.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return new SimpleStringProperty("0");
            return new SimpleStringProperty(String.valueOf(i.getRecomendadasComprar()));
        });

        colDiasTardanza.setCellValueFactory(cellData -> {
            if (!(cellData.getValue() instanceof InventarioLibroDetalle i)) return new SimpleStringProperty("--");
            return new SimpleStringProperty(i.getEstadoStock() != null ? i.getEstadoStock() : "--");
        });
    }

    private void cargarDatos() {
        String tipoReporte = cbTipoReporte.getValue();

        if (REPORTE_ESTUDIANTE_PRESTAMOS.equals(tipoReporte)) {
            configurarTablaPrestamos();
        } else if (REPORTE_DEVUELTOS_TARDE.equals(tipoReporte)) {
            configurarTablaPrestamos();
        } else if (REPORTE_PLATAFORMA_GENERAL.equals(tipoReporte)) {
            configurarTablaPlataforma();
        } else if (REPORTE_PLATAFORMA_DOCENTE.equals(tipoReporte)) {
            configurarTablaPlataforma();
        } else if (REPORTE_INVENTARIO.equals(tipoReporte)) {
            configurarTablaInventario();
        } else {
            configurarTablaPrestamos();
        }

        final Integer grado = obtenerGradoSeleccionado();
        final Estudiante estudiante = this.estudianteSeleccionado;
        final Docente docente = this.docenteSeleccionado;
        final String fInicio = this.fechaFiltroInicio;
        final String fFin = this.fechaFiltroFin;

        Task<List<?>> dataTask = new Task<>() {
            @Override
            protected List<?> call() {
                if (REPORTE_ESTUDIANTE_PRESTAMOS.equals(tipoReporte)) {
                    if (estudiante != null) {
                        return (fInicio != null && fFin != null)
                                ? informesDAO.obtenerHistorialEstudiante(estudiante.getId(), fInicio, fFin)
                                : informesDAO.obtenerHistorialEstudiante(estudiante.getId());
                    } else {
                        return List.of();
                    }
                } else if (REPORTE_DEVUELTOS_TARDE.equals(tipoReporte)) {
                    if (fInicio != null && fFin != null) {
                        return informesDAO.obtenerPrestamosDevueltosTarde(fInicio, fFin, grado);
                    } else if (grado != null) {
                        return informesDAO.obtenerPrestamosDevueltosTarde(grado);
                    } else {
                        return informesDAO.obtenerPrestamosDevueltosTarde();
                    }
                } else if (REPORTE_PLATAFORMA_GENERAL.equals(tipoReporte)) {
                    return informesDAO.obtenerRegistrosPlataforma(fInicio, fFin);
                } else if (REPORTE_PLATAFORMA_DOCENTE.equals(tipoReporte)) {
                    if (docente == null) {
                        return List.of();
                    } else {
                        return informesDAO.obtenerRegistrosPlataformaPorDocente(
                                docente.getId(),
                                fInicio,
                                fFin
                        );
                    }
                } else if (REPORTE_INVENTARIO.equals(tipoReporte)) {
                    return informesDAO.obtenerInventarioParaCompra(UMBRAL_STOCK_BAJO);
                } else {
                    return (fInicio != null && fFin != null)
                            ? informesDAO.obtenerTodosPrestamos(fInicio, fFin)
                            : informesDAO.obtenerTodosPrestamos();
                }
            }
        };

        dataTask.setOnSucceeded(e -> {
            tblPrestamos.setItems(FXCollections.observableArrayList(dataTask.getValue()));
        });

        Thread thread = new Thread(dataTask, "Informes-DataLoader-Thread");
        thread.setDaemon(true);
        thread.start();
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

            final BaseReportGenerator generator;

            if (REPORTE_ESTUDIANTE_PRESTAMOS.equals(tipoReporte)) {
                if (estudianteSeleccionado == null) {
                    Alertas.mostrarError("Debe seleccionar un estudiante");
                    return;
                }
                generator = new EstudianteReportGenerator(config, estudianteSeleccionado.getId());

            } else if (REPORTE_DEVUELTOS_TARDE.equals(tipoReporte)) {
                Integer grado = obtenerGradoSeleccionado();
                if (grado != null) {
                    config.setGrado(grado);
                }
                generator = new PrestamosDevueltosTardeReportGenerator(config, grado);

            } else if (REPORTE_PLATAFORMA_GENERAL.equals(tipoReporte)) {
                generator = new GeneralPlataformaReportGenerator(config);

            } else if (REPORTE_PLATAFORMA_DOCENTE.equals(tipoReporte)) {
                if (docenteSeleccionado == null) {
                    Alertas.mostrarError("Debe seleccionar un docente");
                    return;
                }
                generator = new DocentePlataformaReportGenerator(
                        config,
                        docenteSeleccionado.getId(),
                        docenteSeleccionado.getNombreCompleto()
                );

            } else if (REPORTE_INVENTARIO.equals(tipoReporte)) {
                generator = new InventarioReportGenerator(config, UMBRAL_STOCK_BAJO);

            } else {
                generator = new GeneralReportGenerator(config);
            }

            if (generator.isCancelado()) {
                return;
            }

            btnGenerarReporte.setDisable(true);
            btnGenerarReporte.setText("Generando PDF...");

            Task<Void> reportTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    generator.generar();
                    return null;
                }
            };

            reportTask.setOnSucceeded(e -> {
                btnGenerarReporte.setDisable(false);
                btnGenerarReporte.setText("Generar reporte");
                Alertas.mostrarExito("Reporte PDF generado exitosamente en:\n" + generator.getRutaArchivo());
            });

            reportTask.setOnFailed(e -> {
                btnGenerarReporte.setDisable(false);
                btnGenerarReporte.setText("Generar reporte");
                Throwable ex = reportTask.getException();
                Alertas.mostrarError("Error al generar el reporte: " + (ex != null ? ex.getMessage() : "Error desconocido"));
            });

            Thread thread = new Thread(reportTask, "PDF-Generator-Thread");
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            btnGenerarReporte.setDisable(false);
            btnGenerarReporte.setText("Generar reporte");
            Alertas.mostrarError("Error al iniciar el reporte: " + e.getMessage());
        }
    }

    @FXML
    void clickExportarExcel() {
        String tipoReporte = cbTipoReporte.getValue();
        if (tipoReporte == null) {
            Alertas.mostrarError("Seleccione un tipo de reporte.");
            return;
        }

        if (REPORTE_ESTUDIANTE_PRESTAMOS.equals(tipoReporte) && estudianteSeleccionado == null) {
            Alertas.mostrarError("Debe seleccionar un estudiante.");
            return;
        }

        if (REPORTE_PLATAFORMA_DOCENTE.equals(tipoReporte) && docenteSeleccionado == null) {
            Alertas.mostrarError("Debe seleccionar un docente.");
            return;
        }

        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Guardar Reporte en Excel");
        String nombreSugerido = tipoReporte.replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + LocalDate.now() + ".xlsx";
        chooser.setInitialFileName(nombreSugerido);
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"));

        java.io.File file = chooser.showSaveDialog(tblPrestamos.getScene().getWindow());
        if (file == null) {
            return;
        }

        btnExportarExcel.setDisable(true);
        btnExportarExcel.setText("Exportando Excel...");

        final Integer grado = obtenerGradoSeleccionado();
        final Estudiante estudiante = this.estudianteSeleccionado;
        final Docente docente = this.docenteSeleccionado;
        final String fInicio = this.fechaFiltroInicio;
        final String fFin = this.fechaFiltroFin;
        final List<?> datosActuales = new ArrayList<>(tblPrestamos.getItems());

        Task<Void> excelTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                excelExportManager.exportarReporte(
                        tipoReporte,
                        fInicio,
                        fFin,
                        grado,
                        estudiante,
                        docente,
                        datosActuales,
                        file.toPath()
                );
                return null;
            }
        };

        excelTask.setOnSucceeded(e -> {
            btnExportarExcel.setDisable(false);
            btnExportarExcel.setText("Exportar Excel (.xlsx)");
            Alertas.mostrarExito("Reporte Excel generado exitosamente en:\n" + file.getAbsolutePath());
        });

        excelTask.setOnFailed(e -> {
            btnExportarExcel.setDisable(false);
            btnExportarExcel.setText("Exportar Excel (.xlsx)");
            Throwable ex = excelTask.getException();
            Alertas.mostrarError("Error al generar el archivo Excel: " + (ex != null ? ex.getMessage() : "Error desconocido"));
        });

        Thread thread = new Thread(excelTask, "Excel-Export-Thread");
        thread.setDaemon(true);
        thread.start();
    }

    private String obtenerNombreEstado(int estado) {
        try {
            return model.enums.EstadoPrestamo.fromId(estado).getDescripcion();
        } catch (IllegalArgumentException e) {
            return "Desconocido";
        }
    }

    private String formatearMinutos(int minutos) {
        int horas = minutos / 60;
        int restantes = minutos % 60;
        return horas + "h " + restantes + "m";
    }
}

