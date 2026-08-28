package reports;

import model.Docente;
import model.InventarioLibroDetalle;
import model.MotivoPrestamo;
import model.Prestamo;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reports.generators.ExcelExportManager;
import reports.utils.ExcelReportBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelReportGeneratorTest {

    @TempDir
    Path tempDir;

    private Path excelPath;

    @BeforeEach
    void setUp() {
        excelPath = tempDir.resolve("test_report.xlsx");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(excelPath);
    }

    @Test
    void testExcelReportBuilderBasics() throws Exception {
        ExcelReportBuilder builder = new ExcelReportBuilder("Pruebas");

        builder.agregarCabeceraInstitucional("Reporte de Prueba", "2026-08-01 al 2026-08-31", "Admin Test")
                .agregarTarjetasKpis(
                        List.of("Total Préstamos", "A Tiempo", "Tasa Éxito"),
                        List.of("120", "110", "91.6%")
                )
                .agregarTablaResumen("DISTRIBUCIÓN", "Grado", "Cantidad", Map.of("10°", 50, "11°", 70))
                .agregarTablaDetalle("DETALLE", List.of("ID", "Libro", "Cantidad"), List.of(
                        List.of(1, "Libro A", 10),
                        List.of(2, "Libro B", 25)
                ));

        builder.guardar(excelPath);

        assertTrue(Files.exists(excelPath));
        assertTrue(Files.size(excelPath) > 1000, "El archivo Excel generado debe contener datos y estilos");

        try (FileInputStream in = new FileInputStream(excelPath.toFile());
             XSSFWorkbook wb = new XSSFWorkbook(in)) {
            XSSFSheet sheet = wb.getSheet("Pruebas");
            assertNotNull(sheet);
            assertTrue(sheet.getLastRowNum() > 5, "La hoja debe tener más de 5 filas");
        }
    }

    @Test
    void testExcelExportManagerGeneralReport() throws Exception {
        ExcelExportManager manager = new ExcelExportManager();

        Prestamo p1 = new Prestamo(1, "Cien Años de Soledad", 11, 1, "2026-08-15", "2026-08-01", "Juan Pérez", 101);
        p1.setDocente(new Docente(1, "Carlos", "Alberto", "Gómez", "Restrepo"));
        p1.setMotivoPrestamo(new MotivoPrestamo(1, "Lectura"));
        p1.setFecha_devolucion("2026-08-14");
        p1.setDevuelto_tarde(0);
        p1.setDias_atraso(0);

        List<Prestamo> datos = List.of(p1);

        manager.exportarReporte(
                "Reporte general prestamos de libros",
                "2026-08-01",
                "2026-08-31",
                null,
                null,
                null,
                datos,
                excelPath
        );

        assertTrue(Files.exists(excelPath));
        assertTrue(Files.size(excelPath) > 1000);

        try (FileInputStream in = new FileInputStream(excelPath.toFile());
             XSSFWorkbook wb = new XSSFWorkbook(in)) {
            XSSFSheet sheet = wb.getSheetAt(0);
            assertNotNull(sheet);
            assertEquals("BOOKTECH - SISTEMA DE GESTIÓN BIBLIOTECARIA", sheet.getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test
    void testExcelExportManagerInventario() throws Exception {
        ExcelExportManager manager = new ExcelExportManager();

        InventarioLibroDetalle i1 = new InventarioLibroDetalle(
                1, "El Quijote", "Novela Clásica", "Miguel de Cervantes", "Alfaguara", "A-12",
                2, 5, 20, 10, 8
        );

        manager.exportarReporte(
                "Reporte de inventario",
                null,
                null,
                null,
                null,
                null,
                List.of(i1),
                excelPath
        );

        assertTrue(Files.exists(excelPath));
        assertTrue(Files.size(excelPath) > 1000);

        try (FileInputStream in = new FileInputStream(excelPath.toFile());
             XSSFWorkbook wb = new XSSFWorkbook(in)) {
            XSSFSheet sheet = wb.getSheetAt(0);
            assertNotNull(sheet);
            assertTrue(sheet.getLastRowNum() > 3);
        }
    }
}
