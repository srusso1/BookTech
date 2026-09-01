package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Objects;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ManagerView {

    private static final Logger LOGGER = Logger.getLogger(ManagerView.class.getName());
    public static void cargarVista(Pane contenedor, String fxml) {
        try {
            Parent vista = FXMLLoader.load(
                    Objects.requireNonNull(
                            ManagerView.class.getResource(fxml)));

            contenedor.getChildren().clear();
            contenedor.getChildren().add(vista);

            // Solo si el contenedor es AnchorPane
            if (contenedor instanceof AnchorPane) {
                AnchorPane.setTopAnchor(vista, 0.0);
                AnchorPane.setBottomAnchor(vista, 0.0);
                AnchorPane.setLeftAnchor(vista, 0.0);
                AnchorPane.setRightAnchor(vista, 0.0);
            }

        } catch (IOException e) {
            Alertas.mostrarError("Error al cargar la vista: " + e.getMessage());
        }
    }

    public static void cargarCentro(BorderPane borderPane, String fxml) {
        try {
            Parent vista = FXMLLoader.load(
                    Objects.requireNonNull(
                            ManagerView.class.getResource(fxml)));

            borderPane.setCenter(vista);

        } catch (IOException e) {
            Alertas.mostrarError("Error al cargar vista central: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error al cargar vista: " + fxml, e);
        }
    }

}
