package controllers.Bibliotecario;

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

    private final LibrosDAO librosDAO;

    private void cargarInfoDashboard(){
        java.util.concurrent.CompletableFuture.supplyAsync(librosDAO::infoDashboardBibliotecario)
                .thenAcceptAsync(info -> {
                    librosRegistrados.setText(String.valueOf(info.get(0)));
                    unidadesRegistradas.setText(String.valueOf(info.get(1)));
                    prestamosActivos.setText(String.valueOf(info.get(2)));
                    prestamosRealizados.setText(String.valueOf(info.get(3)));
                }, javafx.application.Platform::runLater);
    }

    @Override
    public void refresh() {
        cargarInfoDashboard();
    }
}
