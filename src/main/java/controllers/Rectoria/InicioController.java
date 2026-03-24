package controllers.Rectoria;

import database.LibrosDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;

public class InicioController {

    @FXML
    private Label categoriaSolicitada;

    @FXML
    private Label informesGenerados;

    @FXML
    private Label librosRegistrados;

    @FXML
    private Label unidadesRegistradas;

    LibrosDAO librosDAO = new LibrosDAO();

    @FXML
    void initialize(){
        ArrayList<String> infoDashboard = librosDAO.infoDashboardRectoria();
        librosRegistrados.setText(infoDashboard.getFirst());
        unidadesRegistradas.setText(infoDashboard.get(1));
        categoriaSolicitada.setText(infoDashboard.getLast());
    }

}
