package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import utils.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
public class InventarioViewTest {

    @Start
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.INVENTARIO_RECTORIA));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    @DisplayName("Verifica que los controles principales de Inventario sean visibles y responsivos")
    void testInventarioControlesVisibles(FxRobot robot) {
        FxAssert.verifyThat("#txtBuscarLibro", NodeMatchers.isVisible());
        FxAssert.verifyThat("#tabla", NodeMatchers.isVisible());

        TableView<?> tabla = robot.lookup("#tabla").queryAs(TableView.class);
        assertThat(tabla).isNotNull();
        assertThat(tabla.getColumns()).hasSize(6);
    }
}
