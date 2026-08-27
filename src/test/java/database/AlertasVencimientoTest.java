package database;

import model.AlertaPrestamo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AlertasVencimientoTest {

    private final PrestamosDAO prestamosDAO = new PrestamosDAO();

    @Test
    @DisplayName("Verifica la detección automática de alertas: Vencido, Por vencer hoy y Próximo a vencer")
    void testAlertasVencimiento() {
        int idLibro = 0;
        int idEstudiante = 0;
        int idPrestamoVencido = 0;
        int idPrestamoHoy = 0;
        int idPrestamoProximo = 0;

        try (Connection conn = ConexionSQLite.conectar()) {
            // 1. Insertar libro y estudiante de prueba
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO libros (titulo, autor, id_categoria, unidades, editorial, ubicacion) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "Libro Test Alertas");
                ps.setString(2, "Autor Test");
                ps.setInt(3, 1);
                ps.setInt(4, 10);
                ps.setString(5, "Editorial Test");
                ps.setString(6, "Estante 1");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idLibro = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO estudiantes (identificacion, grado, apellido_1, apellido_2, nombre_1, nombre_2, genero) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, 99988877);
                ps.setInt(2, 11);
                ps.setString(3, "Perez");
                ps.setString(4, "Gomez");
                ps.setString(5, "Juan");
                ps.setString(6, "Carlos");
                ps.setString(7, "Masculino");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idEstudiante = rs.getInt(1);
                }
            }

            // 2. Insertar 3 préstamos de prueba
            LocalDate hoy = LocalDate.now();
            String fVencida = hoy.minusDays(4).toString();
            String fHoy = hoy.toString();
            String fProxima = hoy.plusDays(1).toString();

            // Préstamo vencido
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO prestamos (id_libro, id_estudiante, id_motivo, id_docente, fecha_prestamo, fecha_limite, estado) VALUES (?, ?, 1, 1, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idLibro);
                ps.setInt(2, idEstudiante);
                ps.setString(3, hoy.minusDays(10).toString());
                ps.setString(4, fVencida);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idPrestamoVencido = rs.getInt(1);
                }
            }

            // Préstamo que vence hoy
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO prestamos (id_libro, id_estudiante, id_motivo, id_docente, fecha_prestamo, fecha_limite, estado) VALUES (?, ?, 1, 1, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idLibro);
                ps.setInt(2, idEstudiante);
                ps.setString(3, hoy.minusDays(5).toString());
                ps.setString(4, fHoy);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idPrestamoHoy = rs.getInt(1);
                }
            }

            // Préstamo próximo a vencer
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO prestamos (id_libro, id_estudiante, id_motivo, id_docente, fecha_prestamo, fecha_limite, estado) VALUES (?, ?, 1, 1, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idLibro);
                ps.setInt(2, idEstudiante);
                ps.setString(3, hoy.minusDays(2).toString());
                ps.setString(4, fProxima);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idPrestamoProximo = rs.getInt(1);
                }
            }

            // 3. Obtener alertas y verificar
            List<AlertaPrestamo> alertas = prestamosDAO.obtenerAlertasVencimiento();
            assertThat(alertas).isNotEmpty();

            boolean encontroVencido = false;
            boolean encontroHoy = false;
            boolean encontroProximo = false;

            for (AlertaPrestamo a : alertas) {
                if (a.getIdPrestamo() == idPrestamoVencido) {
                    encontroVencido = true;
                    assertThat(a.getTipo()).isEqualTo(AlertaPrestamo.TipoAlerta.VENCIDO);
                    assertThat(a.getDescripcion()).contains("VENCIDO hace 4 día(s)");
                } else if (a.getIdPrestamo() == idPrestamoHoy) {
                    encontroHoy = true;
                    assertThat(a.getTipo()).isEqualTo(AlertaPrestamo.TipoAlerta.POR_VENCER_HOY);
                    assertThat(a.getDescripcion()).contains("Vence HOY");
                } else if (a.getIdPrestamo() == idPrestamoProximo) {
                    encontroProximo = true;
                    assertThat(a.getTipo()).isEqualTo(AlertaPrestamo.TipoAlerta.PROXIMO_A_VENCER);
                    assertThat(a.getDescripcion()).contains("Vence en 1 día(s)");
                }
            }

            assertThat(encontroVencido).isTrue();
            assertThat(encontroHoy).isTrue();
            assertThat(encontroProximo).isTrue();

        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Fallo en prueba de alertas: " + e.getMessage());
        } finally {
            // Limpieza
            try (Connection conn = ConexionSQLite.conectar();
                 Statement st = conn.createStatement()) {
                if (idPrestamoVencido > 0) st.execute("DELETE FROM prestamos WHERE id = " + idPrestamoVencido);
                if (idPrestamoHoy > 0) st.execute("DELETE FROM prestamos WHERE id = " + idPrestamoHoy);
                if (idPrestamoProximo > 0) st.execute("DELETE FROM prestamos WHERE id = " + idPrestamoProximo);
                if (idLibro > 0) st.execute("DELETE FROM libros WHERE id = " + idLibro);
                if (idEstudiante > 0) st.execute("DELETE FROM estudiantes WHERE id = " + idEstudiante);
            } catch (Exception ignored) {
            }
        }
    }
}
