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

public class DashboardBibliotecarioController {

    public DashboardBibliotecarioController(PrestamosDAO prestamosDAO) {
        this.prestamosDAO = prestamosDAO;
    }

    @FXML
    private Button btnConsulta;

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
    void clickConsulta(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.CONSULTA_BIBLIOTECARIO);
    }

    @FXML
    void clickInicio(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.INICIO_BIBLIOTECARIO);
        actualizarAlertas();
    }

    @FXML
    void clickPrestamos(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.PRESTAMOS_ACTIVOS);
    }

    @FXML
    void initialize() {
        if (!utils.SessionManager.getInstance().isUserLoggedIn() || utils.SessionManager.getInstance().getUsuarioActual().getRol() != model.enums.RolUsuario.BIBLIOTECARIO.getId()) {
            ManagerView.cargarVista(contenedorPrincipal, Paths.LOGIN);
            return;
        }
        ManagerView.cargarCentro(contenedor, Paths.INICIO_BIBLIOTECARIO);
        
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
    void clickBibliotecaVirtual(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.BIBLIOTECA_VIRTUAL);
    }

    @FXML
    void clickSalir(ActionEvent event) {
        if (Alertas.mostrarConfirmacion("¿Estás seguro que deseas cerrar sesión?")) {
            utils.SessionManager.getInstance().logout();
            ManagerView.cargarVista(contenedorPrincipal, Paths.LOGIN);
        }
    }
}
