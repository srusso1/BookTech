package database;

import model.Prestamo;
import utils.Alertas;
import utils.Fechas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static utils.Fechas.esDespues;
import static utils.Fechas.fechaActual;

public class PrestamosDAO {
    // ESTADO: 0 - Prestado, 1 - Devuelto, 2 - Pendiente

    public boolean registrarPrestamo(int idLibro, String estudiante, int grado, String fechaPrestamo, String fechaLimite){
        String query = "INSERT INTO prestamos (id_libro, estudiante, grado, fecha_prestamo, fecha_limite) VALUES (?, ?, ?, ?, ?)";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, idLibro);
            ps.setString(2, estudiante);
            ps.setInt(3, grado);
            ps.setString(4, fechaPrestamo);
            ps.setString(5, fechaLimite);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Alertas.mostrarError("Error al registrar el prestamo: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public ArrayList<Prestamo> buscarPrestamosLibro(int idLibro) {
        String query = "SELECT * FROM prestamos WHERE id_libro = ? AND estado != 1";
        ArrayList<Prestamo> prestamos = new ArrayList<>();

        try {
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, idLibro);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                prestamos.add(new Prestamo(
                        rs.getInt("estado"),
                        rs.getString("fecha_limite"),
                        rs.getString("fecha_prestamo"),
                        rs.getString("estudiante"),
                        rs.getInt("id_libro"),
                        rs.getInt("id"),
                        rs.getInt("grado")
                ));
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al buscar los préstamos: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }

        return prestamos; //
    }

    public ArrayList<Prestamo> buscarPrestamosActivos() {

            String query = """
            SELECT p.*, l.titulo
            FROM prestamos p
            JOIN libros l ON l.id = p.id_libro
            WHERE p.estado != 1
        """;

        ArrayList<Prestamo> prestamos = new ArrayList<>();

        try {
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                prestamos.add(new Prestamo(
                        rs.getInt("id"),                 // id
                        rs.getString("titulo"),          // tituloLibro
                        rs.getInt("grado"),              // grado
                        rs.getInt("estado"),             // estado
                        rs.getString("fecha_limite"),    // fecha_limite
                        rs.getString("fecha_prestamo"),  // fecha_prestamo
                        rs.getString("estudiante"),      // estudiante
                        rs.getInt("id_libro")             // id_libro
                ));
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al buscar los préstamos: " + e.getMessage());
        } finally {
            ConexionSQLite.cerrarConexion();
        }

        return prestamos;
    }




    public boolean registrarDevolucion(Prestamo prestamo){

        String query = """
                UPDATE prestamos
                SET estado = ?, fecha_devolucion = ?
                WHERE id = ?
            """;
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, 1);
            ps.setString(2, fechaActual());
            ps.setInt(3, prestamo.getId());
            return ps.executeUpdate() > 0;
        }catch (SQLException e) {
            Alertas.mostrarError("Error al registrar la devolución: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

}
