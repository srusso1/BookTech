package database;

import model.Prestamo;
import utils.Alertas;

import java.sql.*;
import java.util.*;

/**
 * DAO especializado para obtener datos de reportes
 * Contiene consultas específicas para la generación de informes
 */
public class InformesDAO {

    /**
     * Obtiene el historial de préstamos de un estudiante
     */
    public List<Prestamo> obtenerHistorialEstudiante(int idEstudiante) {
        String query = """
            SELECT 
                p.id,
                p.id_libro,
                p.id_docente,
                p.id_motivo,
                p.estado,
                p.fecha_prestamo,
                p.fecha_limite,
                p.fecha_devolucion,
                p.devuelto_tarde,
                p.dias_atraso,
                e.grado,
                l.titulo,
                e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante
            FROM prestamos p
            JOIN libros l ON l.id = p.id_libro
            JOIN estudiantes e ON e.id = p.id_estudiante
            WHERE p.id_estudiante = ?
            ORDER BY p.fecha_prestamo DESC
        """;

        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, idEstudiante);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idDocente = rs.getInt("id_docente");
                    int idMotivo = rs.getInt("id_motivo");

                    prestamos.add(mapearPrestamo(rs, idDocente, idMotivo));
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener historial del estudiante: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return prestamos;
    }

    /**
     * Obtiene el historial de préstamos de un estudiante con filtro de fecha
     */
    public List<Prestamo> obtenerHistorialEstudiante(int idEstudiante, String fechaInicio, String fechaFin) {
        String query = """
            SELECT 
                p.id,
                p.id_libro,
                p.id_docente,
                p.id_motivo,
                p.estado,
                p.fecha_prestamo,
                p.fecha_limite,
                p.fecha_devolucion,
                p.devuelto_tarde,
                p.dias_atraso,
                e.grado,
                l.titulo,
                e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante
            FROM prestamos p
            JOIN libros l ON l.id = p.id_libro
            JOIN estudiantes e ON e.id = p.id_estudiante
            WHERE p.id_estudiante = ? 
            AND DATE(p.fecha_prestamo) BETWEEN ? AND ?
            ORDER BY p.fecha_prestamo DESC
        """;

        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, idEstudiante);
            ps.setString(2, fechaInicio);
            ps.setString(3, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idDocente = rs.getInt("id_docente");
                    int idMotivo = rs.getInt("id_motivo");

                    prestamos.add(mapearPrestamo(rs, idDocente, idMotivo));
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener historial del estudiante: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return prestamos;
    }

    /**
     * Obtiene todos los préstamos registrados
     */
    public List<Prestamo> obtenerTodosPrestamos() {
        String query = """
            SELECT 
                p.id,
                p.id_libro,
                p.id_docente,
                p.id_motivo,
                p.id_estudiante,
                p.estado,
                p.fecha_prestamo,
                p.fecha_limite,
                p.fecha_devolucion,
                p.devuelto_tarde,
                p.dias_atraso,
                e.grado,
                l.titulo,
                e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante
            FROM prestamos p
            JOIN libros l ON l.id = p.id_libro
            JOIN estudiantes e ON e.id = p.id_estudiante
            ORDER BY p.fecha_prestamo DESC
        """;

        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idDocente = rs.getInt("id_docente");
                    int idMotivo = rs.getInt("id_motivo");

                    prestamos.add(mapearPrestamo(rs, idDocente, idMotivo));
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener todos los préstamos: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return prestamos;
    }

    /**
     * Obtiene todos los préstamos con filtro de fecha
     */
    public List<Prestamo> obtenerTodosPrestamos(String fechaInicio, String fechaFin) {
        String query = """
            SELECT 
                p.id,
                p.id_libro,
                p.id_docente,
                p.id_motivo,
                p.id_estudiante,
                p.estado,
                p.fecha_prestamo,
                p.fecha_limite,
                p.fecha_devolucion,
                p.devuelto_tarde,
                p.dias_atraso,
                e.grado,
                l.titulo,
                e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante
            FROM prestamos p
            JOIN libros l ON l.id = p.id_libro
            JOIN estudiantes e ON e.id = p.id_estudiante
            WHERE DATE(p.fecha_prestamo) BETWEEN ? AND ?
            ORDER BY p.fecha_prestamo DESC
        """;

        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, fechaInicio);
            ps.setString(2, fechaFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idDocente = rs.getInt("id_docente");
                    int idMotivo = rs.getInt("id_motivo");

                    prestamos.add(mapearPrestamo(rs, idDocente, idMotivo));
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener todos los préstamos: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return prestamos;
    }

    /**
     * Obtiene los IDs de todos los estudiantes con préstamos
     */
    public List<Integer> obtenerIDsEstudiantes() {
        String query = "SELECT DISTINCT id_estudiante FROM prestamos ORDER BY id_estudiante";
        List<Integer> estudiantes = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                estudiantes.add(rs.getInt("id_estudiante"));
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener IDs de estudiantes: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return estudiantes;
    }

    /**
     * Obtiene información estadística de un grado
     */
    public Map<String, Object> obtenerEstadisticasGrado(int grado) {
        Map<String, Object> estadisticas = new HashMap<>();

        try (Connection conexion = ConexionSQLite.conectar()) {

            // Total de préstamos en el grado
            String queryTotal = "SELECT COUNT(*) as total FROM prestamos p " +
                    "JOIN estudiantes e ON e.id = p.id_estudiante WHERE e.grado = ?";
            try (PreparedStatement ps = conexion.prepareStatement(queryTotal)) {
                ps.setInt(1, grado);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        estadisticas.put("totalPrestamos", rs.getInt("total"));
                    }
                }
            }

            // Total de estudiantes en el grado
            String queryEstudiantes = "SELECT COUNT(*) as total FROM estudiantes WHERE grado = ?";
            try (PreparedStatement ps = conexion.prepareStatement(queryEstudiantes)) {
                ps.setInt(1, grado);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        estadisticas.put("totalEstudiantes", rs.getInt("total"));
                    }
                }
            }

            // Libro más solicitado
            String queryLibroMas = "SELECT l.titulo, COUNT(*) as cantidad " +
                    "FROM prestamos p " +
                    "JOIN libros l ON l.id = p.id_libro " +
                    "JOIN estudiantes e ON e.id = p.id_estudiante " +
                    "WHERE e.grado = ? " +
                    "GROUP BY l.titulo " +
                    "ORDER BY cantidad DESC " +
                    "LIMIT 1";
            try (PreparedStatement ps = conexion.prepareStatement(queryLibroMas)) {
                ps.setInt(1, grado);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        estadisticas.put("libroMasSolicitado", rs.getString("titulo"));
                        estadisticas.put("cantidadLibroMas", rs.getInt("cantidad"));
                    }
                }
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener estadísticas del grado: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return estadisticas;
    }

    /**
     * Obtiene estudiantes de un grado con su cantidad de préstamos
     */
    public Map<String, Integer> obtenerEstudiantesPorGrado(int grado) {
        String query = """
            SELECT 
                e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante,
                COUNT(p.id) as cantidad
            FROM estudiantes e
            LEFT JOIN prestamos p ON e.id = p.id_estudiante
            WHERE e.grado = ?
            GROUP BY e.id
            ORDER BY cantidad DESC
        """;

        Map<String, Integer> estudiantes = new LinkedHashMap<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, grado);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    estudiantes.put(rs.getString("estudiante"), rs.getInt("cantidad"));
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener estudiantes del grado: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return estudiantes;
    }

    /**
     * Obtiene estadísticas generales de la biblioteca
     */
    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();

        try (Connection conexion = ConexionSQLite.conectar()) {

            // Total de préstamos
            String queryTotal = "SELECT COUNT(*) as total FROM prestamos";
            try (PreparedStatement ps = conexion.prepareStatement(queryTotal);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    estadisticas.put("totalPrestamos", rs.getInt("total"));
                }
            }

            // Total de devoluciones a tiempo
            String queryDevueltos = "SELECT COUNT(*) as total FROM prestamos WHERE estado = 1";
            try (PreparedStatement ps = conexion.prepareStatement(queryDevueltos);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    estadisticas.put("prestamosDevueltos", rs.getInt("total"));
                }
            }

            // Total de estudiantes con préstamos
            String queryEstudiantes = "SELECT COUNT(DISTINCT id_estudiante) as total FROM prestamos";
            try (PreparedStatement ps = conexion.prepareStatement(queryEstudiantes);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    estadisticas.put("estudiantesConPrestamos", rs.getInt("total"));
                }
            }

            // Libro más solicitado
            String queryLibroMas = """
                SELECT l.titulo, COUNT(*) as cantidad
                FROM prestamos p
                JOIN libros l ON l.id = p.id_libro
                GROUP BY l.titulo
                ORDER BY cantidad DESC
                LIMIT 1
            """;
            try (PreparedStatement ps = conexion.prepareStatement(queryLibroMas);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    estadisticas.put("libroMasSolicitado", rs.getString("titulo"));
                    estadisticas.put("cantidadLibroMas", rs.getInt("cantidad"));
                }
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener estadísticas generales: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return estadisticas;
    }

    public Map<String, Object> obtenerResumenGeneral(String fechaInicio, String fechaFin) {
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalPrestamos", 0);
        resumen.put("totalPrestamosTarde", 0);
        resumen.put("estudianteTop", "Sin datos");
        resumen.put("docenteTop", "Sin datos");
        resumen.put("libroTop", "Sin datos");

        boolean conFiltro = fechaInicio != null && fechaFin != null;

        try (Connection conexion = ConexionSQLite.conectar()) {
            String condicion = conFiltro ? " WHERE DATE(p.fecha_prestamo) BETWEEN ? AND ? " : " ";

            String qTotal = "SELECT COUNT(*) total FROM prestamos p" + condicion;
            try (PreparedStatement ps = conexion.prepareStatement(qTotal)) {
                if (conFiltro) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("totalPrestamos", rs.getInt("total"));
                    }
                }
            }

            String qTarde = "SELECT COUNT(*) total FROM prestamos p" + condicion + " AND p.devuelto_tarde = 1";
            if (!conFiltro) {
                qTarde = "SELECT COUNT(*) total FROM prestamos p WHERE p.devuelto_tarde = 1";
            }
            try (PreparedStatement ps = conexion.prepareStatement(qTarde)) {
                if (conFiltro) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("totalPrestamosTarde", rs.getInt("total"));
                    }
                }
            }

            String qEstudiante = """
                    SELECT e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante,
                           COUNT(p.id) total
                    FROM prestamos p
                    JOIN estudiantes e ON e.id = p.id_estudiante
                """ + condicion + " GROUP BY e.id ORDER BY total DESC LIMIT 1";
            try (PreparedStatement ps = conexion.prepareStatement(qEstudiante)) {
                if (conFiltro) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("estudianteTop", rs.getString("estudiante"));
                    }
                }
            }

            String qDocente = """
                    SELECT d.nombre_1 || ' ' || d.nombre_2 || ' ' || d.apellido_1 || ' ' || d.apellido_2 AS docente,
                           COUNT(DISTINCT p.id_estudiante) total_estudiantes
                    FROM prestamos p
                    JOIN docentes d ON d.id = p.id_docente
                """ + condicion + " GROUP BY d.id ORDER BY total_estudiantes DESC LIMIT 1";
            try (PreparedStatement ps = conexion.prepareStatement(qDocente)) {
                if (conFiltro) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("docenteTop", rs.getString("docente"));
                    }
                }
            }

            String qLibro = """
                    SELECT l.titulo, COUNT(p.id) total
                    FROM prestamos p
                    JOIN libros l ON l.id = p.id_libro
                """ + condicion + " GROUP BY l.id ORDER BY total DESC LIMIT 1";
            try (PreparedStatement ps = conexion.prepareStatement(qLibro)) {
                if (conFiltro) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("libroTop", rs.getString("titulo"));
                    }
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener resumen general: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return resumen;
    }

    public List<Prestamo> obtenerPrestamosDevueltosTarde() {
        return obtenerPrestamosDevueltosTarde(null, null, null);
    }

    public List<Prestamo> obtenerPrestamosDevueltosTarde(String fechaInicio, String fechaFin) {
        return obtenerPrestamosDevueltosTarde(fechaInicio, fechaFin, null);
    }

    public List<Prestamo> obtenerPrestamosDevueltosTarde(Integer grado) {
        return obtenerPrestamosDevueltosTarde(null, null, grado);
    }

    public List<Prestamo> obtenerPrestamosDevueltosTarde(String fechaInicio, String fechaFin, Integer grado) {
        StringBuilder query = new StringBuilder("""
            SELECT
                p.id,
                p.id_libro,
                p.id_docente,
                p.id_motivo,
                p.id_estudiante,
                p.estado,
                p.fecha_prestamo,
                p.fecha_limite,
                p.fecha_devolucion,
                p.devuelto_tarde,
                p.dias_atraso,
                e.grado,
                l.titulo,
                e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante
            FROM prestamos p
            JOIN libros l ON l.id = p.id_libro
            JOIN estudiantes e ON e.id = p.id_estudiante
            WHERE p.estado = 1 AND p.devuelto_tarde = 1
        """);

        List<Object> parametros = new ArrayList<>();

        if (fechaInicio != null && fechaFin != null) {
            query.append(" AND DATE(p.fecha_prestamo) BETWEEN ? AND ? ");
            parametros.add(fechaInicio);
            parametros.add(fechaFin);
        }

        if (grado != null) {
            query.append(" AND e.grado = ? ");
            parametros.add(grado);
        }

        query.append(" ORDER BY p.fecha_prestamo DESC ");

        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                Object parametro = parametros.get(i);
                if (parametro instanceof Integer valorInt) {
                    ps.setInt(i + 1, valorInt);
                } else {
                    ps.setString(i + 1, String.valueOf(parametro));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idDocente = rs.getInt("id_docente");
                    int idMotivo = rs.getInt("id_motivo");
                    prestamos.add(mapearPrestamo(rs, idDocente, idMotivo));
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener préstamos devueltos tarde: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return prestamos;
    }

    public Integer obtenerGradoConMasPrestamosDevueltosTarde(String fechaInicio, String fechaFin) {
        StringBuilder query = new StringBuilder("""
            SELECT e.grado, COUNT(p.id) total
            FROM prestamos p
            JOIN estudiantes e ON e.id = p.id_estudiante
            WHERE p.estado = 1 AND p.devuelto_tarde = 1
        """);

        boolean conFiltroFecha = fechaInicio != null && fechaFin != null;
        if (conFiltroFecha) {
            query.append(" AND DATE(p.fecha_prestamo) BETWEEN ? AND ? ");
        }

        query.append(" GROUP BY e.grado ORDER BY total DESC LIMIT 1 ");

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query.toString())) {

            if (conFiltroFecha) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("grado");
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener grado con más devoluciones tardías: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return null;
    }

    private Prestamo mapearPrestamo(ResultSet rs, int idDocente, int idMotivo) throws SQLException {
        Prestamo prestamo = new Prestamo(
                rs.getInt("id"),
                rs.getInt("id_libro"),
                rs.getString("estudiante"),
                rs.getString("fecha_prestamo"),
                rs.getString("fecha_limite"),
                rs.getInt("estado"),
                rs.getInt("grado"),
                rs.getString("titulo"),
                obtenerDocentePorId(idDocente),
                obtenerMotivoPorId(idMotivo)
        );
        prestamo.setFecha_devolucion(rs.getString("fecha_devolucion"));
        prestamo.setDevuelto_tarde(rs.getInt("devuelto_tarde"));
        prestamo.setDias_atraso(rs.getInt("dias_atraso"));
        return prestamo;
    }

    // Métodos auxiliares privados
    private model.Docente obtenerDocentePorId(int idDocente) {
        String query = "SELECT id, nombre_1, nombre_2, apellido_1, apellido_2 FROM docentes WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, idDocente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new model.Docente(
                            rs.getInt("id"),
                            rs.getString("nombre_1"),
                            rs.getString("nombre_2"),
                            rs.getString("apellido_1"),
                            rs.getString("apellido_2")
                    );
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener docente: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }
        return null;
    }

    private model.MotivoPrestamo obtenerMotivoPorId(int idMotivo) {
        String query = "SELECT id, nombre_motivo FROM motivos_prestamo WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setInt(1, idMotivo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new model.MotivoPrestamo(
                            rs.getInt("id"),
                            rs.getString("nombre_motivo")
                    );
                }
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener motivo: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }
        return null;
    }
}



