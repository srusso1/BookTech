package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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
public class DashboardNavigationTest {

    @Start
    public void start(Stage stage) throws Exception {
        utils.SessionManager.getInstance().setUsuarioActual(new model.Bibliotecario("test", "test", "test", "test"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.DASHBOARD_BIBLIOTECARIO));
            loader.setControllerFactory(utils.AppDIContainer.getInstance());
        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    @DisplayName("Verifica navegación entre los módulos del Dashboard Bibliotecario")
    void testNavegacionDashboardBibliotecario(FxRobot robot) {
        FxAssert.verifyThat("#contenedor", NodeMatchers.isVisible());
        BorderPane contenedor = robot.lookup("#contenedor").queryAs(BorderPane.class);
        assertThat(contenedor.getCenter()).isNotNull();

        // Clic en Consulta
        robot.clickOn("#btnConsulta");
        assertThat(contenedor.getCenter()).isNotNull();

        // Clic en Préstamos Activos
        robot.clickOn("#btnPrestamos");
        assertThat(contenedor.getCenter()).isNotNull();

        // Clic en Biblioteca Virtual
        robot.clickOn("#btnBibliotecaVirtual");
        assertThat(contenedor.getCenter()).isNotNull();

        // Volver a Inicio
        robot.clickOn("#btnInicio");
        assertThat(contenedor.getCenter()).isNotNull();
    }
}




