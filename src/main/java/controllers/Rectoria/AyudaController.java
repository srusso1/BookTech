package controllers.Rectoria;

import javafx.fxml.FXML;
import utils.updates.UpdateService;

public class AyudaController {

    @FXML
    void clickBuscarActualizaciones() {
        UpdateService.buscarActualizacionManual();
    }
}

