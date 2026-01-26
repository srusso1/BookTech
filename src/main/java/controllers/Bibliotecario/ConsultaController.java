package controllers.Bibliotecario;

import database.LibrosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Libro;
import utils.Alertas;
import utils.Validaciones;

import java.util.ArrayList;
import java.util.List;

public class ConsultaController {

    @FXML
    private Button btnConsulta;

    @FXML
    private TextField txtBuscarLibro;

    @FXML
    private Label txtAutor;

    @FXML
    private Label txtCategoria;

    @FXML
    private Label txtEditorial;

    @FXML
    private Label txtUbicacion;

    @FXML
    private VBox contenedorInfoLibro;

    @FXML
    private Label txtUnidades;

    @FXML
    private Button btnRegistrarPrestamo;

    @FXML
    void clickConsultar(ActionEvent event) {

    }

    private static Libro libroSeleccionado;

    private final ContextMenu sugerenciasMenu = new ContextMenu();

    @FXML
    void clickRegistrarPrestamo(ActionEvent event) {

        if (libroSeleccionado == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/Bibliotecario/Prestamo.fxml")
            );

            // 🔹 El root ES un VBox
            VBox root = loader.load();

            // 🔹 Controller del préstamo
            PrestamoController controller = loader.getController();
            controller.setLibro(libroSeleccionado);

            // 🔹 Diálogo
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Registrar préstamo");
            dialog.getDialogPane().setContent(root);

            // 🔹 Botón cerrar (el formulario maneja registrar/cancelar)
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();

        } catch (Exception e) {
            Alertas.mostrarError("ERROR: " + e.getMessage());
        }
    }


    @FXML
    public void initialize() {

        ocultarElementos();

        txtBuscarLibro.textProperty().addListener((obs, oldText, newText) -> {

            if (newText.length() < 3) {
                sugerenciasMenu.hide();
                ocultarElementos();
                return;
            }

            List<Libro> resultados = LibrosDAO.buscarSimilares(newText);

            if (resultados.isEmpty()) {
                sugerenciasMenu.hide();
                Validaciones.agregarPopOver(txtBuscarLibro, "No hay coincidencias");
                ocultarElementos();
                return;
            }

            Validaciones.ocultarPopOver(txtBuscarLibro);

            List<MenuItem> items = getMenuItems(resultados);

            sugerenciasMenu.getItems().setAll(items);

            if (!sugerenciasMenu.isShowing()) {
                sugerenciasMenu.show(txtBuscarLibro, Side.BOTTOM, 0, 0);
            }
        });

        // Ocultar si pierde foco
        txtBuscarLibro.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) {
                sugerenciasMenu.hide();
                Validaciones.ocultarPopOver(txtBuscarLibro);
            }
        });
    }

    private List<MenuItem> getMenuItems(List<Libro> resultados) {
        List<MenuItem> items = new ArrayList<>();

        for (Libro libro : resultados) {
            MenuItem item = new MenuItem(
                    libro.getTitulo() + " — " + libro.getAutor()
            );

            item.setOnAction(e -> {
                txtBuscarLibro.setText(libro.getTitulo());
                sugerenciasMenu.hide();

                // Aquí ya tienes el libro seleccionado
                libroSeleccionado = libro;
                mostrarElementos();
                mostrarInformacionLibro();
            });

            items.add(item);
        }
        return items;
    }

    private void mostrarInformacionLibro(){
        txtAutor.setText(libroSeleccionado.getAutor());
        txtEditorial.setText(libroSeleccionado.getEditorial());
        txtUbicacion.setText(libroSeleccionado.getUbicacion());
        txtCategoria.setText(libroSeleccionado.getCategoria());
        txtUnidades.setText(String.valueOf(libroSeleccionado.getUnidades()));
    }

    private void ocultarElementos(){
        contenedorInfoLibro.setVisible(false);
        contenedorInfoLibro.setManaged(false);

        btnRegistrarPrestamo.setVisible(false);
        btnRegistrarPrestamo.setManaged(false);
    }

    private void mostrarElementos(){
        contenedorInfoLibro.setVisible(true);
        contenedorInfoLibro.setManaged(true);

        btnRegistrarPrestamo.setVisible(true);
        btnRegistrarPrestamo.setManaged(true);
    }




}
