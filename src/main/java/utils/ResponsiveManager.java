package utils;

import javafx.scene.Scene;

/**
 * Gestor centralizado de calibración responsiva y ergonomía de interfaz.
 */
public final class ResponsiveManager {

    private ResponsiveManager() {
    }

    /**
     * Aplica la calibración base a la escena principal.
     */
    public static void aplicarEscena(Scene scene) {
        // La escala visual y ergonomía se gestiona a través de la hoja de estilos global
        // con tamaños generosos (sidebar 280px, botones 48-50px, fuentes 16px).
    }
}
