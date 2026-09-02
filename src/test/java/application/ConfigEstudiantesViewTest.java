package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
import database.EstudiantesDAO;
import model.Estudiante;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
public class ConfigEstudiantesViewTest {
    
    private EstudiantesDAO mockDao;
    private boolean insertCalled = false;

    @Start
    public void start(Stage stage) throws Exception {
        mockDao = new EstudiantesDAO() {
            @Override
            public Estudiante obtenerEstudiantePorIdentificacion(long identificacion) {
                if (identificacion == 999L) {
                    return new Estudiante(1, 999L, 10, "A", "B", "C", "D", "M");
                }
                return null;
            }

            @Override
            public boolean existeIdentificacionEnOtroRegistro(long identificacion, int idExcluir) {
                return identificacion == 999L;
            }

            @Override
            public List<Estudiante> obtenerPaginados(int limit, int offset, String busqueda) {
                return Collections.emptyList();
            }

            @Override
            public int contarTotal(String busqueda) {
                return 0;
            }

            @Override
            public boolean insertarEstudiante(Estudiante estudiante) {
                insertCalled = true;
                return true;
            }
        };

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Rectoria/ConfigEstudiantes.fxml"));
        loader.setControllerFactory(type -> new controllers.Rectoria.ConfigEstudiantesController(mockDao));
        
        Parent root = loader.load();
        // 1280x720 window for responsive test
        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    @DisplayName("Guarda con identificacion duplicada muestra error y no persiste")
    void testGuardarIdentificacionDuplicada(FxRobot robot) {
        // Clic en nuevo estudiante
        robot.clickOn("#btnNuevoEstudiante");
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        
        // Set values directly using interact to ensure they are set reliably
        robot.interact(() -> {
            robot.lookup("#txtIdentificacion").queryAs(TextField.class).setText("999");
            robot.lookup("#txtGrado").queryAs(TextField.class).setText("10");
            robot.lookup("#txtApellido1").queryAs(TextField.class).setText("Perez");
            robot.lookup("#txtNombre1").queryAs(TextField.class).setText("Juan");
            robot.lookup("#txtGenero").queryAs(TextField.class).setText("M");
            
            // Fire the button action directly to avoid mouse coordinate/scroll issues
            robot.lookup("#btnGuardarCambiosEstudiante").queryAs(javafx.scene.control.Button.class).fire();
        });
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        
        // Verificar que apareció el label de error
        FxAssert.verifyThat("#lblErrorIdentificacion", NodeMatchers.isVisible());
        Label lblError = robot.lookup("#lblErrorIdentificacion").queryAs(Label.class);
        assertThat(lblError.getText()).contains("Ya existe un estudiante con esta identificación");
        
        // Verificar que NO se llamó a insertar
        assertThat(insertCalled).isFalse();
    }
    
    @Test
    @DisplayName("btnUltimaPagina es visible en 1280x720")
    void testBotonesPaginacionVisibles(FxRobot robot) {
        FxAssert.verifyThat("#btnUltimaPagina", NodeMatchers.isVisible());
        
        javafx.scene.control.Button btnUltima = robot.lookup("#btnUltimaPagina").queryAs(javafx.scene.control.Button.class);
        
        // Verify bounds are within scene
        javafx.geometry.Bounds boundsInScene = btnUltima.localToScene(btnUltima.getBoundsInLocal());
        assertThat(boundsInScene.getMaxX()).isLessThanOrEqualTo(1280);
    }
}
