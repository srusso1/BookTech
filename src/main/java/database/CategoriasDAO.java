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

    public ArrayList<Categoria> obtenerCategoriasActivas() {
        return obtenerLista("SELECT id, nombre_categoria, estado FROM categorias WHERE estado = 1");
    }

    public ArrayList<Categoria> obtenerCategorias() {
        // Fallback or alias for existing code, returns only active
        return obtenerCategoriasActivas();
    }

    public ArrayList<Categoria> obtenerTodas() {
        return obtenerLista("SELECT id, nombre_categoria, estado FROM categorias");
    }

    private ArrayList<Categoria> obtenerLista(String query) {
        ArrayList<Categoria> lista = new ArrayList<>();
        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre_categoria"), rs.getInt("estado")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener las categorias: " + e.getMessage(), e);
        }
        return lista;
    }

    public boolean insertarCategoria(Categoria categoria) {
        String query = "INSERT INTO categorias (nombre_categoria, estado) VALUES (?, ?)";
        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, categoria.getNombreCategoria());
            ps.setInt(2, categoria.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar la categoria: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean actualizarCategoria(Categoria categoria) {
        String query = "UPDATE categorias SET nombre_categoria = ?, estado = ? WHERE id = ?";
        try (Connection con = ConexionSQLite.conectar();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, categoria.getNombreCategoria());
            ps.setInt(2, categoria.getEstado());
            ps.setInt(3, categoria.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar la categoria: " + e.getMessage(), e);
            return false;
        }
    }
}
