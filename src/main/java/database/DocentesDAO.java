package database;

import model.Docente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocentesDAO {

    private static final Logger LOGGER = Logger.getLogger(DocentesDAO.class.getName());

    public List<Docente> obtenerDocentes() {
        List<Docente> lista = new ArrayList<>();
        String sql = "SELECT * FROM docentes ORDER BY apellido_1, nombre_1";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Docente(
                        rs.getInt("id"),
                        rs.getString("nombre_1"),
                        rs.getString("nombre_2"),
                        rs.getString("apellido_1"),
                        rs.getString("apellido_2")
                ));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los docentes: " + e.getMessage(), e);
        }

        return lista;
    }

    
    public List<Docente> obtenerPaginados(int limit, int offset, String busqueda) {
        List<Docente> lista = new ArrayList<>();
        String sql = "SELECT * FROM docentes WHERE nombre_1 LIKE ? OR apellido_1 LIKE ? ORDER BY apellido_1, nombre_1 LIMIT ? OFFSET ?";
        String param = "%" + (busqueda == null ? "" : busqueda) + "%";
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param); ps.setString(2, param); ps.setInt(3, limit); ps.setInt(4, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { lista.add(new Docente(rs.getInt("id"), rs.getString("nombre_1"), rs.getString("nombre_2"), rs.getString("apellido_1"), rs.getString("apellido_2"))); }
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error paginados", e); }
        return lista;
    }

    public int contarTotal(String busqueda) {
        String sql = "SELECT COUNT(*) FROM docentes WHERE nombre_1 LIKE ? OR apellido_1 LIKE ?";
        String param = "%" + (busqueda == null ? "" : busqueda) + "%";
        try (Connection conn = ConexionSQLite.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param); ps.setString(2, param);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Error conteo", e); }
        return 0;
    }

    public boolean insertarDocente(Docente docente) {
        String sql = "INSERT INTO docentes (nombre_1, nombre_2, apellido_1, apellido_2) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeUpper(docente.getNombre_1()));
            ps.setString(2, safeUpper(docente.getNombre_2()));
            ps.setString(3, safeUpper(docente.getApellido_1()));
            ps.setString(4, safeUpper(docente.getApellido_2()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar docente: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean actualizarDocente(Docente docente) {
        String sql = "UPDATE docentes SET nombre_1 = ?, nombre_2 = ?, apellido_1 = ?, apellido_2 = ? WHERE id = ?";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeUpper(docente.getNombre_1()));
            ps.setString(2, safeUpper(docente.getNombre_2()));
            ps.setString(3, safeUpper(docente.getApellido_1()));
            ps.setString(4, safeUpper(docente.getApellido_2()));
            ps.setInt(5, docente.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar docente: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean eliminarDocente(int id) {
        String sql = "DELETE FROM docentes WHERE id = ?";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar docente: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean docenteTieneRegistros(int id) {
        String sqlPrestamos = "SELECT COUNT(*) FROM prestamos WHERE id_docente = ?";
        String sqlPlataforma = "SELECT COUNT(*) FROM registro_plataforma WHERE id_docente = ?";

        try (Connection conn = ConexionSQLite.conectar()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlPrestamos)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlPlataforma)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar dependencias de docente: " + e.getMessage(), e);
        }

        return false;
    }

    private String safeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }
}
