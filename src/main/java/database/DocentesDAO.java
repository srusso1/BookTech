package database;

import model.Docente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocentesDAO {

    private static final Logger LOGGER = Logger.getLogger(DocentesDAO.class.getName());

    public ArrayList<Docente> obtenerDocentes() {
        ArrayList<Docente> lista = new ArrayList<>();
        String sql = "SELECT * FROM docentes ORDER BY apellido_1, nombre_1";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Docente(
                        rs.getInt("id"),
                        rs.getString("nombre_1"),
                        rs.getString("nombre_2"),
                        rs.getString("apellido_1"),
                        rs.getString("apellido_2")
                ));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los docentes: " + e.getMessage(), e);
        }

        return lista;
    }
}
