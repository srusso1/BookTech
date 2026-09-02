package controllers.Bibliotecario;

import database.LibrosDAO;
import database.PrestamosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Libro;
import model.Prestamo;
import utils.Alertas;
import utils.BusquedaSugerencias;
import utils.Paths;
import utils.Validaciones;
import java.util.concurrent.CompletableFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsultaController {

    private static final Logger LOGGER = Logger.getLogger(ConsultaController.class.getName());

    public ConsultaController(PrestamosDAO prestamosDAO, LibrosDAO librosDAO) {
        this.prestamosDAO = prestamosDAO;
        this.librosDAO = librosDAO;
    }


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
    private Button btnRegistrarDevolucion;

    private final PrestamosDAO prestamosDAO;

    private static Libro libroSeleccionado;

    private static List<Prestamo> prestamosActivos = new ArrayList<>();

    private final ContextMenu sugerenciasMenu = new ContextMenu();
    private final LibrosDAO librosDAO;

    @FXML
    void clickRegistrarPrestamo(ActionEvent event) {

        if (libroSeleccionado == null) return;

        if(!libroSeleccionado.isDisponibleParaPrestamo()){
            Alertas.mostrarError("No hay suficientes unidades disponibles para realizar un prestamo");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(Paths.PRESTAMO_BIBLIOTECARIO)
            );
            loader.setControllerFactory(utils.AppDIContainer.getInstance());

            // 🔹 El root ES un VBox
            VBox root = loader.load();

            // 🔹 Controller del préstamo
            PrestamoController controller = loader.getController();
            controller.setLibro(libroSeleccionado);

            // 🔹 Crear un Stage real en lugar de un Dialog para evitar botones duplicados
            Stage modal = new Stage();
            modal.setTitle("Registrar préstamo");
            modal.initModality(javafx.stage.Modality.WINDOW_MODAL);
            modal.initOwner(txtBuscarLibro.getScene().getWindow());
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(java.util.Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());
            modal.setScene(scene);
            modal.setResizable(false);
            
            modal.showAndWait();

        } catch (Exception e) {
            Alertas.mostrarError("ERROR: " + e.getMessage());
        }
        txtBuscarLibro.clear();
    }

    @FXML
    void clickRegistrarDevolucion(ActionEvent event) {
        if (libroSeleccionado == null) return;

        CompletableFuture.supplyAsync(() -> prestamosDAO.buscarPrestamosLibro(libroSeleccionado.getId()))
                .thenAcceptAsync(prestamos -> {
                    prestamosActivos = prestamos;

                    if(prestamosActivos.isEmpty()){
                        Alertas.mostrarError("No hay prestamos activos para este libro");
                        return;
                    }

                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource(Paths.DEVOLUCION_BIBLIOTECARIO)
                        );
                        loader.setControllerFactory(utils.AppDIContainer.getInstance());

                        // 🔹 El root ES un VBox
                        VBox root = loader.load();

                        // 🔹 Controller del préstamo
                        DevolucionController controller = loader.getController();
                        controller.setLibro(libroSeleccionado);
                        controller.setPrestamos(prestamosActivos);

                        // 🔹 Crear un Stage real en lugar de un Dialog para evitar botones duplicados
                        Stage modal = new Stage();
                        modal.setTitle("Registrar devolución");
                        modal.initModality(javafx.stage.Modality.WINDOW_MODAL);
                        modal.initOwner(txtBuscarLibro.getScene().getWindow());
                        
                        javafx.scene.Scene scene = new javafx.scene.Scene(root);
                        scene.getStylesheets().add(java.util.Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());
                        modal.setScene(scene);
                        modal.setResizable(false);
                        
                        modal.showAndWait();

                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error al cargar la vista lateral", e);
                    }

                    txtBuscarLibro.clear();
                }, javafx.application.Platform::runLater);
    }


    @FXML
    public void initialize() {

        ocultarElementos();

        BusquedaSugerencias.configurar(
                txtBuscarLibro,
                sugerenciasMenu,
                librosDAO::buscarSimilares,
                3,
                l -> l.getTitulo() + " - " + l.getAutor(),
                Libro::getTitulo,
                l -> {
                    libroSeleccionado = l;
                    mostrarElementos();
                    mostrarInformacionLibro();
                },
                this::ocultarElementos
        );
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

        btnRegistrarDevolucion.setVisible(false);
        btnRegistrarDevolucion.setManaged(false);
    }

    private void mostrarElementos(){
        contenedorInfoLibro.setVisible(true);
        contenedorInfoLibro.setManaged(true);

        btnRegistrarPrestamo.setVisible(true);
        btnRegistrarPrestamo.setManaged(true);

        btnRegistrarDevolucion.setVisible(true);
        btnRegistrarDevolucion.setManaged(true);
    }

    private void aplicarEstilosDialogo(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm()
        );
    }




}
