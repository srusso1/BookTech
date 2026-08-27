package database;

import model.Bibliotecario;
import model.Rector;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para gestión de usuarios y autenticación segura con BCrypt.
 */
public class UsuariosDAO {

    private static final Logger LOGGER = Logger.getLogger(UsuariosDAO.class.getName());

    /**
     * Valida credenciales de usuario con soporte de hashing BCrypt y migración automática transparente.
     * @param usuario Nombre de usuario
     * @param password Contraseña ingresada
     * @return Usuario autenticado o null si las credenciales son inválidas
     */
    public Usuario validarUsuario(String usuario, String password) {
        String query = "SELECT id, nombre, apellido, usuario, password, rol FROM usuarios WHERE usuario = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String nombre = rs.getString("nombre");
                    String apellido = rs.getString("apellido");
                    String usuarioRegistrado = rs.getString("usuario");
                    String passwordBD = rs.getString("password");
                    int rol = rs.getInt("rol");

                    boolean passwordValida = false;
                    boolean requiereMigracionHash = false;

                    if (passwordBD != null && (passwordBD.startsWith("$2a$") || passwordBD.startsWith("$2b$") || passwordBD.startsWith("$2y$"))) {
                        // Formato BCrypt moderno
                        try {
                            passwordValida = BCrypt.checkpw(password, passwordBD);
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error al verificar hash BCrypt: " + e.getMessage());
                            passwordValida = false;
                        }
                    } else {
                        // Contraseña legacy en texto plano
                        passwordValida = password.equals(passwordBD);
                        requiereMigracionHash = passwordValida;
                    }

                    if (!passwordValida) {
                        return null;
                    }

                    // Si la contraseña estaba en texto plano, la migramos automáticamente a BCrypt
                    if (requiereMigracionHash) {
                        String nuevoHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
                        actualizarHash(conexion, id, nuevoHash);
                        LOGGER.info("Contraseña de usuario '" + usuarioRegistrado + "' migrada exitosamente a hash BCrypt.");
                    }

                    return switch (rol) {
                        case 1 -> new Rector(nombre, apellido, usuarioRegistrado, "[PROTECTED]");
                        case 0 -> new Bibliotecario(nombre, apellido, usuarioRegistrado, "[PROTECTED]");
                        default -> null;
                    };
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error SQL al validar usuario: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Actualiza la contraseña de un usuario hasheándola con BCrypt.
     */
    public boolean actualizarPassword(int idUsuario, String nuevaPassword) {
        String query = "UPDATE usuarios SET password = ? WHERE id = ?";
        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

            String hash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt(12));
            ps.setString(1, hash);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar contraseña: " + e.getMessage(), e);
        }
        return false;
    }

    private void actualizarHash(Connection conexion, int idUsuario, String nuevoHash) {
        String updateQuery = "UPDATE usuarios SET password = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(updateQuery)) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible persistir la migración del hash: " + e.getMessage());
        }
    }
}
