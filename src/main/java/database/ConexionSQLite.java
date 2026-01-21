package database;

import utils.Alertas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQLite {

    private static final String DB_PATH = "src/main/java/database/BookTechDB.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;
    private static Connection conexion = null;


    public static Connection conectar(){
        try{
            String metodoLlamador = Thread.currentThread().getStackTrace()[2].getMethodName();
            conexion = DriverManager.getConnection(URL);
            System.out.println("Conexion establecida con la base de datos - " + metodoLlamador);
        } catch (SQLException e) {
            Alertas.mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        }
        return conexion;
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null) {
                conexion.close();
                String metodoLlamador = Thread.currentThread().getStackTrace()[2].getMethodName();

                System.out.println("Se cerro la conexion a la base de datos - " + metodoLlamador);
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al cerrar la conexion a la base de datos: " + e.getMessage());
        }
    }

}