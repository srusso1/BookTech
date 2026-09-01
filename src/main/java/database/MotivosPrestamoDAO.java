package database;

import model.MotivoPrestamo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MotivosPrestamoDAO {

    private static final Logger LOGGER = Logger.getLogger(MotivosPrestamoDAO.class.getName());

    public List<MotivoPrestamo> obtenerMotivosPrestamo() {
        return obtenerMotivosPrestamoActivos();
    }

    public List<MotivoPrestamo> obtenerMotivosPrestamoActivos() {
        List<MotivoPrestamo> motivos = new ArrayList<>();
        String query = "SELECT id, nombre_motivo, estado FROM motivos_prestamo WHERE estado = 1 ORDER BY nombre_motivo";

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                motivos.add(new MotivoPrestamo(
                        rs.getInt("id"),
                        rs.getString("nombre_motivo"),
                        rs.getInt("estado")
                ));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener motivos de prÃ©stamo activos: " + e.getMessage(), e);
        }

        return motivos;
    }

    public List<MotivoPrestamo> obtenerTodosMotivosPrestamo() {
        List<MotivoPrestamo> motivos = new ArrayList<>();
        String query = "SELECT id, nombre_motivo, estado FROM motivos_prestamo ORDER BY estado DESC, nombre_motivo";

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                motivos.add(new MotivoPrestamo(
                        rs.getInt("id"),
                        rs.getString("nombre_motivo"),
                        rs.getInt("estado")
                ));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los motivos de prÃ©stamo: " + e.getMessage(), e);
        }

        return motivos;
    }

    public boolean agregarMotivoPrestamo(String nombreMotivo) {
        String query = "INSERT INTO motivos_prestamo (nombre_motivo, estado) VALUES (?, 1)";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, nombreMotivo.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar motivo de prÃ©stamo: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean actualizarNombreMotivoPrestamo(int id, String nuevoNombre) {
        String query = "UPDATE motivos_prestamo SET nombre_motivo = ? WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, nuevoNombre.trim().toUpperCase());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar motivo de prÃ©stamo: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean actualizarEstadoMotivoPrestamo(int id, int estado) {
        String query = "UPDATE motivos_prestamo SET estado = ? WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar estado del motivo de prÃ©stamo: " + e.getMessage(), e);
        }
        return false;
    }
}
