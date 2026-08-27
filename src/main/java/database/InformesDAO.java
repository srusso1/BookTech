package database;

import model.Prestamo;
import model.InventarioLibroDetalle;
import model.RegistroPlataformaDetalle;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.*;
import java.util.*;

/**
 * DAO especializado para obtener datos de reportes
 * Contiene consultas específicas para la generación de informes
 */
public class InformesDAO {

    private static final Logger LOGGER = Logger.getLogger(InformesDAO.class.getName());

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
            LOGGER.log(Level.SEVERE, "Error al obtener historial del estudiante: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener historial del estudiante: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener todos los préstamos: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener todos los préstamos: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener IDs de estudiantes: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener estadísticas del grado: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener estudiantes del grado: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener estadísticas generales: " + e.getMessage(), e);
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
        resumen.put("top5Estudiantes", new ArrayList<String>());
        resumen.put("top5Docentes", new ArrayList<String>());
        resumen.put("top5Libros", new ArrayList<String>());

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

            String qTopEstudiantes = """
                    SELECT e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS nombre,
                           COUNT(p.id) total
                    FROM prestamos p
                    JOIN estudiantes e ON e.id = p.id_estudiante
                """ + condicion + " GROUP BY e.id ORDER BY total DESC LIMIT 5";
            List<String> topEstudiantes = new ArrayList<>();
            try (PreparedStatement ps = conexion.prepareStatement(qTopEstudiantes)) {
                if (conFiltro) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    int pos = 1;
                    while (rs.next()) {
                        topEstudiantes.add(pos + ". " + rs.getString("nombre") + " (" + rs.getInt("total") + ")");
                        pos++;
                    }
                }
            }
            if (topEstudiantes.isEmpty()) {
                topEstudiantes.add("Sin datos");
            }
            resumen.put("top5Estudiantes", topEstudiantes);

            String qTopDocentes = """
                    SELECT d.nombre_1 || ' ' || d.nombre_2 || ' ' || d.apellido_1 || ' ' || d.apellido_2 AS nombre,
                           COUNT(DISTINCT p.id_estudiante) total
                    FROM prestamos p
                    JOIN docentes d ON d.id = p.id_docente
                """ + condicion + " GROUP BY d.id ORDER BY total DESC LIMIT 5";
            List<String> topDocentes = new ArrayList<>();
            try (PreparedStatement ps = conexion.prepareStatement(qTopDocentes)) {
                if (conFiltro) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    int pos = 1;
                    while (rs.next()) {
                        topDocentes.add(pos + ". " + rs.getString("nombre") + " (" + rs.getInt("total") + ")");
                        pos++;
                    }
                }
            }
            if (topDocentes.isEmpty()) {
                topDocentes.add("Sin datos");
            }
            resumen.put("top5Docentes", topDocentes);

            String qTopLibros = """
                    SELECT l.titulo AS nombre, COUNT(p.id) total
                    FROM prestamos p
                    JOIN libros l ON l.id = p.id_libro
                """ + condicion + " GROUP BY l.id ORDER BY total DESC LIMIT 5";
            List<String> topLibros = new ArrayList<>();
            try (PreparedStatement ps = conexion.prepareStatement(qTopLibros)) {
                if (conFiltro) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    int pos = 1;
                    while (rs.next()) {
                        topLibros.add(pos + ". " + rs.getString("nombre") + " (" + rs.getInt("total") + ")");
                        pos++;
                    }
                }
            }
            if (topLibros.isEmpty()) {
                topLibros.add("Sin datos");
            }
            resumen.put("top5Libros", topLibros);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener resumen general: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener préstamos devueltos tarde: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener grado con más devoluciones tardías: " + e.getMessage(), e);
        } 

        return null;
    }

    public List<Map<String, Object>> obtenerTopEstudiantesDetalle(String fechaInicio, String fechaFin, int limite) {
        String query = """
            SELECT
                e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante,
                COUNT(p.id) AS total_prestamos,
                SUM(CASE WHEN p.devuelto_tarde = 1 THEN 1 ELSE 0 END) AS devoluciones_tarde,
                (
                    SELECT mp.nombre_motivo
                    FROM prestamos p2
                    JOIN motivos_prestamo mp ON mp.id = p2.id_motivo
                    WHERE p2.id_estudiante = p.id_estudiante
                      AND (? IS NULL OR DATE(p2.fecha_prestamo) BETWEEN ? AND ?)
                    GROUP BY p2.id_motivo
                    ORDER BY COUNT(*) DESC
                    LIMIT 1
                ) AS motivo_frecuente
            FROM prestamos p
            JOIN estudiantes e ON e.id = p.id_estudiante
            WHERE (? IS NULL OR DATE(p.fecha_prestamo) BETWEEN ? AND ?)
            GROUP BY p.id_estudiante
            ORDER BY total_prestamos DESC
            LIMIT ?
        """;

        List<Map<String, Object>> filas = new ArrayList<>();
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            int idx = 1;
            idx = setFiltroFecha(ps, idx, fechaInicio, fechaFin);
            idx = setFiltroFecha(ps, idx, fechaInicio, fechaFin);
            ps.setInt(idx, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("estudiante", rs.getString("estudiante"));
                    fila.put("total_prestamos", rs.getInt("total_prestamos"));
                    fila.put("devoluciones_tarde", rs.getInt("devoluciones_tarde"));
                    fila.put("motivo_frecuente", rs.getString("motivo_frecuente"));
                    filas.add(fila);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener top de estudiantes: " + e.getMessage(), e);
        } 
        return filas;
    }

    public List<Map<String, Object>> obtenerTopDocentesDetalle(String fechaInicio, String fechaFin, int limite) {
        String query = """
            SELECT
                d.nombre_1 || ' ' || d.nombre_2 || ' ' || d.apellido_1 || ' ' || d.apellido_2 AS docente,
                COUNT(DISTINCT p.id_estudiante) AS estudiantes_enviados,
                COUNT(p.id) AS total_solicitudes,
                (
                    SELECT mp.nombre_motivo
                    FROM prestamos p2
                    JOIN motivos_prestamo mp ON mp.id = p2.id_motivo
                    WHERE p2.id_docente = p.id_docente
                      AND (? IS NULL OR DATE(p2.fecha_prestamo) BETWEEN ? AND ?)
                    GROUP BY p2.id_motivo
                    ORDER BY COUNT(*) DESC
                    LIMIT 1
                ) AS motivo_frecuente
            FROM prestamos p
            JOIN docentes d ON d.id = p.id_docente
            WHERE (? IS NULL OR DATE(p.fecha_prestamo) BETWEEN ? AND ?)
            GROUP BY p.id_docente
            ORDER BY estudiantes_enviados DESC, total_solicitudes DESC
            LIMIT ?
        """;

        List<Map<String, Object>> filas = new ArrayList<>();
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            int idx = 1;
            idx = setFiltroFecha(ps, idx, fechaInicio, fechaFin);
            idx = setFiltroFecha(ps, idx, fechaInicio, fechaFin);
            ps.setInt(idx, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("docente", rs.getString("docente"));
                    fila.put("estudiantes_enviados", rs.getInt("estudiantes_enviados"));
                    fila.put("total_solicitudes", rs.getInt("total_solicitudes"));
                    fila.put("motivo_frecuente", rs.getString("motivo_frecuente"));
                    filas.add(fila);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener top de docentes: " + e.getMessage(), e);
        } 
        return filas;
    }

    public List<Map<String, Object>> obtenerTopLibrosDetalle(String fechaInicio, String fechaFin, int limite) {
        String query = """
            SELECT
                l.titulo AS libro,
                COUNT(p.id) AS total_solicitudes,
                COUNT(DISTINCT p.id_estudiante) AS estudiantes_unicos,
                (
                    SELECT e2.grado
                    FROM prestamos p2
                    JOIN estudiantes e2 ON e2.id = p2.id_estudiante
                    WHERE p2.id_libro = p.id_libro
                      AND (? IS NULL OR DATE(p2.fecha_prestamo) BETWEEN ? AND ?)
                    GROUP BY e2.grado
                    ORDER BY COUNT(*) DESC
                    LIMIT 1
                ) AS grado_frecuente
            FROM prestamos p
            JOIN libros l ON l.id = p.id_libro
            WHERE (? IS NULL OR DATE(p.fecha_prestamo) BETWEEN ? AND ?)
            GROUP BY p.id_libro
            ORDER BY total_solicitudes DESC
            LIMIT ?
        """;

        List<Map<String, Object>> filas = new ArrayList<>();
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            int idx = 1;
            idx = setFiltroFecha(ps, idx, fechaInicio, fechaFin);
            idx = setFiltroFecha(ps, idx, fechaInicio, fechaFin);
            ps.setInt(idx, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    fila.put("libro", rs.getString("libro"));
                    fila.put("total_solicitudes", rs.getInt("total_solicitudes"));
                    fila.put("estudiantes_unicos", rs.getInt("estudiantes_unicos"));
                    fila.put("grado_frecuente", rs.getString("grado_frecuente"));
                    filas.add(fila);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener top de libros: " + e.getMessage(), e);
        } 
        return filas;
    }

    public List<RegistroPlataformaDetalle> obtenerRegistrosPlataforma(String fechaInicio, String fechaFin) {
        StringBuilder query = new StringBuilder("""
            SELECT
                r.id,
                r.id_docente,
                TRIM(
                    COALESCE(d.nombre_1, '') || ' ' ||
                    COALESCE(d.nombre_2, '') || ' ' ||
                    COALESCE(d.apellido_1, '') || ' ' ||
                    COALESCE(d.apellido_2, '')
                ) AS docente,
                COALESCE(mp.nombre_motivo, 'Sin motivo') AS motivo_uso,
                r.fecha,
                r.hora_inicio,
                r.hora_fin,
                COALESCE(r.total_minutos, 0) AS total_minutos,
                COALESCE(r.grado, 0) AS grado
            FROM registro_plataforma r
            JOIN docentes d ON d.id = r.id_docente
            LEFT JOIN motivos_plataforma mp ON mp.id = r.id_motivo_uso
            WHERE 1=1
        """);

        List<Object> parametros = new ArrayList<>();
        if (fechaInicio != null && fechaFin != null) {
            query.append(" AND DATE(r.fecha) BETWEEN ? AND ? ");
            parametros.add(fechaInicio);
            parametros.add(fechaFin);
        }
        query.append(" ORDER BY DATE(r.fecha) DESC, r.id DESC ");

        List<RegistroPlataformaDetalle> registros = new ArrayList<>();
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapearRegistroPlataforma(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener registros de plataforma: " + e.getMessage(), e);
        } 

        return registros;
    }

    public List<RegistroPlataformaDetalle> obtenerRegistrosPlataformaPorDocente(int idDocente, String fechaInicio, String fechaFin) {
        StringBuilder query = new StringBuilder("""
            SELECT
                r.id,
                r.id_docente,
                TRIM(
                    COALESCE(d.nombre_1, '') || ' ' ||
                    COALESCE(d.nombre_2, '') || ' ' ||
                    COALESCE(d.apellido_1, '') || ' ' ||
                    COALESCE(d.apellido_2, '')
                ) AS docente,
                COALESCE(mp.nombre_motivo, 'Sin motivo') AS motivo_uso,
                r.fecha,
                r.hora_inicio,
                r.hora_fin,
                COALESCE(r.total_minutos, 0) AS total_minutos,
                COALESCE(r.grado, 0) AS grado
            FROM registro_plataforma r
            JOIN docentes d ON d.id = r.id_docente
            LEFT JOIN motivos_plataforma mp ON mp.id = r.id_motivo_uso
            WHERE r.id_docente = ?
        """);

        List<Object> parametros = new ArrayList<>();
        parametros.add(idDocente);

        if (fechaInicio != null && fechaFin != null) {
            query.append(" AND DATE(r.fecha) BETWEEN ? AND ? ");
            parametros.add(fechaInicio);
            parametros.add(fechaFin);
        }

        query.append(" ORDER BY DATE(r.fecha) DESC, r.id DESC ");

        List<RegistroPlataformaDetalle> registros = new ArrayList<>();
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapearRegistroPlataforma(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener registros de plataforma por docente: " + e.getMessage(), e);
        } 

        return registros;
    }

    public Map<String, Object> obtenerResumenPlataformaGeneral(String fechaInicio, String fechaFin) {
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalRegistros", 0);
        resumen.put("totalMinutos", 0);
        resumen.put("motivoTop", "Sin datos");
        resumen.put("docenteTop", "Sin datos");
        resumen.put("gradoTop", "Sin datos");

        String condicionFecha = (fechaInicio != null && fechaFin != null) ? " AND DATE(r.fecha) BETWEEN ? AND ? " : " ";

        try (Connection conexion = ConexionSQLite.conectar()) {
            String qTotal = "SELECT COUNT(*) AS total, COALESCE(SUM(r.total_minutos), 0) AS minutos FROM registro_plataforma r WHERE 1=1 " + condicionFecha;
            try (PreparedStatement ps = conexion.prepareStatement(qTotal)) {
                if (fechaInicio != null && fechaFin != null) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("totalRegistros", rs.getInt("total"));
                        resumen.put("totalMinutos", rs.getInt("minutos"));
                    }
                }
            }

            String qMotivo = """
                SELECT COALESCE(mp.nombre_motivo, 'Sin motivo') AS motivo, COUNT(*) AS total
                FROM registro_plataforma r
                LEFT JOIN motivos_plataforma mp ON mp.id = r.id_motivo_uso
                WHERE 1=1
            """ + condicionFecha + " GROUP BY r.id_motivo_uso ORDER BY total DESC LIMIT 1";
            try (PreparedStatement ps = conexion.prepareStatement(qMotivo)) {
                if (fechaInicio != null && fechaFin != null) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("motivoTop", rs.getString("motivo"));
                    }
                }
            }

            String qDocente = """
                SELECT TRIM(
                    COALESCE(d.nombre_1, '') || ' ' ||
                    COALESCE(d.nombre_2, '') || ' ' ||
                    COALESCE(d.apellido_1, '') || ' ' ||
                    COALESCE(d.apellido_2, '')
                ) AS docente,
                COALESCE(SUM(r.total_minutos), 0) AS minutos
                FROM registro_plataforma r
                JOIN docentes d ON d.id = r.id_docente
                WHERE 1=1
            """ + condicionFecha + " GROUP BY r.id_docente ORDER BY minutos DESC LIMIT 1";
            try (PreparedStatement ps = conexion.prepareStatement(qDocente)) {
                if (fechaInicio != null && fechaFin != null) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("docenteTop", rs.getString("docente"));
                    }
                }
            }

            String qGrado = """
                SELECT r.grado, COUNT(*) AS total
                FROM registro_plataforma r
                WHERE COALESCE(r.grado, 0) > 0
            """ + condicionFecha + " GROUP BY r.grado ORDER BY total DESC LIMIT 1";
            try (PreparedStatement ps = conexion.prepareStatement(qGrado)) {
                if (fechaInicio != null && fechaFin != null) {
                    ps.setString(1, fechaInicio);
                    ps.setString(2, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("gradoTop", String.valueOf(rs.getInt("grado")));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener resumen de plataforma: " + e.getMessage(), e);
        } 

        return resumen;
    }

    public Map<String, Object> obtenerResumenPlataformaDocente(int idDocente, String fechaInicio, String fechaFin) {
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalRegistros", 0);
        resumen.put("totalMinutos", 0);
        resumen.put("motivoTop", "Sin datos");
        resumen.put("gradoTop", "Sin datos");

        String condicionFecha = (fechaInicio != null && fechaFin != null) ? " AND DATE(r.fecha) BETWEEN ? AND ? " : " ";

        try (Connection conexion = ConexionSQLite.conectar()) {
            String qTotal = "SELECT COUNT(*) AS total, COALESCE(SUM(r.total_minutos), 0) AS minutos FROM registro_plataforma r WHERE r.id_docente = ? " + condicionFecha;
            try (PreparedStatement ps = conexion.prepareStatement(qTotal)) {
                ps.setInt(1, idDocente);
                if (fechaInicio != null && fechaFin != null) {
                    ps.setString(2, fechaInicio);
                    ps.setString(3, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("totalRegistros", rs.getInt("total"));
                        resumen.put("totalMinutos", rs.getInt("minutos"));
                    }
                }
            }

            String qMotivo = """
                SELECT COALESCE(mp.nombre_motivo, 'Sin motivo') AS motivo, COUNT(*) AS total
                FROM registro_plataforma r
                LEFT JOIN motivos_plataforma mp ON mp.id = r.id_motivo_uso
                WHERE r.id_docente = ?
            """ + condicionFecha + " GROUP BY r.id_motivo_uso ORDER BY total DESC LIMIT 1";
            try (PreparedStatement ps = conexion.prepareStatement(qMotivo)) {
                ps.setInt(1, idDocente);
                if (fechaInicio != null && fechaFin != null) {
                    ps.setString(2, fechaInicio);
                    ps.setString(3, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("motivoTop", rs.getString("motivo"));
                    }
                }
            }

            String qGrado = """
                SELECT r.grado, COUNT(*) AS total
                FROM registro_plataforma r
                WHERE r.id_docente = ? AND COALESCE(r.grado, 0) > 0
            """ + condicionFecha + " GROUP BY r.grado ORDER BY total DESC LIMIT 1";
            try (PreparedStatement ps = conexion.prepareStatement(qGrado)) {
                ps.setInt(1, idDocente);
                if (fechaInicio != null && fechaFin != null) {
                    ps.setString(2, fechaInicio);
                    ps.setString(3, fechaFin);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resumen.put("gradoTop", String.valueOf(rs.getInt("grado")));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener resumen de plataforma por docente: " + e.getMessage(), e);
        } 

        return resumen;
    }

    public List<InventarioLibroDetalle> obtenerInventarioParaCompra(int umbralStock) {
        String query = """
            SELECT
                l.id,
                l.titulo,
                COALESCE(c.nombre_categoria, 'Sin categoria') AS categoria,
                COALESCE(l.autor, 'N/A') AS autor,
                COALESCE(l.editorial, 'N/A') AS editorial,
                COALESCE(l.ubicacion, 'N/A') AS ubicacion,
                COALESCE(l.unidades, 0) AS unidades,
                COALESCE(pa.prestamos_activos, 0) AS prestamos_activos,
                COALESCE(ph.prestamos_historicos, 0) AS prestamos_historicos
            FROM libros l
            LEFT JOIN categorias c ON c.id = l.id_categoria
            LEFT JOIN (
                SELECT p.id_libro, COUNT(*) AS prestamos_activos
                FROM prestamos p
                WHERE p.estado IN (0, 2)
                GROUP BY p.id_libro
            ) pa ON pa.id_libro = l.id
            LEFT JOIN (
                SELECT p.id_libro, COUNT(*) AS prestamos_historicos
                FROM prestamos p
                GROUP BY p.id_libro
            ) ph ON ph.id_libro = l.id
            ORDER BY l.titulo ASC
        """;

        List<InventarioLibroDetalle> inventario = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            int umbralSeguro = Math.max(1, umbralStock);
            while (rs.next()) {
                int unidades = rs.getInt("unidades");
                int prestamosActivos = rs.getInt("prestamos_activos");
                int stockObjetivo = umbralSeguro + prestamosActivos;
                int recomendadasComprar = Math.max(0, stockObjetivo - unidades);

                inventario.add(new InventarioLibroDetalle(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("categoria"),
                        rs.getString("autor"),
                        rs.getString("editorial"),
                        rs.getString("ubicacion"),
                        unidades,
                        prestamosActivos,
                        rs.getInt("prestamos_historicos"),
                        stockObjetivo,
                        recomendadasComprar
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener inventario para compra: " + e.getMessage(), e);
        } 

        inventario.sort((a, b) -> {
            int porRecomendadas = Integer.compare(b.getRecomendadasComprar(), a.getRecomendadasComprar());
            if (porRecomendadas != 0) {
                return porRecomendadas;
            }
            int porUnidades = Integer.compare(a.getUnidades(), b.getUnidades());
            if (porUnidades != 0) {
                return porUnidades;
            }
            return b.getPrestamosHistoricos() - a.getPrestamosHistoricos();
        });

        return inventario;
    }

    public Map<String, Object> obtenerResumenInventario(int umbralStock) {
        Map<String, Object> resumen = new HashMap<>();
        List<InventarioLibroDetalle> inventario = obtenerInventarioParaCompra(umbralStock);

        int totalTitulos = inventario.size();
        int totalUnidades = inventario.stream().mapToInt(InventarioLibroDetalle::getUnidades).sum();
        int totalPrestamosActivos = inventario.stream().mapToInt(InventarioLibroDetalle::getPrestamosActivos).sum();
        int totalSugeridasCompra = inventario.stream().mapToInt(InventarioLibroDetalle::getRecomendadasComprar).sum();
        long titulosConRiesgo = inventario.stream().filter(i -> i.getRecomendadasComprar() > 0).count();

        resumen.put("totalTitulos", totalTitulos);
        resumen.put("totalUnidades", totalUnidades);
        resumen.put("totalPrestamosActivos", totalPrestamosActivos);
        resumen.put("totalSugeridasCompra", totalSugeridasCompra);
        resumen.put("titulosConRiesgo", titulosConRiesgo);
        resumen.put("libroMasCritico", inventario.isEmpty() ? "Sin datos" : inventario.get(0).getTitulo());

        return resumen;
    }

    private int setFiltroFecha(PreparedStatement ps, int start, String fechaInicio, String fechaFin) throws SQLException {
        ps.setString(start, fechaInicio);
        ps.setString(start + 1, fechaInicio);
        ps.setString(start + 2, fechaFin);
        return start + 3;
    }

    private RegistroPlataformaDetalle mapearRegistroPlataforma(ResultSet rs) throws SQLException {
        return new RegistroPlataformaDetalle(
                rs.getInt("id"),
                rs.getInt("id_docente"),
                rs.getString("docente"),
                rs.getString("motivo_uso"),
                rs.getString("fecha"),
                rs.getString("hora_inicio"),
                rs.getString("hora_fin"),
                rs.getInt("total_minutos"),
                rs.getInt("grado")
        );
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
            LOGGER.log(Level.SEVERE, "Error al obtener docente: " + e.getMessage(), e);
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
            LOGGER.log(Level.SEVERE, "Error al obtener motivo: " + e.getMessage(), e);
        } 
        return null;
    }
}




