package controllers.Dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import utils.Alertas;
import utils.ManagerView;
import utils.Paths;

public class DashboardBibliotecarioController {

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
    void clickConsulta(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.CONSULTA_BIBLIOTECARIO);
    }

    @FXML
    void clickInicio(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.INICIO_BIBLIOTECARIO);
    }

    @FXML
    void clickPrestamos(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.PRESTAMOS_ACTIVOS);
    }

    @FXML
    void initialize() {
        ManagerView.cargarCentro(contenedor, Paths.INICIO_BIBLIOTECARIO);
    }

    @FXML
    void clickBibliotecaVirtual(ActionEvent event) {
        ManagerView.cargarCentro(contenedor, Paths.BIBLIOTECA_VIRTUAL);
    }

    @FXML
    void clickSalir(ActionEvent event) {
        if(Alertas.mostrarConfirmacion("¿Estás seguro que deseas cerrar sesión?")){
            ManagerView.cargarVista(contenedorPrincipal, Paths.LOGIN);
        }
    }


}
