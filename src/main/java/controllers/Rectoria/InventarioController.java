package controllers.Rectoria;


import database.LibrosDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Libro;
import utils.Alertas;
import utils.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InventarioController {

    public InventarioController(LibrosDAO librosDAO) {
        this.librosDAO = librosDAO;
    }

    @FXML
    private TableView<Libro> tabla;

    @FXML
    private TableColumn<Libro, String> tbAutor;

    @FXML
    private TableColumn<Libro, String> tbCategoria;

    @FXML
    private TableColumn<Libro, String> tbEditorial;

    @FXML
    private TableColumn<Libro, String> tbTitulo;

    @FXML
    private TableColumn<Libro, String> tbUbicacion;

    @FXML
    private TableColumn<Libro, String> tbUnidades;

    @FXML
    private TextField txtBuscarLibro;


    List<Libro> inventarioLibros = new ArrayList<Libro>();
    private final LibrosDAO librosDAO;
    Libro libroSeleccionado;

    @FXML
    void clickEditar(ActionEvent event) {
        libroSeleccionado = tabla.getSelectionModel().getSelectedItem();
        if(libroSeleccionado == null){
            Alertas.mostrarError("Seleccione un libro para editar");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(Paths.EDITAR_LIBRO_RECTORIA)
            );

            // Ã°Å¸â€Â¹ El root ES un VBox
            VBox root = loader.load();

            // Ã°Å¸â€Â¹ Controller del prÃƒÂ©stamo
            EditarLibroController controller = loader.getController();
            controller.setLibro(libroSeleccionado);

            // Ã°Å¸â€Â¹ DiÃƒÂ¡logo
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Editar libro");
            dialog.getDialogPane().setContent(root);
            aplicarEstilosDialogo(dialog);

            // Ã°Å¸â€Â¹ BotÃƒÂ³n cerrar (el formulario maneja registrar/cancelar)
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();

        } catch (Exception e) {
            Alertas.mostrarError("ERROR: " + e.getMessage());
        }

        cargarLibros();
    }

    @FXML
    void clickNuevoLibro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(Paths.REGISTRAR_LIBRO_RECTORIA)
            );

            // Ã°Å¸â€Â¹ El root ES un VBox
            VBox root = loader.load();

            // Ã°Å¸â€Â¹ DiÃƒÂ¡logo
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Libro nuevo");
            dialog.getDialogPane().setContent(root);
            aplicarEstilosDialogo(dialog);

            // Ã°Å¸â€Â¹ BotÃƒÂ³n cerrar (el formulario maneja registrar/cancelar)
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();

        } catch (Exception e) {
            Alertas.mostrarError("ERROR: " + e.getMessage());
        }
        cargarLibros();
    }

    @FXML
    void clickEliminarLibro(ActionEvent event) {
        libroSeleccionado = tabla.getSelectionModel().getSelectedItem();
        if(libroSeleccionado == null){
            Alertas.mostrarError("Seleccione un libro para eliminar");
            return;
        }
        boolean ok = Alertas.mostrarConfirmacion("Ã‚Â¿EstÃƒÂ¡s seguro de eliminar el libro: '" + libroSeleccionado.getTitulo() +"'? EstÃƒÂ¡ acciÃƒÂ³n no se puede deshacer");
        if(ok){
            if(librosDAO.eliminarLibro(libroSeleccionado.getId())){
                Alertas.mostrarExito("Se eliminado correctamente el libro: '" + libroSeleccionado.getTitulo() + "' del inventario.");
                cargarLibros();
            }
        }else{
            Alertas.mostrarInfo("AcciÃƒÂ³n cancelada por el usuario");
        }
        cargarLibros();
    }

    @FXML
    void initialize() {
        inventarioLibros = librosDAO.inventarioLibros();
        configurarTabla();
        tabla.setPlaceholder(new Label("No hay libros que coincidan"));
        configurarBusquedaTitulo();
    }

    private void configurarBusquedaTitulo(){
        txtBuscarLibro.textProperty().addListener((obs, oldText, newText) -> {

            // Si estÃƒÂ¡ vacÃƒÂ­o Ã¢â€ â€™ mostrar todos
            if (newText == null || newText.isBlank()) {
                tabla.getItems().setAll(inventarioLibros);
                return;
            }

            String texto = newText.toLowerCase();

            List<Libro> filtrados = new ArrayList<>();

            for (Libro libro : inventarioLibros) {

                if (
                        libro.getTitulo().toLowerCase().contains(texto) ||
                                libro.getAutor().toLowerCase().contains(texto) ||
                                libro.getCategoria().toLowerCase().contains(texto) ||
                                libro.getEditorial().toLowerCase().contains(texto) ||
                                libro.getUbicacion().toLowerCase().contains(texto)
                ) {
                    filtrados.add(libro);
                }
            }

            tabla.getItems().setAll(filtrados);
        });
    }

    private void configurarTabla(){
        tbTitulo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTitulo()));
        tbUbicacion.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUbicacion()));
        tbCategoria.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategoria()));
        tbEditorial.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEditorial()));
        tbAutor.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAutor()));
        tbUnidades.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getUnidades())));
        tabla.getColumns().forEach(col -> col.setReorderable(false));

        cargarLibros();
    }

    private void cargarLibros(){
        inventarioLibros = librosDAO.inventarioLibros();
        tabla.getItems().setAll(inventarioLibros);
    }

    private void aplicarEstilosDialogo(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm()
        );
    }

}