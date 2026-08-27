package utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.AlertaPrestamo;

import java.util.List;
import java.util.Objects;

/**
 * Modal interactivo estilizado para visualización escalable de alertas y notificaciones.
 */
public final class ModalAlertas {

    private ModalAlertas() {
    }

    public static void mostrarModal(List<AlertaPrestamo> alertas) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Centro de Notificaciones");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setPrefWidth(620);
        dialogPane.setMaxWidth(620);

        try {
            dialogPane.getStylesheets().add(
                    Objects.requireNonNull(ModalAlertas.class.getResource("/styles/style.css")).toExternalForm()
            );
        } catch (Exception ignored) {
        }

        VBox root = new VBox(14);
        root.setPadding(new Insets(10));

        // Cabecera
        Label lblTitulo = new Label("Alertas y Vencimientos de Préstamos");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label lblSubtitulo = new Label("Total de alertas activas: " + alertas.size());
        lblSubtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        VBox headerBox = new VBox(4, lblTitulo, lblSubtitulo);

        // Contenedor de tarjetas
        VBox listaCards = new VBox(10);
        listaCards.setPadding(new Insets(5));

        // Filtros (Segmented Buttons)
        HBox filtrosBox = new HBox(8);
        filtrosBox.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup group = new ToggleGroup();
        ToggleButton btnTodos = new ToggleButton("Todos (" + alertas.size() + ")");
        btnTodos.setToggleGroup(group);
        btnTodos.setSelected(true);

        long countVencidos = alertas.stream().filter(a -> a.getTipo() == AlertaPrestamo.TipoAlerta.VENCIDO).count();
        ToggleButton btnVencidos = new ToggleButton("Vencidos (" + countVencidos + ")");
        btnVencidos.setToggleGroup(group);

        long countHoy = alertas.stream().filter(a -> a.getTipo() == AlertaPrestamo.TipoAlerta.POR_VENCER_HOY).count();
        ToggleButton btnHoy = new ToggleButton("Vencen hoy (" + countHoy + ")");
        btnHoy.setToggleGroup(group);

        long countProx = alertas.stream().filter(a -> a.getTipo() == AlertaPrestamo.TipoAlerta.PROXIMO_A_VENCER).count();
        ToggleButton btnProx = new ToggleButton("Próximos (" + countProx + ")");
        btnProx.setToggleGroup(group);

        filtrosBox.getChildren().addAll(btnTodos, btnVencidos, btnHoy, btnProx);

        // Renderizado reactivo
        Runnable renderizar = () -> {
            listaCards.getChildren().clear();
            List<AlertaPrestamo> filtradas = alertas;
            if (btnVencidos.isSelected()) {
                filtradas = alertas.stream().filter(a -> a.getTipo() == AlertaPrestamo.TipoAlerta.VENCIDO).toList();
            } else if (btnHoy.isSelected()) {
                filtradas = alertas.stream().filter(a -> a.getTipo() == AlertaPrestamo.TipoAlerta.POR_VENCER_HOY).toList();
            } else if (btnProx.isSelected()) {
                filtradas = alertas.stream().filter(a -> a.getTipo() == AlertaPrestamo.TipoAlerta.PROXIMO_A_VENCER).toList();
            }

            if (filtradas.isEmpty()) {
                Label lblVacio = new Label("No hay alertas en esta categoría.");
                lblVacio.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 20px;");
                listaCards.getChildren().add(lblVacio);
                return;
            }

            for (AlertaPrestamo alerta : filtradas) {
                listaCards.getChildren().add(crearCardAlerta(alerta));
            }
        };

        btnTodos.setOnAction(e -> renderizar.run());
        btnVencidos.setOnAction(e -> renderizar.run());
        btnHoy.setOnAction(e -> renderizar.run());
        btnProx.setOnAction(e -> renderizar.run());

        renderizar.run();

        ScrollPane scrollPane = new ScrollPane(listaCards);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(380);
        scrollPane.setMaxHeight(380);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        root.getChildren().addAll(headerBox, new Separator(), filtrosBox, scrollPane);
        dialogPane.setContent(root);

        dialog.showAndWait();
    }

    private static Node crearCardAlerta(AlertaPrestamo alerta) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10, 14, 10, 14));

        String borderColor;
        String badgeText;
        String badgeBg;

        switch (alerta.getTipo()) {
            case VENCIDO -> {
                borderColor = "#ef4444";
                badgeText = "VENCIDO (" + Math.abs(alerta.getDiasDiferencia()) + " DÍAS)";
                badgeBg = "#fee2e2; -fx-text-fill: #b91c1c;";
            }
            case POR_VENCER_HOY -> {
                borderColor = "#f59e0b";
                badgeText = "VENCE HOY";
                badgeBg = "#fef3c7; -fx-text-fill: #b45309;";
            }
            case PROXIMO_A_VENCER -> {
                borderColor = "#3b82f6";
                badgeText = "VENCE EN " + alerta.getDiasDiferencia() + " DÍAS";
                badgeBg = "#dbeafe; -fx-text-fill: #1d4ed8;";
            }
            default -> {
                borderColor = "#94a3b8";
                badgeText = "PENDIENTE";
                badgeBg = "#f1f5f9; -fx-text-fill: #475569;";
            }
        }

        card.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-border-color: #e2e8f0 #e2e8f0 #e2e8f0 " + borderColor + "; -fx-border-width: 1px 1px 1px 4px;");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label(alerta.getLibroTitulo());
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        HBox.setHgrow(lblTitulo, Priority.ALWAYS);

        Label lblBadge = new Label(badgeText);
        lblBadge.setStyle("-fx-background-color: " + badgeBg + " -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 10px; -fx-padding: 2px 8px;");

        topRow.getChildren().addAll(lblTitulo, lblBadge);

        HBox bottomRow = new HBox(15);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        Label lblEstudiante = new Label("Estudiante: " + alerta.getEstudiante() + " (Grado " + alerta.getGrado() + "°)");
        lblEstudiante.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        Label lblFecha = new Label("Fecha límite: " + alerta.getFechaLimite());
        lblFecha.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 500;");

        bottomRow.getChildren().addAll(lblEstudiante, lblFecha);

        card.getChildren().addAll(topRow, bottomRow);
        return card;
    }
}
