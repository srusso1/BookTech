package database;

import utils.Alertas;
import utils.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistroPlataformaDAO {
    public boolean registrarUso(int id_docente, String motivo_uso) {
        String query = "INSERT INTO registro_plataforma (id_docente, motivo_uso, total_horas, fecha) VALUES (?, ?, ?, ?)";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, id_docente);
            ps.setString(2, motivo_uso);
            ps.setInt(3, 0);
            ps.setString(4, Fechas.fechaActualISO());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            Alertas.mostrarError("Error al registrar el uso de la plataforma: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public boolean registrarUsoConHoras(int id_docente, String motivo_uso, String hora_inicio, String hora_fin, int total_horas) {
        String query = "INSERT INTO registro_plataforma (id_docente, motivo_uso, hora_inicio, hora_fin, total_horas, fecha) VALUES (?, ?, ?, ?, ?, ?)";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, id_docente);
            ps.setString(2, motivo_uso);
            ps.setString(3, hora_inicio);
            ps.setString(4, hora_fin);
            ps.setInt(5, total_horas);
            ps.setString(6, Fechas.fechaActualISO());
            return ps.executeUpdate() > 0;
        }catch(SQLException e){
            Alertas.mostrarError("Error al registrar el uso de la plataforma: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }
}
