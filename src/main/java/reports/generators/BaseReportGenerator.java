package reports.generators;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import reports.models.ReportConfig;
import reports.utils.PDFBuilder;

import java.awt.Desktop;
import java.io.File;

/**
 * Clase base abstracta para todos los generadores de reportes
 * Define estructura común y métodos que pueden reutilizar los subgeneradores
 */
public abstract class BaseReportGenerator {

    protected ReportConfig config;
    protected String rutaArchivo;
    protected PDFBuilder pdfBuilder;

    /**
     * Constructor base
     */
    public BaseReportGenerator(ReportConfig config, String nombreArchivo) {
        this.config = config;
        this.rutaArchivo = seleccionarRutaArchivo(nombreArchivo);
        if (this.rutaArchivo != null) {
            this.pdfBuilder = new PDFBuilder(rutaArchivo);
        }
    }

    private String seleccionarRutaArchivo(String nombreArchivo) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar reporte PDF");
        fileChooser.setInitialFileName(nombreArchivo);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

        File carpetaPorDefecto = new File(ReportConfig.RUTA_REPORTES);
        if (!carpetaPorDefecto.exists()) {
            carpetaPorDefecto.mkdirs();
        }
        if (carpetaPorDefecto.exists() && carpetaPorDefecto.isDirectory()) {
            fileChooser.setInitialDirectory(carpetaPorDefecto);
        }

        Window owner = null;
        for (Window ventana : Window.getWindows()) {
            if (ventana.isShowing()) {
                owner = ventana;
                break;
            }
        }

        File seleccionado = fileChooser.showSaveDialog(owner);
        if (seleccionado == null) {
            return null;
        }

        String ruta = seleccionado.getAbsolutePath();
        return ruta.toLowerCase().endsWith(".pdf") ? ruta : ruta + ".pdf";
    }

    protected boolean puedeGenerar() {
        return pdfBuilder != null && rutaArchivo != null;
    }

    /**
     * Método abstracto que implementarán los subgeneradores
     * Define la estructura del reporte específico
     */
    public abstract void generar();

    /**
     * Método para agregar encabezado estándar a todos los reportes
     */
    protected void agregarEncabezadoEstandar(String titulo) {
        pdfBuilder
                .agregarEncabezadoInstitucional(
                        ReportConfig.INSTITUCION,
                        ReportConfig.ESCUELA,
                        ReportConfig.ESCUDO_REPORTE
                )
                .agregarLineaCiudadFecha(ReportConfig.CIUDAD_REPORTE)
                .agregarAsunto(titulo)
                .agregarEspacio(10);
    }

    /**
     * Método para agregar pie de página estándar
     */
    protected void agregarPieEstandar() {
        pdfBuilder.agregarFooterConFecha();
    }

    /**
     * Cierra y guarda el PDF
     */
    protected void finalizarReporte() {
        agregarPieEstandar();
        pdfBuilder.construir();
        abrirCarpetaDestino();
    }

    private void abrirCarpetaDestino() {
        try {
            if (rutaArchivo == null) {
                return;
            }
            File archivo = new File(rutaArchivo);
            File carpeta = archivo.getParentFile();
            if (carpeta != null && carpeta.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(carpeta);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Obtiene la ruta del archivo generado
     */
    public String getRutaArchivo() {
        return rutaArchivo;
    }

    /**
     * Obtiene el generador PDF para acceso directo (si es necesario)
     */
    public PDFBuilder getPDFBuilder() {
        return pdfBuilder;
    }
}

