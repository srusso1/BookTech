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

    public boolean registrarPrestamo(int idLibro, int id_estudiante, String fechaPrestamo, String fechaLimite){
        String query = "INSERT INTO prestamos (id_libro, id_estudiante, fecha_prestamo, fecha_limite) VALUES (?, ?, ?, ?)";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, idLibro);
            ps.setInt(2, id_estudiante);
            ps.setString(3, fechaPrestamo);
            ps.setString(4, fechaLimite);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Alertas.mostrarError("Error al registrar el prestamo: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

    public ArrayList<Prestamo> buscarPrestamosLibro(int idLibro) {

        String query = """
        SELECT 
            p.id,
            p.id_libro,
            p.estado,
            p.fecha_prestamo,
            p.fecha_limite,
            e.grado,
            e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante
        FROM prestamos p
        JOIN estudiantes e ON e.id = p.id_estudiante
        WHERE p.id_libro = ? AND p.estado != 1
    """;

        ArrayList<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query)) {

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
        }

        return prestamos;
    }

    public ArrayList<Prestamo> buscarPrestamosActivos() {

        String query = """
        SELECT 
            p.id,
            p.id_libro,
            p.estado,
            p.fecha_prestamo,
            p.fecha_limite,
            e.grado,
            l.titulo,
            e.apellido_1 || ' ' || e.apellido_2 || ' ' || e.nombre_1 || ' ' || e.nombre_2 AS estudiante
        FROM prestamos p
        JOIN libros l ON l.id = p.id_libro
        JOIN estudiantes e ON e.id = p.id_estudiante
        WHERE p.estado != 1
    """;

        ArrayList<Prestamo> prestamos = new ArrayList<>();

        try (Connection conexion = ConexionSQLite.conectar();
             PreparedStatement ps = conexion.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prestamos.add(new Prestamo(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getInt("grado"),
                        rs.getInt("estado"),
                        rs.getString("fecha_limite"),
                        rs.getString("fecha_prestamo"),
                        rs.getString("estudiante"),
                        rs.getInt("id_libro")
                ));
            }

        } catch (SQLException e) {
            Alertas.mostrarError("Error al buscar los préstamos: " + e.getMessage());
        }

        return prestamos;
    }

    public void actualizarPrestamosTarde(){
        int actualizados = 0;
        ArrayList<Prestamo> prestamos = buscarPrestamosActivos();
        ArrayList<Prestamo> prestadosTarde = new ArrayList<>();
        for(Prestamo prestados : prestamos){
            if(Fechas.esDespues(fechaActual(), prestados.getFecha_limite())){
                prestadosTarde.add(prestados);
            }
        }

        for(Prestamo prestados : prestadosTarde){
            if(actualizarEstado(prestados)){
                actualizados++;
            }
        }
        if(actualizados > 0){
            Alertas.mostrarInfo("Se actualizaron " + actualizados + " prestamos. Consulte el modulo 'Prestamos activos'");
        }


    }

    public boolean actualizarEstado(Prestamo prestamo){
        String query = "UPDATE prestamos SET estado = ? WHERE id = ?";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, 2);
            ps.setInt(2, prestamo.getId());
            return ps.executeUpdate() > 0;
        }catch (SQLException e){
            Alertas.mostrarError("Error al actualizar el estado: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
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

    public boolean validarPrestamo(int idLibro, int idEstudiante){
        String query = "SELECT * FROM prestamos WHERE id_estudiante = ? AND (estado = 0 OR estado = 2) AND id_libro = ?";
        try{
            Connection conexion = ConexionSQLite.conectar();
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idLibro);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }catch (SQLException e){
            Alertas.mostrarError("Error al validar el prestamo: " + e.getMessage());
        }finally {
            ConexionSQLite.cerrarConexion();
        }
        return false;
    }

}
