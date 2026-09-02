package database;

import model.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LibrosDAO {

    private static final Logger LOGGER = Logger.getLogger(LibrosDAO.class.getName());

    public List<Libro> buscarSimilares(String texto) {
        List<Libro> lista = new ArrayList<>();
        String sql = """
            SELECT l.id, l.titulo, l.ubicacion, l.id_categoria, l.id_editorial, l.autor, l.unidades,
                   c.nombre_categoria, e.nombre AS editorial_nombre
            FROM libros l
            JOIN categorias c ON c.id = l.id_categoria
            LEFT JOIN editoriales e ON e.id = l.id_editorial
            WHERE UPPER(l.titulo) LIKE ?
            ORDER BY l.titulo
            LIMIT 5
        """;

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + texto.toUpperCase() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapLibroConCategoria(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al buscar similitud de libros: " + e.getMessage(), e);
        }

        return lista;
    }

    public boolean disminuirUnidadLibro(int idLibro) {
        try (Connection conn = ConexionSQLite.conectar()) {
            return disminuirUnidadLibro(conn, idLibro);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean disminuirUnidadLibro(Connection conn, int idLibro) {
        String sql = "UPDATE libros SET unidades = unidades - 1 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al disminuir unidad de libro: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean aumentarUnidadLibro(int idLibro) {
        try (Connection conn = ConexionSQLite.conectar()) {
            return aumentarUnidadLibro(conn, idLibro);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean aumentarUnidadLibro(Connection conn, int idLibro) {
        String sql = "UPDATE libros SET unidades = unidades + 1 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al aumentar unidad de libro: " + e.getMessage(), e);
        }
        return false;
    }

    public List<Integer> infoDashboardBibliotecario() {
        List<Integer> info = new ArrayList<>();
        String query = """
            SELECT
                (SELECT COUNT(*) FROM libros) AS libros_registrados,
                (SELECT SUM(unidades) FROM libros) AS unidades_registradas,
                (SELECT COUNT(*) FROM prestamos WHERE estado != %d) AS prestamos_activos,
                (SELECT COUNT(*) FROM prestamos) AS prestamos_realizados
        """.formatted(model.enums.EstadoPrestamo.DEVUELTO.getId());
        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                info.add(rs.getInt("libros_registrados"));
                info.add(rs.getInt("unidades_registradas"));
                info.add(rs.getInt("prestamos_activos"));
                info.add(rs.getInt("prestamos_realizados"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener datos del dashboard bibliotecario: " + e.getMessage(), e);
        }
        return info;
    }

    public List<String> infoDashboardRectoria() {
        List<String> info = new ArrayList<>();
        String query = "SELECT (SELECT COUNT(*) FROM libros) AS libros_registrados, " +
                "(SELECT SUM(unidades) FROM libros) AS unidades_registradas, " +
                "(SELECT c.nombre_categoria FROM prestamos p JOIN libros l ON p.id_libro = l.id JOIN categorias c ON c.id = l.id_categoria GROUP BY c.nombre_categoria ORDER BY COUNT(p.id) DESC LIMIT 1) AS categoria_mas_prestada;";

        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                info.add(rs.getString("libros_registrados"));
                info.add(rs.getString("unidades_registradas"));
                String categoria = rs.getString("categoria_mas_prestada");
                info.add(categoria != null ? categoria : "SIN DATOS");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener datos del dashboard rectoria: " + e.getMessage(), e);
        }
        return info;
    }

    

    public List<Libro> obtenerLibrosPaginados(int limit, int offset, String busqueda) {
        List<Libro> libros = new ArrayList<>();
        String param = "%" + (busqueda == null ? "" : busqueda) + "%";
        String sql = """
                SELECT l.id, l.titulo, l.ubicacion, l.id_categoria, l.id_editorial, l.autor, l.unidades,
                       c.nombre_categoria, e.nombre AS editorial_nombre
                FROM libros l
                JOIN categorias c ON c.id = l.id_categoria
                LEFT JOIN editoriales e ON e.id = l.id_editorial
                WHERE l.titulo LIKE ? OR l.autor LIKE ? OR c.nombre_categoria LIKE ?
                   OR e.nombre LIKE ? OR l.ubicacion LIKE ?
                ORDER BY l.titulo
                LIMIT ? OFFSET ?
                """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            ps.setString(2, param);
            ps.setString(3, param);
            ps.setString(4, param);
            ps.setString(5, param);
            ps.setInt(6, limit);
            ps.setInt(7, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { libros.add(mapLibroConCategoria(rs)); }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener libros paginados", e);
        }
        return libros;
    }

    public int contarTotalLibros(String busqueda) {
        String param = "%" + (busqueda == null ? "" : busqueda) + "%";
        String sql = """
                SELECT COUNT(*) FROM libros l
                JOIN categorias c ON c.id = l.id_categoria
                LEFT JOIN editoriales e ON e.id = l.id_editorial
                WHERE l.titulo LIKE ? OR l.autor LIKE ? OR c.nombre_categoria LIKE ?
                   OR e.nombre LIKE ? OR l.ubicacion LIKE ?
                """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            ps.setString(2, param);
            ps.setString(3, param);
            ps.setString(4, param);
            ps.setString(5, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar libros", e);
        }
        return 0;
    }

    public List<Libro> inventarioLibros() {
        List<Libro> libros = new ArrayList<>();
        String query = """
                SELECT l.id, l.titulo, l.ubicacion, l.id_categoria, l.id_editorial, l.autor, l.unidades,
                       c.nombre_categoria, e.nombre AS editorial_nombre
                FROM libros l
                JOIN categorias c ON c.id = l.id_categoria
                LEFT JOIN editoriales e ON e.id = l.id_editorial
                """;
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                libros.add(mapLibroConCategoria(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener datos del inventario: " + e.getMessage(), e);
        }
        return libros;
    }

    public boolean editarLibro(Libro libro, String campo, String nuevoValor) {
        String columna;
        switch (campo.toLowerCase()) {
            case "titulo":
            case "autor":
            case "ubicacion":
            case "unidades":
                columna = campo.toLowerCase();
                break;
            case "editorial":
                columna = "id_editorial";
                break;
            case "categoria":
                columna = "id_categoria";
                break;
            default:
                LOGGER.warning("Campo no valido para edicion: " + campo);
                return false;
        }

        String query = "UPDATE libros SET " + columna + " = ? WHERE id = ?";

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            if ("id_categoria".equals(columna)) {
                int idCategoria;
                try {
                    idCategoria = Integer.parseInt(nuevoValor);
                } catch (NumberFormatException e) {
                    LOGGER.warning("Para editar categoria debes enviar el ID numerico de la categoria");
                    return false;
                }

                if (!existeCategoria(conexion, idCategoria)) {
                    LOGGER.warning("La categoria seleccionada no existe en la base de datos");
                    return false;
                }

                ps.setInt(1, idCategoria);
            } else if ("id_editorial".equals(columna)) {
                int idEditorial;
                try {
                    idEditorial = Integer.parseInt(nuevoValor);
                } catch (NumberFormatException e) {
                    LOGGER.warning("Para editar editorial debes enviar el ID numerico de la editorial");
                    return false;
                }
                ps.setInt(1, idEditorial);
            } else if ("unidades".equals(columna)) {
                int unidades;
                try {
                    unidades = Integer.parseInt(nuevoValor);
                } catch (NumberFormatException e) {
                    LOGGER.warning("Unidades debe ser un valor numerico");
                    return false;
                }

                if (unidades < 0) {
                    LOGGER.warning("Unidades no puede ser un valor negativo");
                    return false;
                }

                ps.setInt(1, unidades);
            } else {
                ps.setString(1, nuevoValor.toUpperCase());
            }
            ps.setInt(2, libro.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al editar el libro: " + e.getMessage(), e);
        }

        return false;
    }

    private Libro mapLibroConCategoria(ResultSet rs) throws SQLException {
        model.Editorial editorialObj = new model.Editorial(
            rs.getInt("id_editorial"),
            rs.getString("editorial_nombre"),
            1
        );
        return new Libro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("ubicacion"),
                rs.getInt("id_categoria"),
                rs.getString("nombre_categoria"),
                editorialObj,
                rs.getString("autor"),
                rs.getInt("unidades")
        );
    }

    private boolean existeCategoria(Connection conexion, int idCategoria) throws SQLException {
        String query = "SELECT 1 FROM categorias WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean registrarLibro(Libro libro) {
        String query = "INSERT INTO libros (titulo, ubicacion, id_categoria, id_editorial, autor, unidades) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getUbicacion());
            ps.setInt(3, libro.getId_categoria());
            ps.setInt(4, libro.getId_editorial());
            ps.setString(5, libro.getAutor());
            ps.setInt(6, libro.getUnidades());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar el libro: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean eliminarLibro(int idLibro) {
        String query = "DELETE FROM libros WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar el libro: " + e.getMessage(), e);
        }
        return false;
    }
}
