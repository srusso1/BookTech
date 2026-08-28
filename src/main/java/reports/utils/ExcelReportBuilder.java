package reports.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generador profesional de reportes en hojas de cálculo Excel (.xlsx) con Apache POI.
 * Aplica estilos institucionales, tarjetas de KPIs, tablas con autofiltro y franjas alternadas.
 */
public class ExcelReportBuilder {

    private final XSSFWorkbook workbook;
    private final XSSFSheet sheet;
    private int currentRow = 0;

    // Paleta de Colores
    private final XSSFColor COLOR_NAVY = new XSSFColor(new byte[]{(byte) 30, (byte) 58, (byte) 138}, null); // #1E3A8A
    private final XSSFColor COLOR_BLUE = new XSSFColor(new byte[]{(byte) 37, (byte) 99, (byte) 235}, null); // #2563EB
    private final XSSFColor COLOR_CARD_BG = new XSSFColor(new byte[]{(byte) 241, (byte) 245, (byte) 249}, null); // #F1F5F9
    private final XSSFColor COLOR_ZEBRA = new XSSFColor(new byte[]{(byte) 248, (byte) 250, (byte) 252}, null); // #F8FAFC

    // Estilos reutilizables
    private XSSFCellStyle styleHeaderPrincipal;
    private XSSFCellStyle styleSubtitulo;
    private XSSFCellStyle styleMeta;
    private XSSFCellStyle styleCardTitulo;
    private XSSFCellStyle styleCardValor;
    private XSSFCellStyle styleTableHead;
    private XSSFCellStyle styleTableHeadSecondary;
    private XSSFCellStyle styleDataRowNormal;
    private XSSFCellStyle styleDataRowZebra;
    private XSSFCellStyle styleDataNumberNormal;
    private XSSFCellStyle styleDataNumberZebra;

    public ExcelReportBuilder(String nombreHoja) {
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet(nombreHoja != null && !nombreHoja.isBlank() ? nombreHoja : "Reporte BookTech");
        this.sheet.setDisplayGridlines(true);
        inicializarEstilos();
    }

    private void inicializarEstilos() {
        XSSFFont fontTitle = workbook.createFont();
        fontTitle.setFontName("Calibri");
        fontTitle.setFontHeightInPoints((short) 16);
        fontTitle.setBold(true);
        fontTitle.setColor(IndexedColors.WHITE.getIndex());

        styleHeaderPrincipal = workbook.createCellStyle();
        styleHeaderPrincipal.setFont(fontTitle);
        styleHeaderPrincipal.setFillForegroundColor(COLOR_NAVY);
        styleHeaderPrincipal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeaderPrincipal.setAlignment(HorizontalAlignment.CENTER);
        styleHeaderPrincipal.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont fontSub = workbook.createFont();
        fontSub.setFontName("Calibri");
        fontSub.setFontHeightInPoints((short) 12);
        fontSub.setBold(true);
        fontSub.setColor(IndexedColors.WHITE.getIndex());

        styleSubtitulo = workbook.createCellStyle();
        styleSubtitulo.setFont(fontSub);
        styleSubtitulo.setFillForegroundColor(COLOR_BLUE);
        styleSubtitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleSubtitulo.setAlignment(HorizontalAlignment.LEFT);
        styleSubtitulo.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont fontMeta = workbook.createFont();
        fontMeta.setFontName("Calibri");
        fontMeta.setFontHeightInPoints((short) 10);
        fontMeta.setItalic(true);
        fontMeta.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

        styleMeta = workbook.createCellStyle();
        styleMeta.setFont(fontMeta);
        styleMeta.setFillForegroundColor(COLOR_CARD_BG);
        styleMeta.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleMeta.setAlignment(HorizontalAlignment.LEFT);

        XSSFFont fontTableHead = workbook.createFont();
        fontTableHead.setFontName("Calibri");
        fontTableHead.setFontHeightInPoints((short) 11);
        fontTableHead.setBold(true);
        fontTableHead.setColor(IndexedColors.WHITE.getIndex());

        styleTableHead = workbook.createCellStyle();
        styleTableHead.setFont(fontTableHead);
        styleTableHead.setFillForegroundColor(COLOR_NAVY);
        styleTableHead.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleTableHead.setAlignment(HorizontalAlignment.CENTER);
        styleTableHead.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(styleTableHead);

        styleTableHeadSecondary = workbook.createCellStyle();
        styleTableHeadSecondary.setFont(fontTableHead);
        styleTableHeadSecondary.setFillForegroundColor(COLOR_BLUE);
        styleTableHeadSecondary.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleTableHeadSecondary.setAlignment(HorizontalAlignment.LEFT);
        styleTableHeadSecondary.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(styleTableHeadSecondary);

        XSSFFont fontCardTitle = workbook.createFont();
        fontCardTitle.setFontName("Calibri");
        fontCardTitle.setFontHeightInPoints((short) 9);
        fontCardTitle.setBold(true);
        fontCardTitle.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

        styleCardTitulo = workbook.createCellStyle();
        styleCardTitulo.setFont(fontCardTitle);
        styleCardTitulo.setFillForegroundColor(COLOR_CARD_BG);
        styleCardTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleCardTitulo.setAlignment(HorizontalAlignment.CENTER);
        aplicarBordes(styleCardTitulo);

        XSSFFont fontCardVal = workbook.createFont();
        fontCardVal.setFontName("Calibri");
        fontCardVal.setFontHeightInPoints((short) 15);
        fontCardVal.setBold(true);
        fontCardVal.setColor(IndexedColors.DARK_BLUE.getIndex());

        styleCardValor = workbook.createCellStyle();
        styleCardValor.setFont(fontCardVal);
        styleCardValor.setFillForegroundColor(COLOR_CARD_BG);
        styleCardValor.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleCardValor.setAlignment(HorizontalAlignment.CENTER);
        aplicarBordes(styleCardValor);

        XSSFFont fontData = workbook.createFont();
        fontData.setFontName("Calibri");
        fontData.setFontHeightInPoints((short) 10);

        styleDataRowNormal = workbook.createCellStyle();
        styleDataRowNormal.setFont(fontData);
        aplicarBordes(styleDataRowNormal);

        styleDataRowZebra = workbook.createCellStyle();
        styleDataRowZebra.setFont(fontData);
        styleDataRowZebra.setFillForegroundColor(COLOR_ZEBRA);
        styleDataRowZebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        aplicarBordes(styleDataRowZebra);

        DataFormat df = workbook.createDataFormat();

        styleDataNumberNormal = workbook.createCellStyle();
        styleDataNumberNormal.setFont(fontData);
        styleDataNumberNormal.setDataFormat(df.getFormat("#,##0"));
        styleDataNumberNormal.setAlignment(HorizontalAlignment.RIGHT);
        aplicarBordes(styleDataNumberNormal);

        styleDataNumberZebra = workbook.createCellStyle();
        styleDataNumberZebra.setFont(fontData);
        styleDataNumberZebra.setDataFormat(df.getFormat("#,##0"));
        styleDataNumberZebra.setFillForegroundColor(COLOR_ZEBRA);
        styleDataNumberZebra.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleDataNumberZebra.setAlignment(HorizontalAlignment.RIGHT);
        aplicarBordes(styleDataNumberZebra);
    }

