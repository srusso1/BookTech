package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import utils.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
public class LoginViewTest {

    @Start
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.LOGIN));
            loader.setControllerFactory(utils.AppDIContainer.getInstance());
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    @DisplayName("Verifica que los controles de login existan y sean visibles")
    void testElementosLoginVisibles(FxRobot robot) {
        FxAssert.verifyThat("#txtUsuario", NodeMatchers.isVisible());
        FxAssert.verifyThat("#txtPassword", NodeMatchers.isVisible());
        FxAssert.verifyThat("#btnIngresar", NodeMatchers.isVisible());
        FxAssert.verifyThat("#btnIngresar", LabeledMatchers.hasText("Ingresar"));
    }

    @Test
    @DisplayName("Verifica interaccion escribiendo en los campos de usuario y password")
    void testEscribirCredenciales(FxRobot robot) {
        TextField txtUsuario = robot.lookup("#txtUsuario").queryAs(TextField.class);
        javafx.scene.control.PasswordField txtPassword = robot.lookup("#txtPassword").queryAs(javafx.scene.control.PasswordField.class);

        robot.interact(() -> {
            txtUsuario.setText("admin");
            txtPassword.setText("12345");
        });

        assertThat(txtUsuario.getText()).isEqualTo("admin");
        assertThat(txtPassword.getText()).isEqualTo("12345");
    }
}
