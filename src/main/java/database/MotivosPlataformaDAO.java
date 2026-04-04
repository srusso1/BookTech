package database;

import model.MotivoPlataforma;
import utils.Alertas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MotivosPlataformaDAO {

    public ArrayList<MotivoPlataforma> obtenerMotivosPlataforma() {
        return obtenerMotivosPlataformaActivos();
    }

    public ArrayList<MotivoPlataforma> obtenerMotivosPlataformaActivos() {
        ArrayList<MotivoPlataforma> motivos = new ArrayList<>();
        String query = "SELECT id, nombre_motivo, estado FROM motivos_plataforma WHERE estado = 1 ORDER BY nombre_motivo";

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                motivos.add(new MotivoPlataforma(
                        rs.getInt("id"),
                        rs.getString("nombre_motivo"),
                        rs.getInt("estado")
                ));
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener motivos de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return motivos;
    }

    public ArrayList<MotivoPlataforma> obtenerTodosMotivosPlataforma() {
        ArrayList<MotivoPlataforma> motivos = new ArrayList<>();
        String query = "SELECT id, nombre_motivo, estado FROM motivos_plataforma ORDER BY estado DESC, nombre_motivo";

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                motivos.add(new MotivoPlataforma(
                        rs.getInt("id"),
                        rs.getString("nombre_motivo"),
                        rs.getInt("estado")
                ));
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener motivos de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return motivos;
    }

    public boolean agregarMotivoPlataforma(String nombreMotivo) {
        String query = "INSERT INTO motivos_plataforma (nombre_motivo, estado) VALUES (?, 1)";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, nombreMotivo.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Alertas.mostrarError("Error al registrar motivo de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public boolean actualizarNombreMotivoPlataforma(int id, String nuevoNombre) {
        String query = "UPDATE motivos_plataforma SET nombre_motivo = ? WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, nuevoNombre.trim().toUpperCase());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Alertas.mostrarError("Error al actualizar motivo de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public boolean actualizarEstadoMotivoPlataforma(int id, int estado) {
        String query = "UPDATE motivos_plataforma SET estado = ? WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Alertas.mostrarError("Error al actualizar estado del motivo de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }
}

