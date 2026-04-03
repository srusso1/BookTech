package database;

import model.MotivoPrestamo;
import utils.Alertas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MotivosPrestamoDAO {
    public ArrayList<MotivoPrestamo> obtenerMotivosPrestamo() {

        ArrayList<MotivoPrestamo> motivos = new ArrayList<>();

        String query = "SELECT id, nombre_motivo FROM motivos_prestamo";

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                MotivoPrestamo motivo = new MotivoPrestamo(
                        rs.getInt("id"),
                        rs.getString("nombre_motivo")
                );

                motivos.add(motivo);
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al obtener motivos: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return motivos;
    }
}
