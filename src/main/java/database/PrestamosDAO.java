package database;

import model.AlertaPrestamo;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrestamosDAO {

    private static final Logger LOGGER = Logger.getLogger(PrestamosDAO.class.getName());
    // ESTADO: 0 - Prestado, 1 - Devuelto, 2 - Pendiente

    public boolean registrarPrestamo(int idLibro, int id_estudiante, int id_motivo, int id_docente, String fechaPrestamo, String fechaLimite) {
        try (Connection conexion = ConexionSQLite.conectar()) {
            return registrarPrestamo(conexion, idLibro, id_estudiante, id_motivo, id_docente, fechaPrestamo, fechaLimite);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean registrarPrestamo(Connection conexion, int idLibro, int id_estudiante, int id_motivo, int id_docente, String fechaPrestamo, String fechaLimite) {
        String query = "INSERT INTO prestamos (id_libro, id_estudiante, id_motivo, id_docente, fecha_prestamo, fecha_limite, devuelto_tarde, dias_atraso, grado_historico) " +
                       "SELECT ?, ?, ?, ?, ?, ?, ?, ?, grado FROM estudiantes WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, idLibro);
            ps.setInt(2, id_estudiante);
            ps.setInt(3, id_motivo);
            ps.setInt(4, id_docente);
            ps.setString(5, fechaPrestamo);
            ps.setString(6, fechaLimite);
            ps.setInt(7, 0);
            ps.setInt(8, 0);
            ps.setInt(9, id_estudiante);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar el prÃ©stamo: " + e.getMessage(), e);
        }
        return false;
    }

    public List<Prestamo> buscarPrestamosLibro(int idLibro) {
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
            COALESCE(p.grado_historico, e.grado) AS grado,
            l.titulo,
            e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante,
            d.id AS doc_id,
            d.nombre_1 AS doc_nombre_1,
            d.nombre_2 AS doc_nombre_2,
            d.apellido_1 AS doc_apellido_1,
            d.apellido_2 AS doc_apellido_2,
            m.id AS motivo_id,
            m.nombre_motivo
        FROM prestamos p
        JOIN libros l ON l.id = p.id_libro
        JOIN estudiantes e ON e.id = p.id_estudiante
        LEFT JOIN docentes d ON d.id = p.id_docente
        LEFT JOIN motivos_prestamo m ON m.id = p.id_motivo
        WHERE p.id_libro = ? AND p.estado != """ + model.enums.EstadoPrestamo.DEVUELTO.getId();

        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapearPrestamoConRelaciones(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar prÃ©stamos por libro: " + e.getMessage(), e);
        }

        return prestamos;
    }

    public List<Prestamo> buscarPrestamosActivos() {
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
            COALESCE(p.grado_historico, e.grado) AS grado,
            l.titulo,
            e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante,
            d.id AS doc_id,
            d.nombre_1 AS doc_nombre_1,
            d.nombre_2 AS doc_nombre_2,
            d.apellido_1 AS doc_apellido_1,
            d.apellido_2 AS doc_apellido_2,
            m.id AS motivo_id,
            m.nombre_motivo
        FROM prestamos p
        JOIN libros l ON l.id = p.id_libro
        JOIN estudiantes e ON e.id = p.id_estudiante
        LEFT JOIN docentes d ON d.id = p.id_docente
        LEFT JOIN motivos_prestamo m ON m.id = p.id_motivo
        WHERE p.estado != """ + model.enums.EstadoPrestamo.DEVUELTO.getId();

        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prestamos.add(mapearPrestamoConRelaciones(rs));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar prÃ©stamos activos: " + e.getMessage(), e);
        }

        return prestamos;
    }

    private Prestamo mapearPrestamoConRelaciones(ResultSet rs) throws SQLException {
        Docente docente = null;
        int docId = rs.getInt("doc_id");
        if (docId > 0 && !rs.wasNull()) {
            docente = new Docente(
                    docId,
                    rs.getString("doc_nombre_1"),
                    rs.getString("doc_nombre_2"),
                    rs.getString("doc_apellido_1"),
                    rs.getString("doc_apellido_2")
            );
        }

        MotivoPrestamo motivoPrestamo = null;
        int motivoId = rs.getInt("motivo_id");
        if (motivoId > 0 && !rs.wasNull()) {
            motivoPrestamo = new MotivoPrestamo(
                    motivoId,
                    rs.getString("nombre_motivo")
            );
        }

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
        return prestamo;
    }

    public int actualizarPrestamosTarde() {
        String query = """
            UPDATE prestamos 
            SET estado = ? 
            WHERE estado = ? AND fecha_limite < ?
        """;
        
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, model.enums.EstadoPrestamo.VENCIDO.getId());
            ps.setInt(2, model.enums.EstadoPrestamo.PRESTADO.getId());
            ps.setString(3, Fechas.fechaActualISO());
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar prÃ©stamos vencidos: " + e.getMessage(), e);
            return 0;
        }
    }

    public boolean actualizarEstado(Prestamo prestamo) {
        String query = "UPDATE prestamos SET estado = ? WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, model.enums.EstadoPrestamo.VENCIDO.getId());
            ps.setInt(2, prestamo.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar el estado: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Obtiene alertas automÃ¡ticas de prÃ©stamos vencidos, que vencen hoy o prÃ³ximos a vencer en 2 dÃ­as.
     */
    public List<AlertaPrestamo> obtenerAlertasVencimiento() {
        String query = """
        SELECT 
            p.id,
            l.titulo,
            e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante,
            COALESCE(p.grado_historico, e.grado) AS grado,
            p.fecha_limite,
            p.estado
        FROM prestamos p
        JOIN libros l ON l.id = p.id_libro
        JOIN estudiantes e ON e.id = p.id_estudiante
        WHERE p.estado != 1
        ORDER BY p.fecha_limite ASC
        """;

        List<AlertaPrestamo> alertas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String titulo = rs.getString("titulo");
                String estudiante = rs.getString("estudiante");
                int grado = rs.getInt("grado");
                String fechaLimiteStr = rs.getString("fecha_limite");

                try {
                    LocalDate fechaLimite = LocalDate.parse(fechaLimiteStr);
                    long dias = ChronoUnit.DAYS.between(hoy, fechaLimite);

                    if (dias < 0) {
                        alertas.add(new AlertaPrestamo(id, titulo, estudiante, grado, fechaLimiteStr, AlertaPrestamo.TipoAlerta.VENCIDO, (int) dias));
                    } else if (dias == 0) {
                        alertas.add(new AlertaPrestamo(id, titulo, estudiante, grado, fechaLimiteStr, AlertaPrestamo.TipoAlerta.POR_VENCER_HOY, 0));
                    } else if (dias <= 2) {
                        alertas.add(new AlertaPrestamo(id, titulo, estudiante, grado, fechaLimiteStr, AlertaPrestamo.TipoAlerta.PROXIMO_A_VENCER, (int) dias));
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alertas de vencimiento: " + e.getMessage(), e);
        }

        return alertas;
    }

    public boolean registrarDevolucion(Prestamo prestamo) {
        try (Connection conexion = ConexionSQLite.conectar()) {
            return registrarDevolucion(conexion, prestamo);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean registrarDevolucion(Connection conexion, Prestamo prestamo) {
        String fechaDevolucion = Fechas.fechaActualISO();
        int diasAtraso = calcularDiasAtraso(prestamo.getFecha_limite(), fechaDevolucion);
        int devueltoTarde = diasAtraso > 0 ? 1 : 0;

        String query = """
                UPDATE prestamos
                SET estado = ?, fecha_devolucion = ?, devuelto_tarde = ?, dias_atraso = ?
                WHERE id = ?
            """;
        try (PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, model.enums.EstadoPrestamo.DEVUELTO.getId());
            ps.setString(2, fechaDevolucion);
            ps.setInt(3, devueltoTarde);
            ps.setInt(4, diasAtraso);
            ps.setInt(5, prestamo.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar la devoluciÃ³n: " + e.getMessage(), e);
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
        try (Connection conexion = ConexionSQLite.conectar()) {
            return validarPrestamo(conexion, idLibro, idEstudiante);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean validarPrestamo(Connection conexion, int idLibro, int idEstudiante) {
        String query = "SELECT 1 FROM prestamos WHERE id_estudiante = ? AND (estado = " + model.enums.EstadoPrestamo.PRESTADO.getId() + " OR estado = " + model.enums.EstadoPrestamo.VENCIDO.getId() + ") AND id_libro = ?";
        try (PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al validar el prÃ©stamo: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener estadÃ­sticas por gÃ©nero: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener prÃ©stamos por categorÃ­a: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener prÃ©stamos por docente: " + e.getMessage(), e);
        }

        return datos;
    }

    public Map<String, Integer> obtenerPrestamosPorGradoTop(int limite) {
        String query = """
        SELECT COALESCE(p.grado_historico, e.grado) AS grado, COUNT(p.id) AS total
        FROM prestamos p
        JOIN estudiantes e ON e.id = p.id_estudiante
        GROUP BY COALESCE(p.grado_historico, e.grado)
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
            LOGGER.log(Level.SEVERE, "Error al obtener prÃ©stamos por grado: " + e.getMessage(), e);
        }

        return datos;
    }

    // ==================== MÃ‰TODOS CON FILTRO DE FECHAS ====================

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
            LOGGER.log(Level.SEVERE, "Error al obtener estadÃ­sticas por gÃ©nero: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener prÃ©stamos por categorÃ­a con fechas: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener prÃ©stamos por docente con fechas: " + e.getMessage(), e);
        }

        return datos;
    }

    public Map<String, Integer> obtenerPrestamosPorGradoTop(int limite, String fechaInicio, String fechaFin) {
        String query = """
        SELECT COALESCE(p.grado_historico, e.grado) AS grado, COUNT(p.id) AS total
        FROM prestamos p
        JOIN estudiantes e ON e.id = p.id_estudiante
        WHERE p.fecha_prestamo >= ? AND p.fecha_prestamo <= ?
        GROUP BY COALESCE(p.grado_historico, e.grado)
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
            LOGGER.log(Level.SEVERE, "Error al obtener prÃ©stamos por grado con fechas: " + e.getMessage(), e);
        }

        return datos;
    }
}
