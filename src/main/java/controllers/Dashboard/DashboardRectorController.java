package controllers.Dashboard;

import database.PrestamosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import model.AlertaPrestamo;
import utils.Alertas;
import utils.ManagerView;
import utils.Paths;

import java.util.List;

public class DashboardRectorController {

    public DashboardRectorController(PrestamosDAO prestamosDAO) {
        this.prestamosDAO = prestamosDAO;
    }

    @FXML
    private Button btnConfig;

    @FXML
    private Button btnAyuda;

    @FXML
    private Button btnConsulta;

    @FXML
    private Button btnInformes;

    @FXML
    private Button btnInicio;

    @FXML
    private Button btnPrestamos;

    @FXML
    private BorderPane contenedor;

    @FXML
    private AnchorPane contenedorPrincipal;

    @FXML
    private Button btnNotificaciones;


    @FXML
    private Label lblBadgeAlertas;

    private final PrestamosDAO prestamosDAO;

    @FXML
    void clickConfig(ActionEvent event) {
        setActiveButton(btnConfig);
        ManagerView.cargarCentro(contenedor, Paths.CONFIGURACION_RECTORIA);
    }

    @FXML
    void clickAyuda(ActionEvent event) {
        setActiveButton(btnAyuda);
        ManagerView.cargarCentro(contenedor, Paths.AYUDA_RECTORIA);
    }

    @FXML
    void clickEstadisticas(ActionEvent event) {
        setActiveButton(btnPrestamos);
        ManagerView.cargarCentro(contenedor, Paths.ESTADISTICAS_RECTORIA);
    }

    @FXML
    void clickInformes(ActionEvent event) {
        setActiveButton(btnInformes);
        ManagerView.cargarCentro(contenedor, Paths.INFORMES_RECTORIA);
    }

    @FXML
    void clickInicio(ActionEvent event) {
        setActiveButton(btnInicio);
        ManagerView.cargarCentro(contenedor, Paths.INICIO_RECTORIA);
        actualizarAlertas();
    }

    @FXML
    void clickInventario(ActionEvent event) {
        setActiveButton(btnConsulta);
        ManagerView.cargarCentro(contenedor, Paths.INVENTARIO_RECTORIA);
    }

    @FXML
    void initialize() {
        if (!utils.SessionManager.getInstance().isUserLoggedIn() || utils.SessionManager.getInstance().getUsuarioActual().getRol() != model.enums.RolUsuario.RECTOR.getId()) {
            ManagerView.cargarVista(contenedorPrincipal, Paths.LOGIN);
            return;
        }
        ManagerView.cargarCentro(contenedor, Paths.INICIO_RECTORIA);
        setActiveButton(btnInicio);
        
        utils.DashboardNotifier.setActualizarNotificacionesCallback(this::actualizarAlertas);
        
        actualizarAlertas();
    }

    public void actualizarAlertas() {
        if (lblBadgeAlertas == null || btnNotificaciones == null) {
            return;
        }
        List<AlertaPrestamo> alertas = prestamosDAO.obtenerAlertasVencimiento();
        int total = alertas.size();
        lblBadgeAlertas.setText(String.valueOf(total));

        btnNotificaciones.getStyleClass().remove("has-alerts");
        lblBadgeAlertas.getStyleClass().remove("active");

        if (total > 0) {
            btnNotificaciones.getStyleClass().add("has-alerts");
            lblBadgeAlertas.getStyleClass().add("active");
        }
    }

    @FXML
    void clickNotificaciones(ActionEvent event) {
        List<AlertaPrestamo> alertas = prestamosDAO.obtenerAlertasVencimiento();
        if (alertas.isEmpty()) {
            Alertas.mostrarInfo("No hay préstamos vencidos ni alertas pendientes en este momento.");
            return;
        }

        utils.ModalAlertas.mostrarModal(alertas);
    }

    @FXML
    void clickSalir(ActionEvent event) {
        if (Alertas.mostrarConfirmacion("¿Estás seguro que deseas cerrar sesión?")) {
            utils.SessionManager.getInstance().logout();
            ManagerView.clearCache();
            ManagerView.cargarVista(contenedorPrincipal, Paths.LOGIN);
        }
    }

    private void setActiveButton(Button activeButton) {
        Button[] buttons = {btnInicio, btnConsulta, btnPrestamos, btnInformes, btnConfig, btnAyuda};
        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("active");
            }
        }
        if (activeButton != null) {
            if (!activeButton.getStyleClass().contains("active")) {
                activeButton.getStyleClass().add("active");
            }
        }
    }
}
