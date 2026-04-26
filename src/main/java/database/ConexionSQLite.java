package database;

import utils.Alertas;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQLite {

    private static final String APP_DATA_DIR = "BookTech";
    private static final String DB_FILE_NAME = "BookTechDB.db";
    private static final String DB_RESOURCE_PATH = "/database/" + DB_FILE_NAME;
    private static final String DB_EXTERNAL_RELATIVE_PATH = "database";
    private static final String DB_DEV_SOURCE = "src/main/java/database/" + DB_FILE_NAME;
    private static Connection conexion = null;


    public static Connection conectar(){
        try{
            if (conexion == null || conexion.isClosed()) {
                String dbPath = obtenerRutaDbEscribible();
                conexion = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            }
            String metodoLlamador = Thread.currentThread().getStackTrace()[2].getMethodName();
            System.out.println("Conexion establecida con la base de datos - " + metodoLlamador);
        } catch (SQLException e) {
            Alertas.mostrarError("Error al conectar a la base de datos: " + e.getMessage());
        } catch (IOException e) {
            Alertas.mostrarError("No fue posible preparar la base de datos local: " + e.getMessage());
        }
        return conexion;
    }

    private static String obtenerRutaDbEscribible() throws IOException {
        String localAppData = System.getenv("LOCALAPPDATA");

        Path directorioDatos = (localAppData != null && !localAppData.isBlank())
                ? Paths.get(localAppData, APP_DATA_DIR)
                : Paths.get(System.getProperty("user.home"), "AppData", "Local", APP_DATA_DIR);

        Files.createDirectories(directorioDatos);

        Path dbPath = directorioDatos.resolve(DB_FILE_NAME);
        if (Files.notExists(dbPath)) {
            copiarDbSemilla(dbPath);
        }

        return dbPath.toString();
    }

    private static void copiarDbSemilla(Path dbDestino) throws IOException {
        Path dbExterna = Paths.get(System.getProperty("user.dir"), DB_EXTERNAL_RELATIVE_PATH, DB_FILE_NAME);
        if (Files.exists(dbExterna)) {
            Files.copy(dbExterna, dbDestino, StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        Path dbDesarrollo = Paths.get(DB_DEV_SOURCE);
        if (Files.exists(dbDesarrollo)) {
            Files.copy(dbDesarrollo, dbDestino, StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        try (InputStream dbResource = ConexionSQLite.class.getResourceAsStream(DB_RESOURCE_PATH)) {
            if (dbResource == null) {
                throw new IOException("No se encontro la base de datos semilla en recursos: " + DB_RESOURCE_PATH);
            }
            Files.copy(dbResource, dbDestino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null) {
                conexion.close();
                conexion = null;
                String metodoLlamador = Thread.currentThread().getStackTrace()[2].getMethodName();

                System.out.println("Se cerro la conexion a la base de datos - " + metodoLlamador);
            }
        } catch (SQLException e) {
            Alertas.mostrarError("Error al cerrar la conexion a la base de datos: " + e.getMessage());
        }
    }

}