package database;

import utils.Alertas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MotivosPrestamoDAO {
    public Map<Integer, String> obtenerMotivosPrestamo() {
        Map<Integer, String> motivos = new HashMap<>();
        String query = "SELECT * FROM motivos_prestamo";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                motivos.put(rs.getInt("id"), rs.getString("nombre_motivo"));
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al registrar el prestamo: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return motivos;
    }
}
