package controllers.Bibliotecario;

import database.LibrosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Libro;
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
    void clickConsultar(ActionEvent event) {

    }

    private static Libro libroSeleccionado;

    private final ContextMenu sugerenciasMenu = new ContextMenu();

    @FXML
    public void initialize() {

        ocultarElementos();

        txtBuscarLibro.textProperty().addListener((obs, oldText, newText) -> {

            if (newText.length() < 3) {
                sugerenciasMenu.hide();
                return;
            }

            List<Libro> resultados = LibrosDAO.buscarSimilares(newText);

            if (resultados.isEmpty()) {
                sugerenciasMenu.hide();
                Validaciones.agregarPopOver(txtBuscarLibro, "No hay coincidencias");
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
    }

    private void mostrarElementos(){
        contenedorInfoLibro.setVisible(true);
        contenedorInfoLibro.setManaged(true);
    }




}
