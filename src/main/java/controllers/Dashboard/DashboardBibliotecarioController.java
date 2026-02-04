package controllers.Dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import utils.ManagerView;
import utils.Paths;

import java.io.IOException;

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


}
