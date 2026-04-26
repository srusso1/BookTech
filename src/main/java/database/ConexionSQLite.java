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

    private static final Path DB_DIR = resolverDirectorioDatos();
    private static final Path DB_PATH = DB_DIR.resolve("BookTechDB.db");
    private static final String URL = "jdbc:sqlite:" + DB_PATH.toString();
    private static Connection conexion = null;


    public static Connection conectar(){
        try{
            inicializarDbSiNoExiste();
            String metodoLlamador = "desconocido";
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length >= 3) {
                metodoLlamador = stackTrace[2].getMethodName();
            }
            conexion = DriverManager.getConnection(URL);
            System.out.println("Conexion establecida con la base de datos - " + metodoLlamador);
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return conexion;
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null) {
                conexion.close();
                String metodoLlamador = "desconocido";
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                if (stackTrace.length >= 3) {
                    metodoLlamador = stackTrace[2].getMethodName();
                }

                System.out.println("Se cerro la conexion a la base de datos - " + metodoLlamador);
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexion a la base de datos: " + e.getMessage());
        }
    }

    private static Path resolverDirectorioDatos() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null || localAppData.isBlank()) {
            return Paths.get("data");
        }
        return Paths.get(localAppData, "BookTech", "data");
    }

    private static void inicializarDbSiNoExiste() {
        try {
            Files.createDirectories(DB_DIR);
            if (Files.exists(DB_PATH)) {
                return;
            }

            if (copiarDesdePathSemilla(Paths.get("database", "BookTechDB.db"))) {
                return;
            }

            if (copiarDesdePathSemilla(Paths.get("src", "main", "java", "database", "BookTechDB.db"))) {
                return;
            }

            try (InputStream inputStream = ConexionSQLite.class.getResourceAsStream("/database/BookTechDB.db")) {
                if (inputStream != null) {
                    Files.copy(inputStream, DB_PATH, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }

            Files.createFile(DB_PATH);
        } catch (IOException e) {
            System.err.println("Error al inicializar la base de datos local: " + e.getMessage());
        }
    }

    private static boolean copiarDesdePathSemilla(Path semillaPath) throws IOException {
        if (!Files.exists(semillaPath)) {
            return false;
        }
        Files.copy(semillaPath, DB_PATH, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

}