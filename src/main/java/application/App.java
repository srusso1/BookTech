package application;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import utils.Paths;
import utils.updates.UpdateService;

import java.awt.Taskbar;
import java.io.InputStream;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        // Iniciamos la búsqueda de actualizaciones una vez que el toolkit de JavaFX está listo.
        new Thread(() -> {
            try {
                // Pequeña espera para asegurar que la ventana principal sea visible antes de la alerta
                Thread.sleep(1000);
                UpdateService.notificarActualizacionSiExiste();
            } catch (Exception e) {
                System.err.println("[Update] Error al buscar actualizaciones: " + e.getMessage());
            }
        }).start();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(Paths.LOGIN)
        );

        Parent root = loader.load();
        Scene scene = new Scene(root);

        // 1️⃣ BootstrapFX (base)
        scene.getStylesheets().addAll(
                BootstrapFX.bootstrapFXStylesheet(),
                getClass().getResource("/styles/style.css").toExternalForm()
        );


        stage.setTitle("BookTech");
        stage.setScene(scene);
        stage.setMaximized(true);

        configurarIconoAplicacion(stage);

        stage.show();
    }

    private void configurarIconoAplicacion(Stage stage) {
        Image icono = cargarIcono("/images/iconApp.png");
        if (icono == null) {
            icono = cargarIcono("/images/iconLibro.png");
        }
        if (icono == null) {
            icono = cargarIcono("/images/escudo.png");
        }

        if (icono == null) {
            return;
        }

        stage.getIcons().add(icono);

        // En Windows ayuda a que la barra de tareas use el icono de la app y no el de Java.
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    taskbar.setIconImage(SwingFXUtils.fromFXImage(icono, null));
                }
            }
        } catch (UnsupportedOperationException | SecurityException ignored) {
        }
    }

    private Image cargarIcono(String resourcePath) {
        try (InputStream iconStream = getClass().getResourceAsStream(resourcePath)) {
            if (iconStream == null) {
                return null;
            }
            return new Image(iconStream);
        } catch (Exception e) {
            return null;
        }
    }


}