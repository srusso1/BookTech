package application;

import javafx.application.Application;

/**
 * Punto de entrada principal para evitar el chequeo de launcher JavaFX en classpath.
 */
public class Bootstrap {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}


