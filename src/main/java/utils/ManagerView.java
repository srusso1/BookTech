package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManagerView {

    private static final Logger LOGGER = Logger.getLogger(ManagerView.class.getName());

    private static class ViewCacheEntry {
        Parent vista;
        Object controller;
        ViewCacheEntry(Parent vista, Object controller) {
            this.vista = vista;
            this.controller = controller;
        }
    }

    private static final Map<String, ViewCacheEntry> vistaCache = new HashMap<>();

    private static ViewCacheEntry getOrLoadView(String fxml) throws IOException {
        if (vistaCache.containsKey(fxml)) {
            ViewCacheEntry entry = vistaCache.get(fxml);
            if (entry.controller instanceof Refrescable) {
                ((Refrescable) entry.controller).refresh();
            }
            return entry;
        }

        FXMLLoader loader = new FXMLLoader(ManagerView.class.getResource(fxml));
        loader.setControllerFactory(AppDIContainer.getInstance());
        Parent vista = loader.load();
        ViewCacheEntry entry = new ViewCacheEntry(vista, loader.getController());
        vistaCache.put(fxml, entry);
        return entry;
    }

    public static void cargarVista(Pane contenedor, String fxml) {
        try {
            ViewCacheEntry entry = getOrLoadView(fxml);
            Parent vista = entry.vista;

            contenedor.getChildren().clear();
            contenedor.getChildren().add(vista);

            if (contenedor instanceof AnchorPane) {
                AnchorPane.setTopAnchor(vista, 0.0);
                AnchorPane.setBottomAnchor(vista, 0.0);
                AnchorPane.setLeftAnchor(vista, 0.0);
                AnchorPane.setRightAnchor(vista, 0.0);
            }

        } catch (IOException e) {
            Alertas.mostrarError("Error al cargar la vista: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error al cargar vista: " + fxml, e);
        }
    }

    public static void cargarCentro(BorderPane borderPane, String fxml) {
        try {
            ViewCacheEntry entry = getOrLoadView(fxml);
            Parent vista = entry.vista;

            borderPane.setCenter(vista);

        } catch (IOException e) {
            Alertas.mostrarError("Error al cargar vista central: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error al cargar vista central: " + fxml, e);
        }
    }

    public static void clearCache() {
        vistaCache.clear();
    }
}