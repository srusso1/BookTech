package database;

import model.Libro;
import utils.Alertas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibrosDAO {

    public List<Libro> buscarSimilares(String texto) {

        List<Libro> lista = new ArrayList<>();

        String sql = """
        SELECT l.id, l.titulo, l.ubicacion, l.id_categoria, l.editorial, l.autor, l.unidades,
               c.nombre_categoria
        FROM libros l
        JOIN categorias c ON c.id = l.id_categoria
        WHERE UPPER(l.titulo) LIKE ?
        ORDER BY l.titulo
        LIMIT 5
    """;

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + texto.toUpperCase() + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapLibroConCategoria(rs));
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error SQL al buscar similitud de libros: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }

        return lista;
    }

    public boolean disminuirUnidadLibro(int idLibro){
        String sql = "UPDATE libros SET unidades = unidades - 1 WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Alertas.mostrarError("Error SQL al disminuir unidad de libro: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public boolean aumentarUnidadLibro(int idLibro){
        String sql = "UPDATE libros SET unidades = unidades + 1 WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Alertas.mostrarError("Error SQL al aumentar unidad de libro: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public ArrayList<Integer> infoDashboardBibliotecario(){
        ArrayList<Integer> info = new ArrayList<>();
        String query = """
            SELECT
                (SELECT COUNT(*) FROM libros) AS libros_registrados,
                (SELECT SUM(unidades) FROM libros) AS unidades_registradas,
                (SELECT COUNT(*) FROM prestamos WHERE estado = 0) AS prestamos_activos,
                (SELECT COUNT(*) FROM prestamos) AS prestamos_realizados
        """;
        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()){

            if (rs.next()) {
                info.add(rs.getInt("libros_registrados"));
                info.add(rs.getInt("unidades_registradas"));
                info.add(rs.getInt("prestamos_activos"));
                info.add(rs.getInt("prestamos_realizados"));

            }
        }catch (SQLException e){
            Alertas.mostrarError("Error al obtener datos del dashboard: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return info;
    }

    public ArrayList<String> infoDashboardRectoria(){
        ArrayList<String> info = new ArrayList<>();
        String query = "SELECT (SELECT COUNT(*) FROM libros) AS libros_registrados, " +
                "(SELECT SUM(unidades) FROM libros) AS unidades_registradas, " +
                "(SELECT c.nombre_categoria FROM prestamos p JOIN libros l ON p.id_libro = l.id JOIN categorias c ON c.id = l.id_categoria GROUP BY c.nombre_categoria ORDER BY COUNT(p.id) DESC LIMIT 1) AS categoria_mas_prestada;";

        try(Connection con = ConexionSQLite.conectar();
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery()){
            if(rs.next()){
                info.add(rs.getString("libros_registrados"));
                info.add(rs.getString("unidades_registradas"));
                String categoria = rs.getString("categoria_mas_prestada");
                info.add(categoria != null ? categoria : "SIN DATOS");
            }
        }catch (SQLException e){
            Alertas.mostrarError("Error al obtener datos del dashboard: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return info;
    }

    public ArrayList<Libro> inventarioLibros(){
        ArrayList<Libro> libros = new ArrayList<>();
        String query = """
                SELECT l.id, l.titulo, l.ubicacion, l.id_categoria, l.editorial, l.autor, l.unidades,
                       c.nombre_categoria
                FROM libros l
                JOIN categorias c ON c.id = l.id_categoria
                """;
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                libros.add(mapLibroConCategoria(rs));
            }
            return libros;
        }catch (SQLException e){
            Alertas.mostrarError("Error al obtener datos del inventario: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return libros;
    }

    public boolean editarLibro(Libro libro, String campo, String nuevoValor) {
        String columna;
        switch (campo.toLowerCase()) {
            case "titulo":
            case "autor":
            case "editorial":
            case "ubicacion":
            case "unidades":
                columna = campo.toLowerCase();
                break;
            case "categoria":
                columna = "id_categoria";
                break;
            default:
                Alertas.mostrarError("Campo no valido para edicion: " + campo);
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
                    Alertas.mostrarError("Para editar categoria debes enviar el ID numerico de la categoria");
                    return false;
                }

                if (!existeCategoria(conexion, idCategoria)) {
                    Alertas.mostrarError("La categoria seleccionada no existe en la base de datos");
                    return false;
                }

                ps.setInt(1, idCategoria);
            } else if ("unidades".equals(columna)) {
                int unidades;
                try {
                    unidades = Integer.parseInt(nuevoValor);
                } catch (NumberFormatException e) {
                    Alertas.mostrarError("Unidades debe ser un valor numerico");
                    return false;
                }

                if (unidades < 0) {
                    Alertas.mostrarError("Unidades no puede ser un valor negativo");
                    return false;
                }

                ps.setInt(1, unidades);
            } else {
                ps.setString(1, nuevoValor.toUpperCase());
            }
            ps.setInt(2, libro.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            Alertas.mostrarError("Error al editar el libro: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }

        return false;
    }

    private Libro mapLibroConCategoria(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("ubicacion"),
                rs.getInt("id_categoria"),
                rs.getString("nombre_categoria"),
                rs.getString("editorial"),
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
        String query = "INSERT INTO libros (titulo, ubicacion, id_categoria, editorial, autor, unidades) VALUES (?, ?, ?, ?, ?, ?)";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getUbicacion());
            ps.setInt(3, libro.getId_categoria());
            ps.setString(4, libro.getEditorial());
            ps.setString(5, libro.getAutor());
            ps.setInt(6, libro.getUnidades());

            return ps.executeUpdate() > 0;
        }catch (SQLException e){
            Alertas.mostrarError("Error al registrar el libro: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public boolean eliminarLibro(int idLibro){
        String query = "DELETE FROM libros WHERE id = ?";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, idLibro);
            return ps.executeUpdate() > 0;
        }catch (SQLException e){
            Alertas.mostrarError("Error al eliminar el libro: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

}
