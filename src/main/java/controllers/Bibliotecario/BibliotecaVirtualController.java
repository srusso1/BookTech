package controllers.Bibliotecario;

import database.DocentesDAO;
import database.RegistroPlataformaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import model.Docente;
import utils.Alertas;
import utils.BusquedaSugerencias;
import utils.GeneradorHoras;
import utils.Validaciones;

import java.util.ArrayList;
import java.util.List;

public class BibliotecaVirtualController {

    @FXML
    private TextField txtDocente;

    @FXML
    private TextField txtUso;

    @FXML
    private Spinner<String> spinnerInicio;

    @FXML
    private Spinner<String> spinnerFin;

    @FXML
    private Label lblHorasCalculadas;

    private final DocentesDAO docentesDAO = new DocentesDAO();
    private final ArrayList<Docente> listaDocentes = new ArrayList<>();
    private final ContextMenu sugerenciasDocente = new ContextMenu();
    private Docente docenteSeleccionado;
    private RegistroPlataformaDAO registroPlataformaDAO = new RegistroPlataformaDAO();

    @FXML
    void initialize() {
        listaDocentes.addAll(docentesDAO.obtenerDocentes());
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
        spinnerInicio.setEditable(true);

        // Configurar Spinner de fin (por defecto 8:00)
        SpinnerValueFactory<String> factoryFin = new SpinnerValueFactory.ListSpinnerValueFactory<>(
                FXCollections.observableArrayList(horas)
        );
        factoryFin.setValue("08:00");
        spinnerFin.setValueFactory(factoryFin);
        spinnerFin.setEditable(true);

        // Listeners para calcular diferencia automáticamente
        spinnerInicio.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarDiferencia();
        });

        spinnerFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarDiferencia();
        });

        // Calcular inicial
        actualizarDiferencia();
    }

    private void actualizarDiferencia() {
        String horaInicio = spinnerInicio.getValue();
        String horaFin = spinnerFin.getValue();

        if (horaInicio != null && horaFin != null) {
            double diferencia = GeneradorHoras.calcularDiferencia(horaInicio, horaFin);
            String formatted = GeneradorHoras.formatearHoras(diferencia);
            lblHorasCalculadas.setText("Tiempo: " + formatted);
        }
    }

    @FXML
    void clickRegistrar() {
        if (docenteSeleccionado == null) {
            Alertas.mostrarError("Es necesario seleccionar un docente");
            return;
        }

        if(!Validaciones.campoRequerido(txtUso)){
            return;
        }

        if(txtUso.getText().length() < 5){
            Alertas.mostrarError("El campo de uso debe tener al menos 5 caracteres");
            return;
        }

        int id_docente = docenteSeleccionado.getId();
        String uso = txtUso.getText();
        String horaInicio = spinnerInicio.getValue();
        String horaFin = spinnerFin.getValue();
        double diferenciaHoras = GeneradorHoras.calcularDiferencia(horaInicio, horaFin);
        int totalHoras = (int) Math.round(diferenciaHoras);

        if (diferenciaHoras <= 0) {
            Alertas.mostrarError("La hora fin debe ser mayor que la hora inicio");
            return;
        }

        if(registroPlataformaDAO.registrarUsoConHoras(id_docente, uso, horaInicio, horaFin, totalHoras)){
            Alertas.mostrarExito("Registro de plataforma realizado correctamente");
            limpiarCampos();
        } else {
            Alertas.mostrarError("Error al registrar el uso. Intente nuevamente.");
        }

    }

    private void limpiarCampos() {
        txtDocente.clear();
        txtUso.clear();
        spinnerInicio.getValueFactory().setValue("06:00");
        spinnerFin.getValueFactory().setValue("08:00");
        docenteSeleccionado = null;
        actualizarDiferencia();
    }
}
