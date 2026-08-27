package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

@ExtendWith(ApplicationExtension.class)
public class DashboardViewTest {

    @Start
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.DASHBOARD_BIBLIOTECARIO));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    @DisplayName("Verifica que los botones del menú lateral y el contenedor central del Dashboard carguen correctamente")
    void testDashboardElementos(FxRobot robot) {
        FxAssert.verifyThat("#btnInicio", NodeMatchers.isVisible());
        FxAssert.verifyThat("#btnConsulta", NodeMatchers.isVisible());
        FxAssert.verifyThat("#btnPrestamos", NodeMatchers.isVisible());
        FxAssert.verifyThat("#btnBibliotecaVirtual", NodeMatchers.isVisible());
        FxAssert.verifyThat("#contenedor", NodeMatchers.isVisible());
    }
}
