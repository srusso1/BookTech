package application;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utils.Paths;
import utils.updates.UpdateService;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Configuración global del tema moderno AtlantaFX PrimerLight
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

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

        scene.getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm()
        );

        utils.ResponsiveManager.aplicarEscena(scene);

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
                    BufferedImage awtImage = cargarIconoAwt("/images/iconApp.png");
                    if (awtImage != null) {
                        taskbar.setIconImage(awtImage);
                    }
                }
            }
        } catch (UnsupportedOperationException | SecurityException ignored) {
        }
    }

    private BufferedImage cargarIconoAwt(String resourcePath) {
        try (InputStream iconStream = getClass().getResourceAsStream(resourcePath)) {
            if (iconStream == null) {
                return null;
            }
            return ImageIO.read(iconStream);
        } catch (Exception e) {
            return null;
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
