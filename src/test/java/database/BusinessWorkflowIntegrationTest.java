package database;

import model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.Fechas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BusinessWorkflowIntegrationTest {

    private final LibrosDAO librosDAO = new LibrosDAO();
    private final PrestamosDAO prestamosDAO = new PrestamosDAO();
    private final InformesDAO informesDAO = new InformesDAO();
    private final EstudiantesDAO estudiantesDAO = new EstudiantesDAO();
    private final DocentesDAO docentesDAO = new DocentesDAO();
    private final MotivosPrestamoDAO motivosDAO = new MotivosPrestamoDAO();

    @Test
    @DisplayName("Flujo E2E completo: Crear libro -> Préstamo -> Validación de Stock -> Devolución -> Resumen en Informes")
    void testFlujoCompletoPrestamoYDevolucion() {
        // 1. Crear libro de prueba
        String tituloUnico = "LIBRO TEST E2E " + System.currentTimeMillis();
        Libro libro = new Libro(tituloUnico, "ESTANTE A1", 1, 1, "AUTOR TEST", 5);
        boolean libroRegistrado = librosDAO.registrarLibro(libro);
        assertThat(libroRegistrado).isTrue();

        // Buscar el libro recién creado para obtener su ID autogenerado
        List<Libro> librosEncontrados = librosDAO.buscarSimilares(tituloUnico);
        assertThat(librosEncontrados).isNotEmpty();
        Libro libroCreado = librosEncontrados.get(0);
        int libroId = libroCreado.getId();
        assertThat(libroId).isGreaterThan(0);
        assertThat(libroCreado.getUnidades()).isEqualTo(5);

        // 2. Obtener estudiante, docente y motivo válidos
        List<Estudiante> estudiantes = estudiantesDAO.obtenerEstudiantes();
        assertThat(estudiantes).isNotEmpty();
        Estudiante estudiante = estudiantes.get(0);

        List<Docente> docentes = docentesDAO.obtenerDocentes();
        assertThat(docentes).isNotEmpty();
        Docente docente = docentes.get(0);

        List<MotivoPrestamo> motivos = motivosDAO.obtenerTodosMotivosPrestamo();
        assertThat(motivos).isNotEmpty();
        MotivoPrestamo motivo = motivos.get(0);

        // 3. Registrar préstamo del libro
        LocalDate hoy = LocalDate.now();
        String fPrestamo = Fechas.convertirAISO(hoy);
        String fLimite = Fechas.convertirAISO(hoy.plusDays(3));

        boolean prestamoGuardado = prestamosDAO.registrarPrestamo(
                libroId,
                estudiante.getId(),
                motivo.getId(),
                docente.getId(),
                fPrestamo,
                fLimite
        );
        assertThat(prestamoGuardado).isTrue();

        // 4. Reducir unidad en inventario y verificar stock
        boolean reduccionExitosa = librosDAO.disminuirUnidadLibro(libroId);
        assertThat(reduccionExitosa).isTrue();

        Libro libroPostPrestamo = librosDAO.buscarSimilares(tituloUnico).get(0);
        assertThat(libroPostPrestamo.getUnidades()).isEqualTo(4);

        // 5. Verificar que el préstamo aparece en la lista de préstamos del libro
        List<Prestamo> prestamosLibro = prestamosDAO.buscarPrestamosLibro(libroId);
        assertThat(prestamosLibro).isNotEmpty();
        Prestamo prestamoActivo = prestamosLibro.get(0);
        assertThat(prestamoActivo.getId_libro()).isEqualTo(libroId);

        // 6. Registrar la devolución
        boolean devolucionRegistrada = prestamosDAO.registrarDevolucion(prestamoActivo);
        assertThat(devolucionRegistrada).isTrue();

        boolean aumentoExitoso = librosDAO.aumentarUnidadLibro(libroId);
        assertThat(aumentoExitoso).isTrue();

        Libro libroPostDevolucion = librosDAO.buscarSimilares(tituloUnico).get(0);
        assertThat(libroPostDevolucion.getUnidades()).isEqualTo(5);

        // 7. Verificar que el historial del estudiante refleja el préstamo
        List<Prestamo> historial = informesDAO.obtenerHistorialEstudiante(estudiante.getId());
        assertThat(historial).isNotEmpty();

        // 8. Verificar que el resumen general consolida datos
        Map<String, Object> resumen = informesDAO.obtenerResumenGeneral(null, null);
        assertThat(resumen).isNotEmpty();
        assertThat((Integer) resumen.get("totalPrestamos")).isGreaterThan(0);

        // 9. Limpieza de datos de prueba
        boolean eliminado = librosDAO.eliminarLibro(libroId);
        assertThat(eliminado).isTrue();
    }
}