    private void aplicarBordes(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    /**
     * Escribe la cabecera institucional principal del reporte.
     */
    public ExcelReportBuilder agregarCabeceraInstitucional(String tituloReporte, String rangoFechas, String usuario) {
        // Fila 0: Institución
        Row row0 = sheet.createRow(currentRow++);
        row0.setHeightInPoints(32);
        Cell c0 = row0.createCell(0);
        c0.setCellValue("BOOKTECH - SISTEMA DE GESTIÓN BIBLIOTECARIA");
        c0.setCellStyle(styleHeaderPrincipal);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        // Fila 1: Título del Reporte
        Row row1 = sheet.createRow(currentRow++);
        row1.setHeightInPoints(24);
        Cell c1 = row1.createCell(0);
        c1.setCellValue(" " + tituloReporte.toUpperCase());
        c1.setCellStyle(styleSubtitulo);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

        // Fila 2: Metadatos
        String fechaGen = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String metaText = " Generado el: " + fechaGen + "  |  Rango: " + (rangoFechas != null ? rangoFechas : "Histórico completo") + "  |  Generado por: " + (usuario != null ? usuario : "Rectoría");
        Row row2 = sheet.createRow(currentRow++);
        row2.setHeightInPoints(18);
        Cell c2 = row2.createCell(0);
        c2.setCellValue(metaText);
        c2.setCellStyle(styleMeta);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 7));

