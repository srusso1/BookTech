package database;

import model.Libro;
import utils.Alertas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LibrosDAO {

    public List<Libro> buscarSimilares(String texto) {

        List<Libro> lista = new ArrayList<>();

        String sql = """
        SELECT *
        FROM libros
        WHERE UPPER(titulo) LIKE ?
        ORDER BY titulo
        LIMIT 5
    """;

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + texto.toUpperCase() + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Libro(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("ubicacion"),
                        rs.getString("categoria"),
                        rs.getString("editorial"),
                        rs.getString("autor"),
                        rs.getInt("unidades")
                ));
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

    public ArrayList<Libro> inventarioLibros(){
        ArrayList<Libro> libros = new ArrayList<>();
        String query = "SELECT * FROM libros";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String titulo = rs.getString("titulo");
                String ubicacion = rs.getString("ubicacion");
                String categoria = rs.getString("categoria");
                String editorial = rs.getString("editorial");
                String autor = rs.getString("autor");
                int unidades = rs.getInt("unidades");
                int id = rs.getInt("id");
                libros.add(new Libro(id, titulo, ubicacion, categoria, editorial, autor, unidades));
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
        String query = "UPDATE libros SET " + campo.toLowerCase() + " = ? WHERE id = ?";

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, nuevoValor.toUpperCase());
            ps.setInt(2, libro.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            Alertas.mostrarError("Error al editar el libro: " + e.getMessage());
        }

        return false;
    }

    public boolean registrarLibro(Libro libro) {
        String query = "INSERT INTO libros (titulo, ubicacion, categoria, editorial, autor, unidades) VALUES (?, ?, ?, ?, ?, ?)";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getUbicacion());
            ps.setString(3, libro.getCategoria());
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

}
