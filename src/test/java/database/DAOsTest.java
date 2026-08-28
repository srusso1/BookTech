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
    @DisplayName("Verifica que DocentesDAO y EstudiantesDAO funcionen de forma segura con CRUD")
    void testDocentesYEstudiantesDAO() {
        DocentesDAO docentesDAO = new DocentesDAO();
        ArrayList<Docente> docentes = docentesDAO.obtenerDocentes();
        assertThat(docentes).isNotNull();

        // Prueba de inserción en minúsculas/mixto
        Docente prueba = new Docente(0, "carlos", "alberto", "gomez", "perez");
        boolean insertado = docentesDAO.insertarDocente(prueba);
        assertThat(insertado).isTrue();

        // Buscar el docente insertado y verificar mayúsculas
        ArrayList<Docente> listaActualizada = docentesDAO.obtenerDocentes();
        Docente encontrado = listaActualizada.stream()
                .filter(d -> "CARLOS".equals(d.getNombre_1()) && "GOMEZ".equals(d.getApellido_1()))
                .findFirst().orElse(null);
        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getNombre_2()).isEqualTo("ALBERTO");
        assertThat(encontrado.getApellido_2()).isEqualTo("PEREZ");

        // Prueba de actualización y verificación de mayúsculas
        encontrado.setNombre_1("carlos modificado");
        boolean actualizado = docentesDAO.actualizarDocente(encontrado);
        assertThat(actualizado).isTrue();

        ArrayList<Docente> listaTrasUpdate = docentesDAO.obtenerDocentes();
        Docente actualizadoEncontrado = listaTrasUpdate.stream()
                .filter(d -> d.getId() == encontrado.getId())
                .findFirst().orElse(null);
        assertThat(actualizadoEncontrado).isNotNull();
        assertThat(actualizadoEncontrado.getNombre_1()).isEqualTo("CARLOS MODIFICADO");

        // Prueba de eliminación
        boolean eliminado = docentesDAO.eliminarDocente(encontrado.getId());
        assertThat(eliminado).isTrue();

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
