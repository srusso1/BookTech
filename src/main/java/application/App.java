package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import utils.Paths;

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
        stage.show();
    }


}
