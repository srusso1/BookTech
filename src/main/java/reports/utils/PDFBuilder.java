package reports.utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Clase utilidad para construir PDFs con iText 7
 * Proporciona métodos comunes para crear reportes con estándares visuales consistentes
 */
public class PDFBuilder {

    private Document document;
    private PdfFont fontTitulo;
    private PdfFont fontSubtitulo;
    private PdfFont fontNormal;
    private PdfFont fontBold;

    /**
     * Constructor que inicializa el documento PDF
     * @param rutaArchivo Ruta completa donde se guardará el PDF
     */
    public PDFBuilder(String rutaArchivo) {
        try {
            // Crear archivo si no existe
            File file = new File(rutaArchivo);
            file.getParentFile().mkdirs();

            // Inicializar escritor y documento
            PdfWriter writer = new PdfWriter(rutaArchivo);
            PdfDocument pdfDoc = new PdfDocument(writer);
            document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(20, 20, 20, 20);

            // Inicializar fuentes con constantes oficiales de iText
            fontTitulo = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            fontSubtitulo = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
            fontNormal = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
            fontBold = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);

        } catch (Exception e) {
            throw new RuntimeException("Error al crear el PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Agrega un título principal al reporte
     */
    public PDFBuilder agregarTitulo(String titulo) {
        Paragraph p = new Paragraph(titulo)
                .setFont(fontTitulo)
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(p);
        return this;
    }

    /**
     * Agrega un subtítulo al reporte
     */
    public PDFBuilder agregarSubtitulo(String subtitulo) {
        Paragraph p = new Paragraph(subtitulo)
                .setFont(fontSubtitulo)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15)
                .setFontColor(ColorConstants.GRAY);
        document.add(p);
        return this;
    }

    /**
     * Agrega un párrafo de texto normal
     */
    public PDFBuilder agregarParrafo(String texto) {
        Paragraph p = new Paragraph(texto)
                .setFont(fontNormal)
                .setFontSize(11)
                .setMarginBottom(10);
        document.add(p);
        return this;
    }

    /**
     * Agrega un título de sección con estilo discreto
     */
    public PDFBuilder agregarSeccion(String titulo) {
        Paragraph p = new Paragraph(titulo)
                .setFont(fontBold)
                .setFontSize(12)
                .setMarginTop(4)
                .setMarginBottom(6)
                .setFontColor(ColorConstants.DARK_GRAY);
        document.add(p);
        return this;
    }

    /**
     * Agrega línea con etiqueta en negrita y valor normal
     */
    public PDFBuilder agregarLineaDetalle(String etiqueta, String valor) {
        Paragraph p = new Paragraph()
                .setMarginLeft(12)
                .setMarginBottom(6)
                .setFontSize(11);

        p.add(new Text(etiqueta + ": ").setFont(fontBold));
        p.add(new Text(valor != null ? valor : "--").setFont(fontNormal));

        document.add(p);
        return this;
    }

    /**
     * Agrega un párrafo con sangría para notas o descripciones
     */
    public PDFBuilder agregarParrafoIndentado(String texto) {
        Paragraph p = new Paragraph(texto)
                .setFont(fontNormal)
                .setFontSize(11)
                .setMarginLeft(12)
                .setMarginBottom(8);
        document.add(p);
        return this;
    }

    /**
     * Agrega espacio en blanco
     */
    public PDFBuilder agregarEspacio(float espacio) {
        document.add(new Paragraph().setMarginBottom(espacio));
        return this;
    }

    /**
     * Crea una tabla con encabezados
     * @param numColumnas Número de columnas
     * @param encabezados Array de encabezados
     */
    public Table crearTabla(int numColumnas, String[] encabezados) {
        return crearTabla(numColumnas, encabezados, ColorConstants.LIGHT_GRAY);
    }

    /**
     * Crea una tabla con anchos relativos personalizados por columna
     */
    public Table crearTabla(float[] anchosRelativos, String[] encabezados) {
        return crearTabla(anchosRelativos, encabezados, ColorConstants.LIGHT_GRAY);
    }

    /**
     * Crea una tabla con anchos relativos personalizados y color de encabezado
     */
    public Table crearTabla(float[] anchosRelativos, String[] encabezados, Color colorEncabezado) {
        Table tabla = new Table(UnitValue.createPercentArray(anchosRelativos))
                .useAllAvailableWidth()
                .setFixedLayout();

        int numColumnas = anchosRelativos.length;
        float fontEncabezado = numColumnas >= 8 ? 9 : (numColumnas >= 7 ? 10 : 11);
        float paddingEncabezado = numColumnas >= 8 ? 5 : (numColumnas >= 7 ? 6 : 8);

        for (String encabezado : encabezados) {
            Cell cell = new Cell()
                    .add(new Paragraph(encabezado)
                            .setFont(fontBold)
                            .setFontSize(fontEncabezado)
                            .setMultipliedLeading(1.0f))
                    .setBackgroundColor(colorEncabezado)
                    .setPadding(paddingEncabezado);
            tabla.addCell(cell);
        }

        return tabla;
    }

    /**
     * Crea una tabla con encabezados y color de fondo configurable
     */
    public Table crearTabla(int numColumnas, String[] encabezados, Color colorEncabezado) {
        Table tabla = new Table(UnitValue.createPercentArray(numColumnas))
                .useAllAvailableWidth()
                .setFixedLayout();

        float fontEncabezado = numColumnas >= 8 ? 9 : (numColumnas >= 7 ? 10 : 11);
        float paddingEncabezado = numColumnas >= 8 ? 5 : (numColumnas >= 7 ? 6 : 8);

        // Agregar encabezados
        for (String encabezado : encabezados) {
            Cell cell = new Cell()
                    .add(new Paragraph(encabezado)
                            .setFont(fontBold)
                            .setFontSize(fontEncabezado)
                            .setMultipliedLeading(1.0f))
                    .setBackgroundColor(colorEncabezado)
                    .setPadding(paddingEncabezado);
            tabla.addCell(cell);
        }

        return tabla;
    }

    /**
     * Agrega una fila a la tabla
     */
    public void agregarFilaTabla(Table tabla, String[] valores) {
        int numColumnas = valores.length;
        float fontContenido = numColumnas >= 8 ? 8 : (numColumnas >= 7 ? 9 : 10);
        float paddingContenido = numColumnas >= 8 ? 4 : (numColumnas >= 7 ? 5 : 6);

        for (String valor : valores) {
            Cell cell = new Cell()
                    .add(new Paragraph(valor != null ? valor : "")
                            .setFont(fontNormal)
                            .setFontSize(fontContenido)
                            .setMultipliedLeading(1.0f))
                    .setPadding(paddingContenido);
            tabla.addCell(cell);
        }
    }

    /**
     * Agrega una tabla al documento
     */
    public PDFBuilder agregarTabla(Table tabla) {
        document.add(tabla);
        return this;
    }

    /**
     * Agrega fecha y hora del reporte en el pie
     */
    public PDFBuilder agregarFooterConFecha() {
        agregarEspacio(20);
        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        Paragraph footer = new Paragraph("Reporte generado: " + fecha)
                .setFont(fontNormal)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY);
        document.add(footer);
        return this;
    }

