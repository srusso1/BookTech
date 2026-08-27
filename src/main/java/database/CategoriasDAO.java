package database;

import model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoriasDAO {

    private static final Logger LOGGER = Logger.getLogger(CategoriasDAO.class.getName());

    public ArrayList<Categoria> obtenerCategorias() {
        ArrayList<Categoria> lista = new ArrayList<>();
        String query = "SELECT id, nombre_categoria FROM categorias";
        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre_categoria")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener las categorías: " + e.getMessage(), e);
        }
        return lista;
    }
}
