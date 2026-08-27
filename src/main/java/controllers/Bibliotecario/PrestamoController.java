package controllers.Bibliotecario;

import database.DocentesDAO;
import database.EstudiantesDAO;
import database.LibrosDAO;
import database.MotivosPrestamoDAO;
import database.PrestamosDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Docente;
import model.Estudiante;
import model.Libro;
import model.MotivoPrestamo;
import utils.Alertas;
import utils.BusquedaSugerencias;
import utils.Fechas;
import utils.Validaciones;

import java.util.ArrayList;


public class PrestamoController {

    @FXML private Label lblLibro;
    @FXML private TextField txtEstudiante;
    @FXML private DatePicker dpFechaDevolucion;
    @FXML private Label infoGrado;
    @FXML ComboBox<MotivoPrestamo> comboMotivosPrestamos;
    @FXML private ContextMenu sugerenciasDocente;
    @FXML private TextField txtDocente;
    @FXML private Label infoIdentificacion;
    @FXML private VBox contenedorInfoEstudiante;
    private final ContextMenu sugerenciasMenu = new ContextMenu();
    private ArrayList<Estudiante> listaEstudiantes = new ArrayList<>();
    private Estudiante estudianteSeleccionado;
    private Docente docenteSeleccionado;


    private Libro libro;
    PrestamosDAO prestamosDAO = new PrestamosDAO();
    LibrosDAO librosDAO = new LibrosDAO();
    EstudiantesDAO estudiantesDAO = new EstudiantesDAO();
    MotivosPrestamoDAO motivosPrestamoDAO = new MotivosPrestamoDAO();
    DocentesDAO docentesDAO = new DocentesDAO();
    ArrayList<MotivoPrestamo> motivosPrestamos = new ArrayList<>();
    ArrayList<Docente> listaDocentes = new ArrayList<>();
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

        motivosPrestamos = motivosPrestamoDAO.obtenerMotivosPrestamoActivos();
        comboMotivosPrestamos.getItems().addAll(motivosPrestamos);
        if (motivosPrestamos.isEmpty()) {
            Alertas.mostrarError("No hay motivos de préstamo activos. Solicite activarlos en Configuración.");
        }

        listaDocentes = docentesDAO.obtenerDocentes();

        configurarBusquedaDocentes();
    }

    private void registrarPrestamo(){
        Validaciones.ocultarPopOver(txtEstudiante);
        Validaciones.ocultarPopOver(dpFechaDevolucion.getEditor());

        if(!Validaciones.campoRequerido(txtEstudiante)){
            return;
        }

        if(estudianteSeleccionado == null){
            Alertas.mostrarError("Es necesario seleccionar un estudiante");
            return;
        }

        if(docenteSeleccionado == null){
            Alertas.mostrarError("Es necesario seleccionar un docente");
            return;
        }

        if(comboMotivosPrestamos.getSelectionModel().getSelectedItem() == null){
            Alertas.mostrarError("Es necesario seleccionar un motivo de prestamo");
            return;
        }


        if(dpFechaDevolucion.getValue() == null){
            Alertas.mostrarError("Es necesario establecer una fecha límite de devolución");
            return;
        }


        String fechaDevolucion = Fechas.convertirAISO(dpFechaDevolucion.getValue());
        String fechaHoy = Fechas.fechaActualISO();

        int comparacionFechas = Fechas.compararFechas(fechaDevolucion, fechaHoy);

        if(comparacionFechas < 0){
            Alertas.mostrarError("Fecha no valida, asegurate de elegir la fecha actual o una posterior");
            return;
        }

        if(comparacionFechas == 0){
            Alertas.mostrarInfo("La devolucion quedo para hoy. Recuerda indicarle al estudiante que debe regresar el libro el mismo dia");
        }

        int idLibro = libro.getId();
        int id_estudiante = estudianteSeleccionado.getId();
        MotivoPrestamo motivoPrestamo = comboMotivosPrestamos.getSelectionModel().getSelectedItem();
        int id_motivo = motivoPrestamo.getId();
        int id_docente = docenteSeleccionado.getId();

        if(prestamosDAO.validarPrestamo(idLibro, id_estudiante)){
            Alertas.mostrarError("El estudiante ya tiene un prestamo activo/pendiente para este libro");
            return;
        }


        if(prestamosDAO.registrarPrestamo(idLibro, id_estudiante, id_motivo, id_docente, fechaHoy, fechaDevolucion)){
            librosDAO.disminuirUnidadLibro(idLibro);
            Alertas.mostrarExito("Se registro el prestamo del libro: '" + libro.getTitulo() + "' al estudiante: " + estudianteSeleccionado.getNombreCompleto());
        }else{
            Alertas.mostrarError("Error al registrar el prestamo");
        }
        cerrar();
    }

    private void configurarBusquedaDocentes() {
        BusquedaSugerencias.configurar(
                txtDocente,
                sugerenciasDocente,
                listaDocentes,
                2,
                5,
                Docente::getNombreCompleto,
                Docente::getNombreCompleto,
                Docente::getNombreCompleto,
                doc -> docenteSeleccionado = doc,
                null
        );
    }

    private void configurarBusquedaEstudiantes() {

        ocultarInfoEstudiante();

        BusquedaSugerencias.configurar(
                txtEstudiante,
                sugerenciasMenu,
                listaEstudiantes,
                2,
                5,
                Estudiante::getNombreCompleto,
                Estudiante::getNombreCompletoYGrado,
                Estudiante::getNombreCompleto,
                est -> {
                    estudianteSeleccionado = est;
                    infoGrado.setText(String.valueOf(est.getGrado()));
                    infoIdentificacion.setText(String.valueOf(est.getIdentificacion()));
                    mostrarInfoEstudiante();
                },
                this::ocultarInfoEstudiante
        );
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
