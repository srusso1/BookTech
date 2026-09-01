package controllers.Rectoria;

import database.LibrosDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

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
        List<String> infoDashboard = librosDAO.infoDashboardRectoria();
        librosRegistrados.setText(infoDashboard.getFirst());
        unidadesRegistradas.setText(infoDashboard.get(1));
        categoriaSolicitada.setText(infoDashboard.getLast());
    }

}
