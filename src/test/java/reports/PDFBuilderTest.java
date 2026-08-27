package reports;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import reports.utils.PDFBuilder;
import com.itextpdf.layout.element.Table;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class PDFBuilderTest {

    @Test
    @DisplayName("Verifica la creación y construcción de PDF mediante PDFBuilder de forma aislada")
    void testCreacionPDF(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("test_report.pdf").toFile();
        PDFBuilder builder = new PDFBuilder(pdfFile.getAbsolutePath());

        builder.agregarTitulo("Reporte de Prueba BookTech")
               .agregarSubtitulo("Subtítulo de verificación unitaria")
               .agregarParrafo("Este es un párrafo generado automáticamente durante las pruebas.")
               .agregarSeccion("Sección de Datos")
               .agregarLineaDetalle("Clave", "Valor");

        Table tabla = builder.crearTabla(3, new String[]{"Col 1", "Col 2", "Col 3"});
        builder.agregarFilaTabla(tabla, new String[]{"A", "B", "C"});
        builder.agregarTabla(tabla);

        builder.agregarFooterConFecha();
        builder.construir();

        assertThat(pdfFile.exists()).isTrue();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }
}
