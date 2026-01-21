package database;

import model.Bibliotecario;
import model.Rector;
import model.Usuario;
import utils.Alertas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuariosDAO {

    public Usuario validarUsuario(String usuario, String password) {
        String query = "SELECT * FROM usuarios WHERE usuario = ? AND password = ?";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setString(1, usuario);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            Usuario user = null;
            if(rs.next()){
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String usuarioRegistrado = rs.getString("usuario");
                String passwordRegistrada = rs.getString("password");
                int rol = rs.getInt("rol");
                switch(rol){
                    case 1:
                        user = new Rector(nombre, apellido, usuarioRegistrado, password);
                        break;
                    case 0:
                        user = new Bibliotecario(nombre, apellido, usuarioRegistrado, passwordRegistrada);
                        break;
                }
                return user;
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            Alertas.mostrarError("EROR SQL: " + e.getMessage());
        }finally{
            ConexionSQLite.cerrarConexion();
        }

        return null;
    }
}
