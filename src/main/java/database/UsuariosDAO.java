package database;

import model.Bibliotecario;
import model.Rector;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuariosDAO {

    private static final Logger LOGGER = Logger.getLogger(UsuariosDAO.class.getName());

    public Usuario validarUsuario(String usuario, String password) {
        String query = "SELECT * FROM usuarios WHERE usuario = ? AND password = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, usuario);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String apellido = rs.getString("apellido");
                    String usuarioRegistrado = rs.getString("usuario");
                    String passwordRegistrada = rs.getString("password");
                    int rol = rs.getInt("rol");

                    switch (rol) {
                        case 1:
                            return new Rector(nombre, apellido, usuarioRegistrado, password);
                        case 0:
                            return new Bibliotecario(nombre, apellido, usuarioRegistrado, passwordRegistrada);
                        default:
                            return null;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al validar usuario: " + e.getMessage(), e);
        }

        return null;
    }
}
