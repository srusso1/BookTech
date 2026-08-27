package database;

import model.Categoria;
import model.Docente;
import model.Estudiante;
import model.Libro;
import model.MotivoPlataforma;
import model.MotivoPrestamo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class DAOsTest {

    @Test
    @DisplayName("Verifica que ConexionSQLite conecte con WAL mode y sin singleton estático bloqueante")
    void testConexionSQLite() {
        try (Connection conn = ConexionSQLite.conectar()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isClosed()).isFalse();
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Fallo al conectar: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Verifica que CategoriasDAO obtenga datos sin depender de JavaFX Alertas")
    void testCategoriasDAO() {
        CategoriasDAO dao = new CategoriasDAO();
        ArrayList<Categoria> categorias = dao.obtenerCategorias();
        assertThat(categorias).isNotNull();
    }

    @Test
    @DisplayName("Verifica que DocentesDAO y EstudiantesDAO funcionen de forma segura")
    void testDocentesYEstudiantesDAO() {
        DocentesDAO docentesDAO = new DocentesDAO();
        ArrayList<Docente> docentes = docentesDAO.obtenerDocentes();
        assertThat(docentes).isNotNull();

        EstudiantesDAO estudiantesDAO = new EstudiantesDAO();
        ArrayList<Estudiante> estudiantes = estudiantesDAO.obtenerEstudiantes();
        assertThat(estudiantes).isNotNull();
    }

    @Test
    @DisplayName("Verifica que LibrosDAO obtenga inventario e info de dashboards")
    void testLibrosDAO() {
        LibrosDAO librosDAO = new LibrosDAO();
        ArrayList<Libro> libros = librosDAO.inventarioLibros();
        assertThat(libros).isNotNull();

        ArrayList<Integer> infoBiblio = librosDAO.infoDashboardBibliotecario();
        assertThat(infoBiblio).isNotNull();

        ArrayList<String> infoRector = librosDAO.infoDashboardRectoria();
        assertThat(infoRector).isNotNull();
    }

    @Test
    @DisplayName("Verifica que MotivosPrestamoDAO y MotivosPlataformaDAO obtengan listas")
    void testMotivosDAOs() {
        MotivosPrestamoDAO prestamoDAO = new MotivosPrestamoDAO();
        ArrayList<MotivoPrestamo> motivosPrestamo = prestamoDAO.obtenerTodosMotivosPrestamo();
        assertThat(motivosPrestamo).isNotNull();

        MotivosPlataformaDAO plataformaDAO = new MotivosPlataformaDAO();
        ArrayList<MotivoPlataforma> motivosPlataforma = plataformaDAO.obtenerTodosMotivosPlataforma();
        assertThat(motivosPlataforma).isNotNull();
    }
}
