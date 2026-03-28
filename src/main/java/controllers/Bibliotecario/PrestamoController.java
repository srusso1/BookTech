package controllers.Bibliotecario;

import database.EstudiantesDAO;
import database.LibrosDAO;
import database.PrestamosDAO;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Estudiante;
import model.Libro;
import utils.Alertas;
import utils.Fechas;
import utils.Validaciones;

import java.util.ArrayList;
import java.util.List;


public class PrestamoController {

    @FXML private Label lblLibro;
    @FXML private TextField txtEstudiante;
    @FXML private DatePicker dpFechaDevolucion;
    @FXML private Label infoGrado;
    @FXML private Label infoIdentificacion;
    @FXML private VBox contenedorInfoEstudiante;
    private final ContextMenu sugerenciasMenu = new ContextMenu();
    private ArrayList<Estudiante> listaEstudiantes = new ArrayList<>();
    private Estudiante estudianteSeleccionado;


    private Libro libro;
    PrestamosDAO prestamosDAO = new PrestamosDAO();
    LibrosDAO librosDAO = new LibrosDAO();
    EstudiantesDAO estudiantesDAO = new EstudiantesDAO();

    // 🔹 método para recibir el libro
    public void setLibro(Libro libro) {
        this.libro = libro;
        lblLibro.setText(libro.getTitulo() + " — " + libro.getAutor());
    }

    @FXML
    void clickRegistrar() {
        registrarPrestamo();
    }

    @FXML
    void clickCancelar() {
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) lblLibro.getScene().getWindow();
        stage.close();
    }

    @FXML
    void initialize() {
        dpFechaDevolucion.setEditable(false);

        listaEstudiantes = estudiantesDAO.obtenerEstudiantes();

        configurarBusquedaEstudiantes();
    }

    private void registrarPrestamo(){
        Validaciones.ocultarPopOver(txtEstudiante);
        Validaciones.ocultarPopOver(dpFechaDevolucion.getEditor());

        if(!Validaciones.campoRequerido(txtEstudiante)){
            return;
        }

        if(dpFechaDevolucion.getValue() == null){
            Alertas.mostrarError("Es necesario establecer una fecha límite de devolución");
            return;
        }

        String fechaDevolucion = dpFechaDevolucion.getValue().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String fechaHoy = Fechas.fechaActual();

        if(!Fechas.esDespues(fechaDevolucion, fechaHoy)){
            Alertas.mostrarError("Fecha no válida, asegurate de elegir una fecha posterior a la actual");
            return;
        }
        int idLibro = libro.getId();
        int id_estudiante = estudianteSeleccionado.getId();

        if(prestamosDAO.validarPrestamo(idLibro, id_estudiante)){
            Alertas.mostrarError("El estudiante ya tiene un prestamo activo/pendiente para este libro");
            return;
        }


        if(prestamosDAO.registrarPrestamo(idLibro, id_estudiante,fechaHoy, fechaDevolucion)){
            librosDAO.disminuirUnidadLibro(idLibro);
            Alertas.mostrarExito("Se registro el prestamo del libro: '" + libro.getTitulo() + "' al estudiante: " + estudianteSeleccionado.getNombreCompleto());
        }else{
            Alertas.mostrarError("Error al registrar el prestamo");
        }
        cerrar();
    }

    private void configurarBusquedaEstudiantes() {

        ocultarInfoEstudiante();

        txtEstudiante.textProperty().addListener((obs, oldText, newText) -> {

            if (newText.length() < 2) {
                sugerenciasMenu.hide();
                return;
            }

            String filtro = newText.toUpperCase();

            List<Estudiante> resultados = listaEstudiantes.stream()
                    .filter(e -> e.getNombreCompleto().toUpperCase().contains(filtro))
                    .limit(5)
                    .toList();

            if (resultados.isEmpty()) {
                sugerenciasMenu.hide();
                Validaciones.agregarPopOver(txtEstudiante, "No hay coincidencias");
                return;
            }

            Validaciones.ocultarPopOver(txtEstudiante);

            List<MenuItem> items = new ArrayList<>();

            for (Estudiante est : resultados) {

                MenuItem item = new MenuItem(
                        est.getNombreCompletoYGrado()
                );

                item.setOnAction(e -> {
                    txtEstudiante.setText(est.getNombreCompleto());
                    estudianteSeleccionado = est; // 🔥 guardas el objeto completo
                    infoGrado.setText(String.valueOf(est.getGrado()));
                    infoIdentificacion.setText(String.valueOf(est.getIdentificacion()));
                    sugerenciasMenu.hide();
                    mostrarInfoEstudiante();
                });

                items.add(item);
            }

            sugerenciasMenu.getItems().setAll(items);

            if (!sugerenciasMenu.isShowing()) {
                sugerenciasMenu.show(txtEstudiante, Side.BOTTOM, 0, 0);
                ocultarInfoEstudiante();
            }
        });

        txtEstudiante.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) {
                sugerenciasMenu.hide();
                Validaciones.ocultarPopOver(txtEstudiante);
            }
        });
    }

    private void ocultarInfoEstudiante(){
        contenedorInfoEstudiante.setVisible(false);
        contenedorInfoEstudiante.setManaged(false);
    }

    private void mostrarInfoEstudiante(){
        contenedorInfoEstudiante.setVisible(true);
        contenedorInfoEstudiante.setManaged(true);
    }
}
