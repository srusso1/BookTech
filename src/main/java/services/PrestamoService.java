package services;

import database.ConexionSQLite;
import database.LibrosDAO;
import database.PrestamosDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrestamoService {

    private static final Logger LOGGER = Logger.getLogger(PrestamoService.class.getName());

    /**
     * Registra un préstamo y disminuye el stock del libro de forma atómica.
     */
    public boolean registrarPrestamo(int idLibro, int idEstudiante, int idMotivo, int idDocente, String fechaHoy, String fechaDevolucion) {
        Connection conn = null;
        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            conn.setAutoCommit(false); // Iniciar transacción

            PrestamosDAO prestamosDAO = new PrestamosDAO();
            LibrosDAO librosDAO = new LibrosDAO();

            if (prestamosDAO.validarPrestamo(conn, idLibro, idEstudiante)) {
                conn.rollback();
                return false;
            }

            boolean prestamoOk = prestamosDAO.registrarPrestamo(conn, idLibro, idEstudiante, idMotivo, idDocente, fechaHoy, fechaDevolucion);
            if (!prestamoOk) {
                conn.rollback();
                return false;
            }

            boolean stockOk = librosDAO.disminuirUnidadLibro(conn, idLibro);
            if (!stockOk) {
                conn.rollback();
                return false;
            }

            conn.commit();
            utils.DashboardNotifier.notificarCambio();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la transacción de préstamo: " + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback: " + ex.getMessage(), ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error al cerrar conexión: " + e.getMessage(), e);
                }
            }
        }
    }
    public boolean registrarDevolucion(model.Prestamo prestamo, int idLibro) {
        Connection conn = null;
        try {
            conn = ConexionSQLite.conectar();
            if (conn == null) return false;

            conn.setAutoCommit(false);

            PrestamosDAO prestamosDAO = new PrestamosDAO();
            LibrosDAO librosDAO = new LibrosDAO();

            boolean devolucionOk = prestamosDAO.registrarDevolucion(conn, prestamo);
            if (!devolucionOk) {
                conn.rollback();
                return false;
            }

            boolean stockOk = librosDAO.aumentarUnidadLibro(conn, idLibro);
            if (!stockOk) {
                conn.rollback();
                return false;
            }

            conn.commit();
            utils.DashboardNotifier.notificarCambio();
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la transacción de devolución: " + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback: " + ex.getMessage(), ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error al cerrar conexión: " + e.getMessage(), e);
                }
            }
        }
    }
}
