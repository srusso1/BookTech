package database;

import model.Estudiante;
import utils.Alertas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class EstudiantesDAO {
    public ArrayList<Estudiante> obtenerEstudiantes() {
        ArrayList<Estudiante> lista = new ArrayList<>();

        String sql = "SELECT * FROM estudiantes ORDER BY apellido_1, nombre_1";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Estudiante(
                        rs.getInt("id"),
                        rs.getInt("identificacion"), // 👈 aquí está como int en DB
                        rs.getInt("grado"),
                        rs.getString("apellido_1"),
                        rs.getString("apellido_2"),
                        rs.getString("nombre_1"),
                        rs.getString("nombre_2"),
                        rs.getString("genero")
                ));
            }

        } catch (Exception e) {
            Alertas.mostrarError("Error al obtener los estudiantes: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }

        return lista;
    }
}