        currentRow++; // Espaciador
        return this;
    }

    /**
     * Agrega un bloque de tarjetas métricas (KPI Cards).
     */
    public ExcelReportBuilder agregarTarjetasKpis(List<String> titulos, List<String> valores) {
        int numCards = Math.min(titulos.size(), valores.size());
        if (numCards == 0) return this;

        Row rowTitulos = sheet.createRow(currentRow++);
        rowTitulos.setHeightInPoints(18);

        Row rowValores = sheet.createRow(currentRow++);
        rowValores.setHeightInPoints(28);

        for (int i = 0; i < numCards; i++) {
            Cell ct = rowTitulos.createCell(i * 2);
            ct.setCellValue(titulos.get(i).toUpperCase());
            ct.setCellStyle(styleCardTitulo);

            Cell cv = rowValores.createCell(i * 2);
            cv.setCellValue(valores.get(i));
            cv.setCellStyle(styleCardValor);

            // Merge 2 columnas por tarjeta
            sheet.addMergedRegion(new CellRangeAddress(rowTitulos.getRowNum(), rowTitulos.getRowNum(), i * 2, i * 2 + 1));
            sheet.addMergedRegion(new CellRangeAddress(rowValores.getRowNum(), rowValores.getRowNum(), i * 2, i * 2 + 1));
        }

        currentRow++; // Espaciador
        return this;
    }

    /**
     * Agrega una sección de desglose por categoría o mapa estadístico.
     */
    public ExcelReportBuilder agregarTablaResumen(String tituloSeccion, String headerCol1, String headerCol2, Map<String, Integer> datos) {
        if (datos == null || datos.isEmpty()) return this;

        Row rowSec = sheet.createRow(currentRow++);
        rowSec.setHeightInPoints(20);
        Cell cSec = rowSec.createCell(0);
        cSec.setCellValue(" " + tituloSeccion);
        cSec.setCellStyle(styleTableHeadSecondary);
        sheet.addMergedRegion(new CellRangeAddress(rowSec.getRowNum(), rowSec.getRowNum(), 0, 2));

        Row rowHead = sheet.createRow(currentRow++);
        rowHead.setHeightInPoints(20);
        Cell h1 = rowHead.createCell(0);
        h1.setCellValue(headerCol1);
        h1.setCellStyle(styleTableHead);
        sheet.addMergedRegion(new CellRangeAddress(rowHead.getRowNum(), rowHead.getRowNum(), 0, 1));

        Cell h2 = rowHead.createCell(2);
        h2.setCellValue(headerCol2);
        h2.setCellStyle(styleTableHead);

        int total = 0;
        int idx = 0;
        for (Map.Entry<String, Integer> entry : datos.entrySet()) {
            Row r = sheet.createRow(currentRow++);
            boolean zebra = (idx % 2 == 1);
            CellStyle sTxt = zebra ? styleDataRowZebra : styleDataRowNormal;
            CellStyle sNum = zebra ? styleDataNumberZebra : styleDataNumberNormal;

            Cell cNombre = r.createCell(0);
            cNombre.setCellValue(entry.getKey());
            cNombre.setCellStyle(sTxt);
            sheet.addMergedRegion(new CellRangeAddress(r.getRowNum(), r.getRowNum(), 0, 1));

            Cell cVal = r.createCell(2);
            cVal.setCellValue(entry.getValue());
            cVal.setCellStyle(sNum);

            total += entry.getValue();
            idx++;
        }

        // Fila de total
        Row rTot = sheet.createRow(currentRow++);
        Cell cTotLbl = rTot.createCell(0);
        cTotLbl.setCellValue("TOTAL");
        cTotLbl.setCellStyle(styleTableHeadSecondary);
        sheet.addMergedRegion(new CellRangeAddress(rTot.getRowNum(), rTot.getRowNum(), 0, 1));

        Cell cTotVal = rTot.createCell(2);
        cTotVal.setCellValue(total);
        cTotVal.setCellStyle(styleTableHeadSecondary);

        currentRow++; // Espaciador
        return this;
    }

    /**
     * Agrega la tabla de datos detallados con autofiltro y encabezados fijados.
     */
    public ExcelReportBuilder agregarTablaDetalle(String tituloSeccion, List<String> encabezados, List<List<Object>> filas) {
        if (encabezados == null || encabezados.isEmpty()) return this;

        if (tituloSeccion != null && !tituloSeccion.isBlank()) {
            Row rowSec = sheet.createRow(currentRow++);
            rowSec.setHeightInPoints(22);
            Cell cSec = rowSec.createCell(0);
            cSec.setCellValue(" " + tituloSeccion);
            cSec.setCellStyle(styleSubtitulo);
            sheet.addMergedRegion(new CellRangeAddress(rowSec.getRowNum(), rowSec.getRowNum(), 0, encabezados.size() - 1));
        }

        int headerRowIndex = currentRow;
        Row rowHead = sheet.createRow(currentRow++);
        rowHead.setHeightInPoints(24);

        for (int i = 0; i < encabezados.size(); i++) {
            Cell c = rowHead.createCell(i);
            c.setCellValue(encabezados.get(i));
            c.setCellStyle(styleTableHead);
        }

        if (filas != null && !filas.isEmpty()) {
            for (int rIdx = 0; rIdx < filas.size(); rIdx++) {
                List<Object> fila = filas.get(rIdx);
                Row row = sheet.createRow(currentRow++);
                boolean zebra = (rIdx % 2 == 1);

                for (int cIdx = 0; cIdx < fila.size(); cIdx++) {
                    Object val = fila.get(cIdx);
                    Cell cell = row.createCell(cIdx);

                    if (val instanceof Number num) {
                        cell.setCellValue(num.doubleValue());
                        cell.setCellStyle(zebra ? styleDataNumberZebra : styleDataNumberNormal);
                    } else {
                        cell.setCellValue(val != null ? val.toString() : "");
                        cell.setCellStyle(zebra ? styleDataRowZebra : styleDataRowNormal);
                    }
                }
            }

            // AutoFiltro
            sheet.setAutoFilter(new CellRangeAddress(headerRowIndex, currentRow - 1, 0, encabezados.size() - 1));
        }

        // Autoajustar columnas con padding
        for (int i = 0; i < encabezados.size(); i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.max(currentWidth + 1200, 3800));
        }

        return this;
    }

    /**
     * Guarda el libro Excel generado en el archivo de destino.
     */
    public void guardar(Path destino) throws IOException {
        try (FileOutputStream out = new FileOutputStream(destino.toFile())) {
            workbook.write(out);
        } finally {
            workbook.close();
        }
    }
}
