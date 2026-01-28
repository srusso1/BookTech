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

}
