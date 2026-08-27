package controllers.Bibliotecario;

import database.LibrosDAO;
import database.PrestamosDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Libro;
import model.Prestamo;
import utils.Alertas;
import utils.Paths;
import utils.Validaciones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    private Button btnRegistrarDevolucion;

    PrestamosDAO prestamosDAO = new PrestamosDAO();

    private static Libro libroSeleccionado;

    private static ArrayList<Prestamo> prestamosActivos = new ArrayList<>();

    private final ContextMenu sugerenciasMenu = new ContextMenu();
    LibrosDAO librosDAO = new LibrosDAO();

    @FXML
    void clickRegistrarPrestamo(ActionEvent event) {

        if (libroSeleccionado == null) return;

        if(libroSeleccionado.getUnidades() < 3){
            Alertas.mostrarError("No hay suficientes unidades disponibles para realizar un prestamo");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(Paths.PRESTAMO_BIBLIOTECARIO)
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
            aplicarEstilosDialogo(dialog);

            // 🔹 Botón cerrar (el formulario maneja registrar/cancelar)
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();

        } catch (Exception e) {
            Alertas.mostrarError("ERROR: " + e.getMessage());
        }
        txtBuscarLibro.clear();
    }

    @FXML
    void clickRegistrarDevolucion(ActionEvent event) {
        if (libroSeleccionado == null) return;

        prestamosActivos = prestamosDAO.buscarPrestamosLibro(libroSeleccionado.getId());

        if(prestamosActivos.isEmpty()){
            Alertas.mostrarError("No hay prestamos activos para este libro");
            return;
        }

        System.out.println("Prestamos activos: " + prestamosActivos.size() + " para el libro: " + prestamosActivos.getFirst().getEstudiante());


        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(Paths.DEVOLUCION_BIBLIOTECARIO)
            );

            // 🔹 El root ES un VBox
            VBox root = loader.load();

            // 🔹 Controller del préstamo
            DevolucionController controller = loader.getController();
            controller.setLibro(libroSeleccionado);
            controller.setPrestamos(prestamosActivos);

            // 🔹 Diálogo
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Registrar devolución");
            dialog.getDialogPane().setContent(root);
            aplicarEstilosDialogo(dialog);

            // 🔹 Botón cerrar (el formulario maneja registrar/cancelar)
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            dialog.showAndWait();

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }

        txtBuscarLibro.clear();
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

            List<Libro> resultados = librosDAO.buscarSimilares(newText);

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
