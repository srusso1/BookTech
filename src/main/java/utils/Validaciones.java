package utils;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import org.controlsfx.control.PopOver;

import java.util.HashMap;
import java.util.Map;

public class Validaciones {

    private static final Map<TextInputControl, PopOver> popOvers = new HashMap<>();

    public static boolean campoRequerido(TextInputControl campo) {
        if (campo.getText().isEmpty()) {
            campoInvalido(campo);
            agregarPopOver(campo, "Este campo es obligatorio");
            return false;
        }

        if (campo.getText().length() > 32) {
            campoInvalido(campo);
            agregarPopOver(campo, "No se admiten más de 32 caracteres");
            return false;
        }

        campoValido(campo);

        return true;
    }

    public static boolean validarUsuario(TextInputControl campo) {
        if (campo.getText().length() < 4 || campo.getText().length() > 32) {
            campoInvalido(campo);
            agregarPopOver(campo, "El usuario debe contener entre 4 y 32 caracteres");
            return false;
        }

        campoValido(campo);
        return true;
    }

    public static boolean validarPassword(TextInputControl campo) {
        if (campo.getText().length() < 4 || campo.getText().length() > 32) {
            campoInvalido(campo);
            agregarPopOver(campo, "La contraseña debe contener entre 4 y 32 caracteres");
            return false;
        }
        campoValido(campo);
        return true;
    }

    // Método privado para agregar el PopOver

    public static void agregarPopOver(TextInputControl campo, String texto) {

        // Si ya existe, solo actualiza el texto
        if (popOvers.containsKey(campo)) {
            Label label = (Label) popOvers.get(campo).getContentNode();
            label.setText(texto);
            popOvers.get(campo).show(campo);
            return;
        }

        Label label = new Label(texto);
        label.setWrapText(true);

        PopOver popOver = new PopOver(label);
        popOver.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
        popOver.setAutoHide(false);

        popOvers.put(campo, popOver);
        popOver.show(campo);
    }

    public static void ocultarPopOver(TextInputControl campo) {
        PopOver popOver = popOvers.get(campo);

        if (popOver != null && popOver.isShowing()) {
            popOver.hide();
        }
    }

    // Método privado para marcar valido o invalido

    private static void campoInvalido(TextInputControl campo) {
        campo.getStyleClass().removeAll("is-valid", "is-invalid");
        campo.getStyleClass().add("is-invalid");
    }

    private static void campoValido(TextInputControl campo) {
        campo.getStyleClass().removeAll("is-valid", "is-invalid");
        campo.getStyleClass().add("is-valid");
    }

    public static boolean validarCampoNumerico(TextInputControl campo) {
        try {
            Integer.parseInt(campo.getText());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
