package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import utils.Paths;

import java.io.InputStream;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

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

        try (InputStream iconStream = getClass().getResourceAsStream("/images/iconApp.ico")) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        }

        stage.show();
    }


}
