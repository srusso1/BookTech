package controllers.Rectoria;

import database.LibrosDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import utils.Refrescable;

public class InicioController implements Refrescable {

    public InicioController(LibrosDAO librosDAO) {
        this.librosDAO = librosDAO;
    }

    @FXML
    private Label categoriaSolicitada;

    @FXML
    private Label informesGenerados;

    @FXML
    private Label librosRegistrados;

    @FXML
    private Label unidadesRegistradas;

    private final LibrosDAO librosDAO;

    @FXML
    void initialize(){
        cargarInfoDashboard();
    }

    private void cargarInfoDashboard() {
        java.util.concurrent.CompletableFuture.supplyAsync(librosDAO::infoDashboardRectoria)
                .thenAcceptAsync(infoDashboard -> {
                    librosRegistrados.setText(infoDashboard.getFirst());
                    unidadesRegistradas.setText(infoDashboard.get(1));
                    categoriaSolicitada.setText(infoDashboard.getLast());
                }, javafx.application.Platform::runLater);
    }

    @Override
    public void refresh() {
        cargarInfoDashboard();
    }
}
