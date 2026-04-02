package database;

import model.Docente;
import utils.Alertas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class DocentesDAO {
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

        } catch (Exception e) {
            Alertas.mostrarError("Error al obtener los docentes: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return lista;
    }
}
