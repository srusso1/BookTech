package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import utils.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
public class ViewsLoadingTest {

    @Start
    public void start(Stage stage) {
        // Init JavaFX context
    }

    private void assertLoadsSuccessfully(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(utils.AppDIContainer.getInstance());
            Parent root = loader.load();
            assertThat(root).as("La vista " + fxmlPath + " debe cargar correctamente").isNotNull();
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Fallo al cargar la vista " + fxmlPath + ": " + e.getMessage(), e);
        }
    }

    @Test
    @DisplayName("Verifica que todas las vistas FXML del sistema carguen sin errores de importaciÃ³n o tipo")
    void testAllViewsLoadWithoutError() {
        String[] allViews = {
            Paths.LOGIN,
            Paths.DASHBOARD_BIBLIOTECARIO,
            Paths.DASHBOARD_RECTORIA,
            Paths.INICIO_BIBLIOTECARIO,
            Paths.CONSULTA_BIBLIOTECARIO,
            Paths.PRESTAMO_BIBLIOTECARIO,
            Paths.DEVOLUCION_BIBLIOTECARIO,
            Paths.PRESTAMOS_ACTIVOS,
            Paths.BIBLIOTECA_VIRTUAL,
            Paths.INICIO_RECTORIA,
            Paths.INVENTARIO_RECTORIA,
            Paths.EDITAR_LIBRO_RECTORIA,
            Paths.REGISTRAR_LIBRO_RECTORIA,
            Paths.ESTADISTICAS_RECTORIA,
            Paths.INFORMES_RECTORIA,
            Paths.CONFIGURACION_RECTORIA,
            Paths.AYUDA_RECTORIA
        };

        for (String fxml : allViews) {
            assertLoadsSuccessfully(fxml);
        }
    }
}
