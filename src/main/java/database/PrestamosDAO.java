package database;

import model.Docente;
import model.MotivoPrestamo;
import model.Prestamo;
import utils.Alertas;
import utils.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PrestamosDAO {
    // ESTADO: 0 - Prestado, 1 - Devuelto, 2 - Pendiente

    public boolean registrarPrestamo(int idLibro, int id_estudiante, int id_motivo, int id_docente, String fechaPrestamo, String fechaLimite){
        String query = "INSERT INTO prestamos (id_libro, id_estudiante, id_motivo, id_docente, fecha_prestamo, fecha_limite) VALUES (?, ?, ?, ?, ?, ?)";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, idLibro);
            ps.setInt(2, id_estudiante);
            ps.setInt(3, id_motivo);
            ps.setInt(4, id_docente);
            ps.setString(5, fechaPrestamo);
            ps.setString(6, fechaLimite);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Alertas.mostrarError("Error al registrar el prestamo: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public ArrayList<Prestamo> buscarPrestamosLibro(int idLibro) {

        String query = """
        SELECT 
            p.id,
            p.id_libro,
            p.id_docente,
            p.id_motivo,
            p.estado,
            p.fecha_prestamo,
            p.fecha_limite,
            e.grado,
            l.titulo,
            e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante
        FROM prestamos p
        JOIN libros l ON l.id = p.id_libro
        JOIN estudiantes e ON e.id = p.id_estudiante
        WHERE p.id_libro = ? AND p.estado != 1
    """;

        ArrayList<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, idLibro);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int idDocente = rs.getInt("id_docente");
                int idMotivo = rs.getInt("id_motivo");
                
                Docente docente = obtenerDocentePorId(idDocente);
                MotivoPrestamo motivoPrestamo = obtenerMotivoPorId(idMotivo);
                
                prestamos.add(new Prestamo(
                        rs.getInt("id"),
                        rs.getInt("id_libro"),
                        rs.getString("estudiante"),
                        rs.getString("fecha_prestamo"),
                        rs.getString("fecha_limite"),
                        rs.getInt("estado"),
                        rs.getInt("grado"),
                        rs.getString("titulo"),
                        docente,
                        motivoPrestamo
                ));
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al buscar los préstamos: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }

        return prestamos;
    }

    public ArrayList<Prestamo> buscarPrestamosActivos() {

        String query = """
        SELECT 
            p.id,
            p.id_libro,
            p.id_docente,
            p.id_motivo,
            p.estado,
            p.fecha_prestamo,
            p.fecha_limite,
            e.grado,
            l.titulo,
            e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante
        FROM prestamos p
        JOIN libros l ON l.id = p.id_libro
        JOIN estudiantes e ON e.id = p.id_estudiante
        WHERE p.estado != 1
    """;

        ArrayList<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idDocente = rs.getInt("id_docente");
                int idMotivo = rs.getInt("id_motivo");
                
                Docente docente = obtenerDocentePorId(idDocente);
                MotivoPrestamo motivoPrestamo = obtenerMotivoPorId(idMotivo);
                
                prestamos.add(new Prestamo(
                        rs.getInt("id"),
                        rs.getInt("id_libro"),
                        rs.getString("estudiante"),
                        rs.getString("fecha_prestamo"),
                        rs.getString("fecha_limite"),
                        rs.getInt("estado"),
                        rs.getInt("grado"),
                        rs.getString("titulo"),
                        docente,
                        motivoPrestamo
                ));
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al buscar los préstamos: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }

        return prestamos;
    }

    public void actualizarPrestamosTarde(){
        String query = """
            UPDATE prestamos 
            SET estado = 2 
            WHERE estado = 0 AND fecha_limite < ?
        """;
        
        try {
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setString(1, Fechas.fechaActualISO());
            
            int actualizados = ps.executeUpdate();
            
            if (actualizados > 0) {
                Alertas.mostrarInfo("Se actualizaron " + actualizados + " prestamos. Consulte el modulo 'Prestamos activos'");
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al actualizar préstamos vencidos: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

    }

    public boolean actualizarEstado(Prestamo prestamo){
        String query = "UPDATE prestamos SET estado = ? WHERE id = ?";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, 2);
            ps.setInt(2, prestamo.getId());
            return ps.executeUpdate() > 0;
        }catch (SQLException e){
            Alertas.mostrarError("Error al actualizar el estado: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }


    public boolean registrarDevolucion(Prestamo prestamo){

        String query = """
                UPDATE prestamos
                SET estado = ?, fecha_devolucion = ?
                WHERE id = ?
            """;
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, 1);
            ps.setString(2, Fechas.fechaActualISO());
            ps.setInt(3, prestamo.getId());
            return ps.executeUpdate() > 0;
        }catch (SQLException e) {
            Alertas.mostrarError("Error al registrar la devolución: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public boolean validarPrestamo(int idLibro, int idEstudiante){
        String query = "SELECT * FROM prestamos WHERE id_estudiante = ? AND (estado = 0 OR estado = 2) AND id_libro = ?";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idLibro);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }catch (SQLException e){
            Alertas.mostrarError("Error al validar el prestamo: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public Map<String, Integer> obtenerPrestamosPorGenero() {

        String query = """
        SELECT 
            e.genero,
            COUNT(p.id) AS total_prestamos
        FROM prestamos p
        JOIN estudiantes e ON e.id = p.id_estudiante
        GROUP BY e.genero
    """;

        Map<String, Integer> datos = new HashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String genero = rs.getString("genero");
                int total = rs.getInt("total_prestamos");

                datos.put(genero, total);
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener estadísticas: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    public Map<String, Integer> obtenerPrestamosPorCategoria() {

        String query = """
        SELECT c.nombre_categoria, COUNT(p.id) AS total
        FROM libros l
        JOIN categorias c ON c.id = l.id_categoria
        LEFT JOIN prestamos p ON l.id = p.id_libro
        GROUP BY c.nombre_categoria
        ORDER BY total DESC
    """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String categoria = rs.getString("nombre_categoria");
                int total = rs.getInt("total");

                datos.put(categoria, total);
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener préstamos por categoría: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    public Map<String, Integer> obtenerPrestamosPorDocenteTop(int limite) {

        String query = """
        SELECT 
            d.id,
            d.nombre_1 || ' ' || d.nombre_2 || ' ' || d.apellido_1 || ' ' || d.apellido_2 AS docente,
            COUNT(p.id) AS total
        FROM prestamos p
        JOIN docentes d ON d.id = p.id_docente
        GROUP BY d.id, d.apellido_1, d.apellido_2, d.nombre_1, d.nombre_2
        ORDER BY total DESC
        LIMIT ?
    """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.put(
                            rs.getString("docente"),
                            rs.getInt("total")
                    );
                }
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener préstamos por docente: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    public Map<String, Integer> obtenerPrestamosPorGradoTop(int limite) {

        String query = """
        SELECT e.grado, COUNT(p.id) AS total
        FROM prestamos p
        JOIN estudiantes e ON e.id = p.id_estudiante
        GROUP BY e.grado
        ORDER BY total DESC
        LIMIT ?
    """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, limite);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                datos.put(
                        "Grado " + rs.getInt("grado"),
                        rs.getInt("total")
                );
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener préstamos por grado: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }

        return datos;
    }

    private Docente obtenerDocentePorId(int idDocente) {
        String query = "SELECT id, nombre_1, nombre_2, apellido_1, apellido_2 FROM docentes WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, idDocente);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Docente(
                        rs.getInt("id"),
                        rs.getString("nombre_1"),
                        rs.getString("nombre_2"),
                        rs.getString("apellido_1"),
                        rs.getString("apellido_2")
                );
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener docente: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }
        return null;
    }

    private MotivoPrestamo obtenerMotivoPorId(int idMotivo) {
        String query = "SELECT id, nombre_motivo FROM motivos_prestamo WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, idMotivo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new MotivoPrestamo(
                        rs.getInt("id"),
                        rs.getString("nombre_motivo")
                );
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener motivo de préstamo: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }
        return null;
    }


}
