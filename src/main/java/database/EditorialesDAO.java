package database;

import model.Editorial;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EditorialesDAO {
    private static final Logger LOGGER = Logger.getLogger(EditorialesDAO.class.getName());

    public List<Editorial> obtenerEditorialesActivas() {
        List<Editorial> editoriales = new ArrayList<>();
        String query = "SELECT * FROM editoriales WHERE estado = 1 ORDER BY nombre ASC";
        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                editoriales.add(new Editorial(rs.getInt("id"), rs.getString("nombre"), rs.getInt("estado")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener editoriales activas: " + e.getMessage(), e);
        }
        return editoriales;
    }

    public List<Editorial> obtenerTodas() {
        List<Editorial> editoriales = new ArrayList<>();
        String query = "SELECT * FROM editoriales ORDER BY nombre ASC";
        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                editoriales.add(new Editorial(rs.getInt("id"), rs.getString("nombre"), rs.getInt("estado")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todas las editoriales: " + e.getMessage(), e);
        }
        return editoriales;
    }

    public boolean insertarEditorial(Editorial editorial) {
        String query = "INSERT INTO editoriales(nombre, estado) VALUES(?, ?)";
        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, editorial.getNombre());
            ps.setInt(2, editorial.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar editorial: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean actualizarEditorial(Editorial editorial) {
        String query = "UPDATE editoriales SET nombre = ?, estado = ? WHERE id = ?";
        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, editorial.getNombre());
            ps.setInt(2, editorial.getEstado());
            ps.setInt(3, editorial.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar editorial: " + e.getMessage(), e);
            return false;
        }
    }
}
