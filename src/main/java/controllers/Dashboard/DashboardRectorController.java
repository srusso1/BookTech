package controllers.Dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import utils.Alertas;
import utils.ManagerView;
import utils.Paths;

public class DashboardRectorController {

    @FXML
    private Button btnConfig;

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
    void clickConfig(ActionEvent event) {

    }

    @FXML
    void clickEstadisticas(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.ESTADISTICAS_RECTORIA);
    }

    @FXML
    void clickInformes(ActionEvent event) {

    }

    @FXML
    void clickInicio(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.INICIO_RECTORIA);
    }

    @FXML
    void clickInventario(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.INVENTARIO_RECTORIA);
    }

    @FXML
    void clickSalir(ActionEvent event) {
        if(Alertas.mostrarConfirmacion("¿Estás seguro que deseas cerrar sesión?")){
            ManagerView.cargarVista(contenedorPrincipal, Paths.LOGIN);
        }
    }

    @FXML
    void initialize() {
        ManagerView.cargarCentro(contenedor, Paths.INICIO_RECTORIA);
    }

}
