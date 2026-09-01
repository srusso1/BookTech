package controllers.Bibliotecario;

import database.LibrosDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

public class InicioController {

    @FXML
    private Label librosRegistrados;

    @FXML
    private Label prestamosActivos;

    @FXML
    private Label prestamosRealizados;

    @FXML
    private Label unidadesRegistradas;

    @FXML
    void initialize(){
        cargarInfoDashboard();
    }

    LibrosDAO librosDAO = new LibrosDAO();

    private void cargarInfoDashboard(){
        List<Integer> info = librosDAO.infoDashboardBibliotecario();
        librosRegistrados.setText(String.valueOf(info.get(0)));
        unidadesRegistradas.setText(String.valueOf(info.get(1)));
        prestamosActivos.setText(String.valueOf(info.get(2)));
        prestamosRealizados.setText(String.valueOf(info.get(3)));
    }

}
