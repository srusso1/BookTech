package controllers.Bibliotecario;

import database.DocentesDAO;
import database.EstudiantesDAO;
import database.MotivosPlataformaDAO;
import database.RegistroPlataformaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Docente;
import model.MotivoPlataforma;
import utils.Alertas;
import utils.BusquedaSugerencias;
import utils.GeneradorHoras;

import java.util.ArrayList;
import java.util.List;

public class PlataformaVirtualController {

    public PlataformaVirtualController(DocentesDAO docentesDAO, EstudiantesDAO estudiantesDAO, MotivosPlataformaDAO motivosPlataformaDAO, RegistroPlataformaDAO registroPlataformaDAO) {
        this.docentesDAO = docentesDAO;
        this.estudiantesDAO = estudiantesDAO;
        this.motivosPlataformaDAO = motivosPlataformaDAO;
        this.registroPlataformaDAO = registroPlataformaDAO;
    }

    @FXML
    private TextField txtDocente;

    @FXML
    private ComboBox<MotivoPlataforma> comboMotivoUso;

    @FXML
    private ComboBox<Integer> comboGrados;

    @FXML
    private Spinner<String> spinnerInicio;

    @FXML
    private Spinner<String> spinnerFin;

    @FXML
    private Label lblHorasCalculadas;

    private final DocentesDAO docentesDAO;
    private final EstudiantesDAO estudiantesDAO;
    private final MotivosPlataformaDAO motivosPlataformaDAO;
    private final ArrayList<Docente> listaDocentes = new ArrayList<>();
    private final ContextMenu sugerenciasDocente = new ContextMenu();
    private Docente docenteSeleccionado;
    private final RegistroPlataformaDAO registroPlataformaDAO;

    @FXML
    void initialize() {
        listaDocentes.addAll(docentesDAO.obtenerDocentes());
        List<Integer> grados = estudiantesDAO.obtenerGrados();
        List<MotivoPlataforma> motivos = motivosPlataformaDAO.obtenerMotivosPlataformaActivos();
        comboGrados.getItems().addAll(grados);
        comboMotivoUso.getItems().setAll(motivos);
        if (motivos.isEmpty()) {
            Alertas.mostrarError("No hay motivos de plataforma activos. Solicite activarlos en Configuración.");
        }
        txtDocente.setContextMenu(sugerenciasDocente);

        BusquedaSugerencias.configurar(
                txtDocente,
                sugerenciasDocente,
                listaDocentes,
                2,
                5,
                Docente::getNombreCompleto,
                Docente::getNombreCompleto,
                Docente::getNombreCompleto,
                doc -> docenteSeleccionado = doc,
                null
        );

        configurarSpinners();
    }

    private void configurarSpinners() {
        // Generar lista de horas desde 6:00 hasta 14:00 con incremento de 10 minutos
        List<String> horas = GeneradorHoras.generarHoras();

        // Configurar Spinner de inicio (por defecto 6:00)
        SpinnerValueFactory<String> factoryInicio = new SpinnerValueFactory.ListSpinnerValueFactory<>(
                FXCollections.observableArrayList(horas)
        );
        factoryInicio.setValue("06:00");
        spinnerInicio.setValueFactory(factoryInicio);
        spinnerInicio.setEditable(false);

        // Configurar Spinner de fin (por defecto 8:00)
        SpinnerValueFactory<String> factoryFin = new SpinnerValueFactory.ListSpinnerValueFactory<>(
                FXCollections.observableArrayList(horas)
        );
        factoryFin.setValue("08:00");
        spinnerFin.setValueFactory(factoryFin);
        spinnerFin.setEditable(false);

        // Listeners para calcular diferencia automíƒ¡ticamente
        spinnerInicio.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (obs != null && newVal != null && !newVal.equals(oldVal)) {
                actualizarDiferencia();
            }
        });

        spinnerFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (obs != null && newVal != null && !newVal.equals(oldVal)) {
                actualizarDiferencia();
            }
        });

        // Calcular inicial
        actualizarDiferencia();
    }

    private void actualizarDiferencia() {
        String horaInicio = spinnerInicio.getValue();
        String horaFin = spinnerFin.getValue();

        if (horaInicio != null && horaFin != null) {
            int totalMinutos = GeneradorHoras.calcularDiferenciaMinutos(horaInicio, horaFin);
            String formatted = GeneradorHoras.formatearMinutos(totalMinutos);
            lblHorasCalculadas.setText("Tiempo: " + formatted);
        }
    }

    @FXML
    void clickRegistrar() {
        if (docenteSeleccionado == null) {
            Alertas.mostrarError("Es necesario seleccionar un docente");
            return;
        }

        if (comboGrados.getValue() == null) {
            Alertas.mostrarError("Es necesario seleccionar un grado");
            return;
        }

        if (comboMotivoUso.getValue() == null) {
            Alertas.mostrarError("Es necesario seleccionar un motivo de uso");
            return;
        }

        int id_docente = docenteSeleccionado.getId();
        int idMotivoUso = comboMotivoUso.getValue().getId();
        String horaInicio = spinnerInicio.getValue();
        String horaFin = spinnerFin.getValue();
        int totalMinutos = GeneradorHoras.calcularDiferenciaMinutos(horaInicio, horaFin);
        int grado = comboGrados.getValue();

        if (totalMinutos <= 0) {
            Alertas.mostrarError("La hora fin debe ser mayor que la hora inicio");
            return;
        }

        if(registroPlataformaDAO.registrarUsoConHorasYGrado(id_docente, idMotivoUso, horaInicio, horaFin, totalMinutos, grado)){
            Alertas.mostrarExito("Registro de plataforma realizado correctamente");
            limpiarCampos();
        } else {
            Alertas.mostrarError("Error al registrar el uso. Intente nuevamente.");
        }

    }

    private void limpiarCampos() {
        txtDocente.clear();
        comboMotivoUso.setValue(null);
        spinnerInicio.getValueFactory().setValue("06:00");
        spinnerFin.getValueFactory().setValue("08:00");
        docenteSeleccionado = null;
        comboGrados.setValue(null);
        actualizarDiferencia();
    }
}
