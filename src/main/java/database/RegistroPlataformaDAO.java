package database;

import utils.Alertas;
import utils.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistroPlataformaDAO {
    public boolean registrarUso(int id_docente, int id_motivo_uso) {
        String query = "INSERT INTO registro_plataforma (id_docente, id_motivo_uso, total_minutos, fecha) VALUES (?, ?, ?, ?)";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, id_docente);
            ps.setInt(2, id_motivo_uso);
            ps.setInt(3, 0);
            ps.setString(4, Fechas.fechaActualISO());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            Alertas.mostrarError("Error al registrar el uso de la plataforma: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public boolean registrarUsoConHoras(int id_docente, int id_motivo_uso, String hora_inicio, String hora_fin, int total_minutos) {
        String query = "INSERT INTO registro_plataforma (id_docente, id_motivo_uso, hora_inicio, hora_fin, total_minutos, grado, fecha) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, id_docente);
            ps.setInt(2, id_motivo_uso);
            ps.setString(3, hora_inicio);
            ps.setString(4, hora_fin);
            ps.setInt(5, total_minutos);
            ps.setInt(6, 0);
            ps.setString(7, Fechas.fechaActualISO());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            Alertas.mostrarError("Error al registrar el uso de la plataforma: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public boolean registrarUsoConHorasYGrado(int id_docente, int id_motivo_uso, String hora_inicio, String hora_fin, int total_minutos, int grado) {
        String query = "INSERT INTO registro_plataforma (id_docente, id_motivo_uso, hora_inicio, hora_fin, total_minutos, grado, fecha) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, id_docente);
            ps.setInt(2, id_motivo_uso);
            ps.setString(3, hora_inicio);
            ps.setString(4, hora_fin);
            ps.setInt(5, total_minutos);
            ps.setInt(6, grado);
            ps.setString(7, Fechas.fechaActualISO());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            Alertas.mostrarError("Error al registrar el uso de la plataforma: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public Map<String, Integer> obtenerTopDocentesUsoPlataforma(int limite) {
        String query = """
                SELECT 
                    TRIM(
                        COALESCE(d.nombre_1, '') || ' ' || 
                        COALESCE(d.nombre_2, '') || ' ' || 
                        COALESCE(d.apellido_1, '') || ' ' || 
                        COALESCE(d.apellido_2, '')
                    ) AS docente,
                    COALESCE(SUM(r.total_minutos), 0) AS total_minutos
                FROM registro_plataforma r
                JOIN docentes d ON d.id = r.id_docente
                GROUP BY d.id, d.nombre_1, d.nombre_2, d.apellido_1, d.apellido_2
                HAVING COALESCE(SUM(r.total_minutos), 0) > 0
                ORDER BY COALESCE(SUM(r.total_minutos), 0) DESC
                LIMIT ?
                """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.put(rs.getString("docente"), rs.getInt("total_minutos"));
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener el top de docentes por uso de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    public Map<String, Integer> obtenerTopGradosUsoPlataforma(int limite) {
        String query = """
                SELECT 
                    grado,
                    COALESCE(SUM(total_minutos), 0) AS total_minutos
                FROM registro_plataforma
                WHERE grado > 0
                GROUP BY grado
                HAVING COALESCE(SUM(total_minutos), 0) > 0
                ORDER BY COALESCE(SUM(total_minutos), 0) DESC
                LIMIT ?
                """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int grado = rs.getInt("grado");
                    int totalMinutos = rs.getInt("total_minutos");
                    datos.put("Grado " + grado, totalMinutos);
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener el top de grados por uso de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    public Map<String, Integer> obtenerTopDocentesUsoPlataforma(String fechaInicio, String fechaFin, int limite) {
        String query = """
                SELECT 
                    TRIM(
                        COALESCE(d.nombre_1, '') || ' ' || 
                        COALESCE(d.nombre_2, '') || ' ' || 
                        COALESCE(d.apellido_1, '') || ' ' || 
                        COALESCE(d.apellido_2, '')
                    ) AS docente,
                    COALESCE(SUM(r.total_minutos), 0) AS total_minutos
                FROM registro_plataforma r
                JOIN docentes d ON d.id = r.id_docente
                WHERE r.fecha >= ? AND r.fecha <= ?
                GROUP BY d.id, d.nombre_1, d.nombre_2, d.apellido_1, d.apellido_2
                HAVING COALESCE(SUM(r.total_minutos), 0) > 0
                ORDER BY COALESCE(SUM(r.total_minutos), 0) DESC
                LIMIT ?
                """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ps.setInt(3, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.put(rs.getString("docente"), rs.getInt("total_minutos"));
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener el top de docentes por uso de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    public Map<String, Integer> obtenerTopGradosUsoPlataforma(String fechaInicio, String fechaFin, int limite) {
        String query = """
                SELECT 
                    grado,
                    COALESCE(SUM(total_minutos), 0) AS total_minutos
                FROM registro_plataforma
                WHERE grado > 0 AND fecha >= ? AND fecha <= ?
                GROUP BY grado
                HAVING COALESCE(SUM(total_minutos), 0) > 0
                ORDER BY COALESCE(SUM(total_minutos), 0) DESC
                LIMIT ?
                """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ps.setInt(3, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int grado = rs.getInt("grado");
                    int totalMinutos = rs.getInt("total_minutos");
                    datos.put("Grado " + grado, totalMinutos);
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener el top de grados por uso de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    public Map<String, Integer> obtenerTopMotivosUsoPlataforma(int limite) {
        String query = """
                SELECT COALESCE(mp.nombre_motivo, 'Sin motivo') AS motivo,
                       COUNT(*) AS total
                FROM registro_plataforma r
                LEFT JOIN motivos_plataforma mp ON mp.id = r.id_motivo_uso
                GROUP BY r.id_motivo_uso, mp.nombre_motivo
                HAVING COUNT(*) > 0
                ORDER BY total DESC
                LIMIT ?
                """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.put(rs.getString("motivo"), rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener el top de motivos de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    public Map<String, Integer> obtenerTopMotivosUsoPlataforma(String fechaInicio, String fechaFin, int limite) {
        String query = """
                SELECT COALESCE(mp.nombre_motivo, 'Sin motivo') AS motivo,
                       COUNT(*) AS total
                FROM registro_plataforma r
                LEFT JOIN motivos_plataforma mp ON mp.id = r.id_motivo_uso
                WHERE r.fecha >= ? AND r.fecha <= ?
                GROUP BY r.id_motivo_uso, mp.nombre_motivo
                HAVING COUNT(*) > 0
                ORDER BY total DESC
                LIMIT ?
                """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ps.setInt(3, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.put(rs.getString("motivo"), rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener el top de motivos de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    public List<Map<String, Object>> obtenerTopMotivosUsoPlataformaConTiempo(int limite) {
        String query = """
                SELECT COALESCE(mp.nombre_motivo, 'Sin motivo') AS motivo,
                       COUNT(*) AS total,
                       COALESCE(SUM(r.total_minutos), 0) AS minutos
                FROM registro_plataforma r
                LEFT JOIN motivos_plataforma mp ON mp.id = r.id_motivo_uso
                GROUP BY r.id_motivo_uso, mp.nombre_motivo
                HAVING COUNT(*) > 0
                ORDER BY total DESC
                LIMIT ?
                """;

        List<Map<String, Object>> datos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("motivo", rs.getString("motivo"));
                    fila.put("total", rs.getInt("total"));
                    fila.put("minutos", rs.getInt("minutos"));
                    datos.add(fila);
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener el top de motivos de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    public List<Map<String, Object>> obtenerTopMotivosUsoPlataformaConTiempo(String fechaInicio, String fechaFin, int limite) {
        String query = """
                SELECT COALESCE(mp.nombre_motivo, 'Sin motivo') AS motivo,
                       COUNT(*) AS total,
                       COALESCE(SUM(r.total_minutos), 0) AS minutos
                FROM registro_plataforma r
                LEFT JOIN motivos_plataforma mp ON mp.id = r.id_motivo_uso
                WHERE r.fecha >= ? AND r.fecha <= ?
                GROUP BY r.id_motivo_uso, mp.nombre_motivo
                HAVING COUNT(*) > 0
                ORDER BY total DESC
                LIMIT ?
                """;

        List<Map<String, Object>> datos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ps.setInt(3, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("motivo", rs.getString("motivo"));
                    fila.put("total", rs.getInt("total"));
                    fila.put("minutos", rs.getInt("minutos"));
                    datos.add(fila);
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener el top de motivos de plataforma: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }
}
