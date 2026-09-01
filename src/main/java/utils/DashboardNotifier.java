package utils;

import javafx.application.Platform;

public class DashboardNotifier {
    private static Runnable actualizarNotificacionesCallback;

    public static void setActualizarNotificacionesCallback(Runnable callback) {
        actualizarNotificacionesCallback = callback;
    }

    public static void notificarCambio() {
        if (actualizarNotificacionesCallback != null) {
            Platform.runLater(() -> actualizarNotificacionesCallback.run());
        }
    }
}
