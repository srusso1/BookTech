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
    private static final String URL;

    static {
        if ("test".equals(System.getProperty("booktech.env"))) {
            URL = "jdbc:sqlite:target/BookTechDB_test.db";
        } else {
            URL = "jdbc:sqlite:" + DB_PATH.toString();
        }
    }

    private static boolean migracionesAplicadas = false;

    public static Connection conectar() {
        try {
            inicializarDbSiNoExiste();
            Connection conn = DriverManager.getConnection(URL);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.execute("PRAGMA journal_mode = WAL;");
                stmt.execute("PRAGMA busy_timeout = 5000;");
            }
            if (!migracionesAplicadas) {
                ejecutarMigraciones(conn);
                migracionesAplicadas = true;
            }
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar a la base de datos: " + e.getMessage(), e);
            return null;
        }
    }

    private static synchronized void ejecutarMigraciones(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // MigraciÃ³n 1: grado_historico
            try {
                stmt.execute("ALTER TABLE prestamos ADD COLUMN grado_historico INTEGER;");
                LOGGER.info("Columna grado_historico aÃ±adida a la tabla prestamos.");
                stmt.executeUpdate("UPDATE prestamos SET grado_historico = (SELECT grado FROM estudiantes WHERE estudiantes.id = prestamos.id_estudiante) WHERE grado_historico IS NULL;");
                LOGGER.info("Datos de grado_historico migrados exitosamente.");
            } catch (SQLException e) {
                if (!e.getMessage().contains("duplicate column name")) {
                    LOGGER.log(Level.WARNING, "Advertencia en migraciÃ³n grado_historico: " + e.getMessage());
                }
            }

            // MigraciÃ³n 2: NormalizaciÃ³n de Editoriales
            try {
                // Crear tabla si no existe
                stmt.execute("CREATE TABLE IF NOT EXISTS editoriales (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT UNIQUE NOT NULL, estado INTEGER DEFAULT 1);");
                
                // Intentar aÃ±adir id_editorial a libros
                stmt.execute("ALTER TABLE libros ADD COLUMN id_editorial INTEGER REFERENCES editoriales(id);");
                LOGGER.info("Columna id_editorial aÃ±adida a la tabla libros.");
                
                // Insertar editoriales Ãºnicas a partir del texto existente en libros
                stmt.executeUpdate("INSERT OR IGNORE INTO editoriales (nombre) SELECT DISTINCT editorial FROM libros WHERE editorial IS NOT NULL AND trim(editorial) != '';");
                
                // Mapear el texto existente con su nuevo ID
                stmt.executeUpdate("UPDATE libros SET id_editorial = (SELECT id FROM editoriales WHERE editoriales.nombre = libros.editorial) WHERE id_editorial IS NULL AND editorial IS NOT NULL;");
                LOGGER.info("Datos de editoriales migrados exitosamente a estructura relacional.");
                
            } catch (SQLException e) {
                if (!e.getMessage().contains("duplicate column name")) {
                    LOGGER.log(Level.WARNING, "Advertencia en migraciÃ³n editoriales: " + e.getMessage());
                }
            }
            
            // MigraciÃ³n 3: Limpiar columna legacy editorial
            try {
                stmt.execute("ALTER TABLE libros DROP COLUMN editorial;");
                LOGGER.info("Columna legacy 'editorial' eliminada exitosamente de la tabla libros.");
            } catch (SQLException e) {
                // Silenciar error si la versiÃ³n de SQLite no soporta DROP COLUMN o si ya se eliminÃ³
                LOGGER.log(Level.INFO, "No se pudo hacer DROP de la columna 'editorial' (puede que ya no exista o versiÃ³n antigua de SQLite): " + e.getMessage());
            }

            // MigraciÃ³n 4: Estado en categorias
            try {
                stmt.execute("ALTER TABLE categorias ADD COLUMN estado INTEGER DEFAULT 1;");
                LOGGER.info("Columna 'estado' aÃ±adida a la tabla categorias.");
            } catch (SQLException e) {
                if (!e.getMessage().contains("duplicate column name")) {
                    LOGGER.log(Level.WARNING, "Advertencia en migraciÃ³n categorias: " + e.getMessage());
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error crÃ­tico ejecutando migraciones: " + e.getMessage(), e);
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
        if ("test".equals(System.getProperty("booktech.env"))) {
            try {
                Path testDb = Paths.get("target", "BookTechDB_test.db");
                if (Files.exists(testDb)) return;
                Files.createDirectories(testDb.getParent());
                if (Files.exists(Paths.get("src/main/java/database/BookTechDB.db"))) {
                    Files.copy(Paths.get("src/main/java/database/BookTechDB.db"), testDb, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {}
            return;
        }
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
