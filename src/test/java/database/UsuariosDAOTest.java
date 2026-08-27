package database;

import model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

public class UsuariosDAOTest {

    private final UsuariosDAO usuariosDAO = new UsuariosDAO();

    @Test
    @DisplayName("Verifica la autenticación y migración automática transparente a BCrypt")
    void testAutenticacionYMigracionBCrypt() {
        // 1. Crear usuario temporal con contraseña en texto plano
        String usuarioTemp = "user_test_" + System.currentTimeMillis();
        String passwordPlana = "ClaveSecreta123";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO usuarios (nombre, apellido, usuario, password, rol) VALUES (?, ?, ?, ?, ?)"
             )) {
            ps.setString(1, "TestNombre");
            ps.setString(2, "TestApellido");
            ps.setString(3, usuarioTemp);
            ps.setString(4, passwordPlana); // Texto plano
            ps.setInt(5, 0); // Bibliotecario
            ps.executeUpdate();
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Fallo al insertar usuario de prueba: " + e.getMessage());
        }

        // 2. Primer login: debe autenticar exitosamente y migrar a hash BCrypt
        Usuario user1 = usuariosDAO.validarUsuario(usuarioTemp, passwordPlana);
        assertThat(user1).isNotNull();
        assertThat(user1.getUsuario()).isEqualTo(usuarioTemp);

        // 3. Comprobar que en la base de datos ahora está guardado el hash $2a$
        String passwordEnBD = null;
        int userId = 0;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement("SELECT id, password FROM usuarios WHERE usuario = ?")) {
            ps.setString(1, usuarioTemp);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("id");
                    passwordEnBD = rs.getString("password");
                }
            }
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Fallo al consultar hash: " + e.getMessage());
        }

        assertThat(passwordEnBD).isNotNull();
        assertThat(passwordEnBD).startsWith("$2a$");
        assertThat(BCrypt.checkpw(passwordPlana, passwordEnBD)).isTrue();

        // 4. Segundo login: debe autenticar correctamente contra el hash BCrypt
        Usuario user2 = usuariosDAO.validarUsuario(usuarioTemp, passwordPlana);
        assertThat(user2).isNotNull();

        // 5. Intento con contraseña errónea debe fallar
        Usuario userErroneo = usuariosDAO.validarUsuario(usuarioTemp, "ClaveEquivocada");
        assertThat(userErroneo).isNull();

        // 6. Prueba de actualizarPassword con nuevo hash
        String nuevaClave = "NuevaClave456";
        boolean actualizada = usuariosDAO.actualizarPassword(userId, nuevaClave);
        assertThat(actualizada).isTrue();

        Usuario user3 = usuariosDAO.validarUsuario(usuarioTemp, nuevaClave);
        assertThat(user3).isNotNull();

        // 7. Limpieza del usuario de prueba
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM usuarios WHERE usuario = ?")) {
            ps.setString(1, usuarioTemp);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