    /**
     * Agrega información institucional en el encabezado
     */
    public PDFBuilder agregarEncabezadoInstitucional(String institucion) {
        Paragraph encabezado = new Paragraph(institucion)
                .setFont(fontBold)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(encabezado);
        agregarEspacio(5);
        return this;
    }

    /**
     * Agrega encabezado institucional con escudo y textos principales
     */
    public PDFBuilder agregarEncabezadoInstitucional(String institucion, String sede, String recursoEscudo) {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{1.0f, 5.0f}))
                .useAllAvailableWidth();

        Cell celdaEscudo = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
        Image logo = crearImagenDesdeRecurso(recursoEscudo);
        if (logo != null) {
            logo.scaleToFit(60, 60);
            celdaEscudo.add(logo);
        }

        Cell celdaTexto = new Cell().setBorder(Border.NO_BORDER);
        celdaTexto
                .add(new Paragraph(institucion)
                        .setFont(fontBold)
                        .setFontSize(14)
                        .setMarginBottom(2))
                .add(new Paragraph(sede)
                        .setFont(fontNormal)
                        .setFontSize(12)
                        .setMarginBottom(0));

        tabla.addCell(celdaEscudo);
        tabla.addCell(celdaTexto);
        document.add(tabla);
        agregarEspacio(6);
        return this;
    }

    /**
     * Agrega línea de ubicación y fecha, por ejemplo: Becerril, 14 de octubre del 2025
     */
    public PDFBuilder agregarLineaCiudadFecha(String ciudad) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("d 'de' MMMM 'del' yyyy", Locale.of("es", "CO"));
        String texto = ciudad + ", " + LocalDate.now().format(formato);
        document.add(new Paragraph(texto)
                .setFont(fontNormal)
                .setFontSize(11)
                .setMarginBottom(8));
        return this;
    }

    /**
     * Agrega una línea de asunto en negrita
     */
    public PDFBuilder agregarAsunto(String asunto) {
        document.add(new Paragraph("ASUNTO: " + asunto.toUpperCase(Locale.ROOT))
                .setFont(fontBold)
                .setFontSize(12)
                .setMarginBottom(10));
        return this;
    }

    private Image crearImagenDesdeRecurso(String recurso) {
        if (recurso == null || recurso.isBlank()) {
            return null;
        }

        try (InputStream is = PDFBuilder.class.getResourceAsStream(recurso)) {
            if (is == null) {
                return null;
            }
            byte[] bytes = is.readAllBytes();
            ImageData imageData = ImageDataFactory.create(bytes);
            return new Image(imageData);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Cierra el documento y guarda el PDF
     */
    public void construir() {
        try {
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al cerrar el PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el documento actual (para casos avanzados)
     */
    public Document getDocument() {
        return document;
    }
}

