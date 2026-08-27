package database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConexionSQLite {

    private static final Logger LOGGER = Logger.getLogger(ConexionSQLite.class.getName());
    private static final Path DB_DIR = resolverDirectorioDatos();
    private static final Path DB_PATH = DB_DIR.resolve("BookTechDB.db");
    private static final String URL = "jdbc:sqlite:" + DB_PATH.toString();

    public static Connection conectar() {
        try {
            inicializarDbSiNoExiste();
            Connection conn = DriverManager.getConnection(URL);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.execute("PRAGMA journal_mode = WAL;");
                stmt.execute("PRAGMA busy_timeout = 5000;");
            }
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar a la base de datos: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * @deprecated Las conexiones deben gestionarse con try-with-resources.
     */
    @Deprecated
    public static void cerrarConexion() {
        // No-op para compatibilidad: cada try-with-resources cierra su propia conexion
    }

    private static Path resolverDirectorioDatos() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null || localAppData.isBlank()) {
            return Paths.get("data");
        }
        return Paths.get(localAppData, "BookTech", "data");
    }

    private static synchronized void inicializarDbSiNoExiste() {
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
            LOGGER.log(Level.SEVERE, "Error al inicializar la base de datos local: " + e.getMessage(), e);
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
