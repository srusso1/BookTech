package database;

import model.Docente;
import model.MotivoPrestamo;
import model.Prestamo;
import utils.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrestamosDAO {

    private static final Logger LOGGER = Logger.getLogger(PrestamosDAO.class.getName());
    // ESTADO: 0 - Prestado, 1 - Devuelto, 2 - Pendiente

    public boolean registrarPrestamo(int idLibro, int id_estudiante, int id_motivo, int id_docente, String fechaPrestamo, String fechaLimite) {
        String query = "INSERT INTO prestamos (id_libro, id_estudiante, id_motivo, id_docente, fecha_prestamo, fecha_limite, devuelto_tarde, dias_atraso) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, idLibro);
            ps.setInt(2, id_estudiante);
            ps.setInt(3, id_motivo);
            ps.setInt(4, id_docente);
            ps.setString(5, fechaPrestamo);
            ps.setString(6, fechaLimite);
            ps.setInt(7, 0);
            ps.setInt(8, 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar el préstamo: " + e.getMessage(), e);
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
            p.devuelto_tarde,
            p.dias_atraso,
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
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idDocente = rs.getInt("id_docente");
                    int idMotivo = rs.getInt("id_motivo");
                    
                    Docente docente = obtenerDocentePorId(conexion, idDocente);
                    MotivoPrestamo motivoPrestamo = obtenerMotivoPorId(conexion, idMotivo);
                    
                    Prestamo prestamo = new Prestamo(
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
                    );
                    prestamo.setDevuelto_tarde(rs.getInt("devuelto_tarde"));
                    prestamo.setDias_atraso(rs.getInt("dias_atraso"));
                    prestamos.add(prestamo);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar préstamos por libro: " + e.getMessage(), e);
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
            p.devuelto_tarde,
            p.dias_atraso,
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
                
                Docente docente = obtenerDocentePorId(conexion, idDocente);
                MotivoPrestamo motivoPrestamo = obtenerMotivoPorId(conexion, idMotivo);
                
                Prestamo prestamo = new Prestamo(
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
                );
                prestamo.setDevuelto_tarde(rs.getInt("devuelto_tarde"));
                prestamo.setDias_atraso(rs.getInt("dias_atraso"));
                prestamos.add(prestamo);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar préstamos activos: " + e.getMessage(), e);
        }

        return prestamos;
    }

    public int actualizarPrestamosTarde() {
        String query = """
            UPDATE prestamos 
            SET estado = 2 
            WHERE estado = 0 AND fecha_limite < ?
        """;
        
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setString(1, Fechas.fechaActualISO());
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar préstamos vencidos: " + e.getMessage(), e);
            return 0;
        }
    }

    public boolean actualizarEstado(Prestamo prestamo) {
        String query = "UPDATE prestamos SET estado = ? WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, 2);
            ps.setInt(2, prestamo.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el estado: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean registrarDevolucion(Prestamo prestamo) {
        String fechaDevolucion = Fechas.fechaActualISO();
        int diasAtraso = calcularDiasAtraso(prestamo.getFecha_limite(), fechaDevolucion);
        int devueltoTarde = diasAtraso > 0 ? 1 : 0;

        String query = """
                UPDATE prestamos
                SET estado = ?, fecha_devolucion = ?, devuelto_tarde = ?, dias_atraso = ?
                WHERE id = ?
            """;
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, 1);
            ps.setString(2, fechaDevolucion);
            ps.setInt(3, devueltoTarde);
            ps.setInt(4, diasAtraso);
            ps.setInt(5, prestamo.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar la devolución: " + e.getMessage(), e);
        }
        return false;
    }

    private int calcularDiasAtraso(String fechaLimite, String fechaDevolucion) {
        try {
            LocalDate limite = LocalDate.parse(fechaLimite);
            LocalDate devolucion = LocalDate.parse(fechaDevolucion);
            if (!devolucion.isAfter(limite)) {
                return 0;
            }
            return (int) ChronoUnit.DAYS.between(limite, devolucion);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean validarPrestamo(int idLibro, int idEstudiante) {
        String query = "SELECT 1 FROM prestamos WHERE id_estudiante = ? AND (estado = 0 OR estado = 2) AND id_libro = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al validar el préstamo: " + e.getMessage(), e);
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
                datos.put(rs.getString("genero"), rs.getInt("total_prestamos"));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener estadísticas por género: " + e.getMessage(), e);
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
                datos.put(rs.getString("nombre_categoria"), rs.getInt("total"));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener préstamos por categoría: " + e.getMessage(), e);
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
                    datos.put(rs.getString("docente"), rs.getInt("total"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener préstamos por docente: " + e.getMessage(), e);
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

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.put("Grado " + rs.getInt("grado"), rs.getInt("total"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener préstamos por grado: " + e.getMessage(), e);
        }

        return datos;
    }

    private Docente obtenerDocentePorId(Connection conexion, int idDocente) {
        String query = "SELECT id, nombre_1, nombre_2, apellido_1, apellido_2 FROM docentes WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, idDocente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Docente(
                            rs.getInt("id"),
                            rs.getString("nombre_1"),
                            rs.getString("nombre_2"),
                            rs.getString("apellido_1"),
                            rs.getString("apellido_2")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener docente por id: " + e.getMessage(), e);
        }
        return null;
    }

    private MotivoPrestamo obtenerMotivoPorId(Connection conexion, int idMotivo) {
        String query = "SELECT id, nombre_motivo FROM motivos_prestamo WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, idMotivo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MotivoPrestamo(
                            rs.getInt("id"),
                            rs.getString("nombre_motivo")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener motivo de préstamo por id: " + e.getMessage(), e);
        }
        return null;
    }

    // ==================== MÉTODOS CON FILTRO DE FECHAS ====================

    public Map<String, Integer> obtenerPrestamosPorGenero(String fechaInicio, String fechaFin) {
        String query = """
        SELECT 
            e.genero,
            COUNT(p.id) AS total_prestamos
        FROM prestamos p
        JOIN estudiantes e ON e.id = p.id_estudiante
        WHERE p.fecha_prestamo >= ? AND p.fecha_prestamo <= ?
        GROUP BY e.genero
        """;

        Map<String, Integer> datos = new HashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.put(rs.getString("genero"), rs.getInt("total_prestamos"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener estadísticas por género: " + e.getMessage(), e);
        }

        return datos;
    }

    public Map<String, Integer> obtenerPrestamosPorCategoria(String fechaInicio, String fechaFin) {
        String query = """
        SELECT c.nombre_categoria, COUNT(p.id) AS total
        FROM libros l
        JOIN categorias c ON c.id = l.id_categoria
        LEFT JOIN prestamos p ON l.id = p.id_libro AND p.fecha_prestamo >= ? AND p.fecha_prestamo <= ?
        GROUP BY c.nombre_categoria
        ORDER BY total DESC
        """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.put(rs.getString("nombre_categoria"), rs.getInt("total"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener préstamos por categoría con fechas: " + e.getMessage(), e);
        }

        return datos;
    }

    public Map<String, Integer> obtenerPrestamosPorDocenteTop(int limite, String fechaInicio, String fechaFin) {
        String query = """
        SELECT 
            d.id,
            d.nombre_1 || ' ' || d.nombre_2 || ' ' || d.apellido_1 || ' ' || d.apellido_2 AS docente,
            COUNT(p.id) AS total
        FROM prestamos p
        JOIN docentes d ON d.id = p.id_docente
        WHERE p.fecha_prestamo >= ? AND p.fecha_prestamo <= ?
        GROUP BY d.id, d.apellido_1, d.apellido_2, d.nombre_1, d.nombre_2
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
                    datos.put(rs.getString("docente"), rs.getInt("total"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener préstamos por docente con fechas: " + e.getMessage(), e);
        }

        return datos;
    }

    public Map<String, Integer> obtenerPrestamosPorGradoTop(int limite, String fechaInicio, String fechaFin) {
        String query = """
        SELECT e.grado, COUNT(p.id) AS total
        FROM prestamos p
        JOIN estudiantes e ON e.id = p.id_estudiante
        WHERE p.fecha_prestamo >= ? AND p.fecha_prestamo <= ?
        GROUP BY e.grado
        ORDER BY total DESC
        LIMIT ?
        """;

        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);
            ps.setInt(3, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.put("Grado " + rs.getInt("grado"), rs.getInt("total"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener préstamos por grado con fechas: " + e.getMessage(), e);
        }

        return datos;
    }
}
