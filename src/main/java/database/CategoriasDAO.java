package database;

import model.Categoria;
import utils.Alertas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CategoriasDAO {
    public ArrayList<Categoria> obtenerCategorias() {
        ArrayList<Categoria> lista = new ArrayList<>();
        String query = "SELECT id, nombre_categoria FROM categorias";
        try{
            Connection con = ConexionSQLite.conectar();
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre_categoria")));
            }
            return lista;
        }catch (SQLException e){
            Alertas.mostrarError("Error al obtener las categorias: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }
        return lista;
    }
}
