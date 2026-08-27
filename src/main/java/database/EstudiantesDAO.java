package database;

import model.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EstudiantesDAO {

    private static final Logger LOGGER = Logger.getLogger(EstudiantesDAO.class.getName());

    public static final int RESULTADO_INSERTADO = 1;
    public static final int RESULTADO_ACTUALIZADO = 2;
    public static final int RESULTADO_SIN_CAMBIOS = 3;

    public ArrayList<Estudiante> obtenerEstudiantes() {
        ArrayList<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY apellido_1, nombre_1";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Estudiante(
                        rs.getInt("id"),
                        rs.getInt("identificacion"),
                        rs.getInt("grado"),
                        rs.getString("apellido_1"),
                        rs.getString("apellido_2"),
                        rs.getString("nombre_1"),
                        rs.getString("nombre_2"),
                        rs.getString("genero")
                ));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los estudiantes: " + e.getMessage(), e);
        }

        return lista;
    }

    public Estudiante obtenerEstudiante(int id) {
        String sql = "SELECT * FROM estudiantes WHERE id = ?";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Estudiante(
                            rs.getInt("id"),
                            rs.getInt("identificacion"),
                            rs.getInt("grado"),
                            rs.getString("apellido_1"),
                            rs.getString("apellido_2"),
                            rs.getString("nombre_1"),
                            rs.getString("nombre_2"),
                            rs.getString("genero")
                    );
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener el estudiante: " + e.getMessage(), e);
        }

        return null;
    }

    public ArrayList<Integer> obtenerGrados() {
        ArrayList<Integer> grados = new ArrayList<>();
        String sql = "SELECT DISTINCT grado FROM estudiantes ORDER BY grado";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                grados.add(rs.getInt("grado"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los grados: " + e.getMessage(), e);
        }
        return grados;
    }

    public Estudiante obtenerEstudiantePorIdentificacion(long identificacion) {
        String sql = "SELECT * FROM estudiantes WHERE identificacion = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, identificacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Estudiante(
                            rs.getInt("id"),
                            rs.getLong("identificacion"),
                            rs.getInt("grado"),
                            rs.getString("apellido_1"),
                            rs.getString("apellido_2"),
                            rs.getString("nombre_1"),
                            rs.getString("nombre_2"),
                            rs.getString("genero")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener estudiante por identificación: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean existeIdentificacionEnOtroRegistro(long identificacion, int idExcluir) {
        String sql = "SELECT COUNT(*) total FROM estudiantes WHERE identificacion = ? AND id <> ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, identificacion);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al validar identificación: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean actualizarEstudiante(Estudiante estudiante) {
        String sql = """
                UPDATE estudiantes
                SET identificacion = ?, grado = ?, apellido_1 = ?, apellido_2 = ?, nombre_1 = ?, nombre_2 = ?, genero = ?
                WHERE id = ?
                """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, estudiante.getIdentificacion());
            ps.setInt(2, estudiante.getGrado());
            ps.setString(3, normalizarTexto(estudiante.getApellido_1()));
            ps.setString(4, normalizarTexto(estudiante.getApellido_2()));
            ps.setString(5, normalizarTexto(estudiante.getNombre_1()));
            ps.setString(6, normalizarTexto(estudiante.getNombre_2()));
            ps.setString(7, normalizarTexto(estudiante.getGenero()));
            ps.setInt(8, estudiante.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar estudiante: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean insertarEstudiante(Estudiante estudiante) {
        String sql = """
                INSERT INTO estudiantes (identificacion, grado, apellido_1, apellido_2, nombre_1, nombre_2, genero)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, estudiante.getIdentificacion());
            ps.setInt(2, estudiante.getGrado());
            ps.setString(3, normalizarTexto(estudiante.getApellido_1()));
            ps.setString(4, normalizarTexto(estudiante.getApellido_2()));
            ps.setString(5, normalizarTexto(estudiante.getNombre_1()));
            ps.setString(6, normalizarTexto(estudiante.getNombre_2()));
            ps.setString(7, normalizarTexto(estudiante.getGenero()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar estudiante: " + e.getMessage(), e);
        }
        return false;
    }

    public int guardarOModificarPorIdentificacion(Estudiante estudiante) {
        Estudiante existente = obtenerEstudiantePorIdentificacion(estudiante.getIdentificacion());
        if (existente == null) {
            return insertarEstudiante(estudiante) ? RESULTADO_INSERTADO : RESULTADO_SIN_CAMBIOS;
        }

        boolean cambio = existente.getGrado() != estudiante.getGrado()
                || !textoSeguro(existente.getApellido_1()).equals(textoSeguro(estudiante.getApellido_1()))
                || !textoSeguro(existente.getApellido_2()).equals(textoSeguro(estudiante.getApellido_2()))
                || !textoSeguro(existente.getNombre_1()).equals(textoSeguro(estudiante.getNombre_1()))
                || !textoSeguro(existente.getNombre_2()).equals(textoSeguro(estudiante.getNombre_2()))
                || !textoSeguro(existente.getGenero()).equals(textoSeguro(estudiante.getGenero()));

        if (!cambio) {
            return RESULTADO_SIN_CAMBIOS;
        }

        estudiante.setId(existente.getId());
        return actualizarEstudiante(estudiante) ? RESULTADO_ACTUALIZADO : RESULTADO_SIN_CAMBIOS;
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase();
    }

    private String textoSeguro(String valor) {
        return normalizarTexto(valor);
    }
}
