package reports.generators;

import database.InformesDAO;
import model.Docente;
import model.Estudiante;
import model.InventarioLibroDetalle;
import model.Prestamo;
import model.RegistroPlataformaDetalle;
import reports.models.ReportConfig;
import reports.utils.ExcelReportBuilder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gestor especializado para compilar y generar reportes analíticos completos en Excel (.xlsx).
 */
public class ExcelExportManager {

    private final InformesDAO informesDAO;

    public ExcelExportManager() {
        this.informesDAO = new InformesDAO();
    }

    public void exportarReporte(String tipoReporte, String fInicio, String fFin, Integer grado,
                                Estudiante estudiante, Docente docente, List<?> datosTabla, Path destino) throws Exception {

        ExcelReportBuilder builder = new ExcelReportBuilder(tipoReporte);
        String rango = (fInicio != null && fFin != null) ? (fInicio + " al " + fFin) : "Histórico completo";
        builder.agregarCabeceraInstitucional(tipoReporte, rango, "Rectoría");

        if ("Reporte por estudiante prestamos de libros".equals(tipoReporte)) {
            exportarPorEstudiante(builder, estudiante, fInicio, fFin, datosTabla);
        } else if ("Reporte prestamos devueltos tarde".equals(tipoReporte)) {
            exportarDevueltosTarde(builder, grado, fInicio, fFin, datosTabla);
        } else if ("Reporte plataforma virtual general".equals(tipoReporte)) {
            exportarPlataformaGeneral(builder, fInicio, fFin, datosTabla);
        } else if ("Reporte plataforma virtual por docente".equals(tipoReporte)) {
            exportarPlataformaDocente(builder, docente, fInicio, fFin, datosTabla);
        } else if ("Reporte de inventario".equals(tipoReporte)) {
            exportarInventario(builder, datosTabla);
        } else {
            exportarGeneral(builder, fInicio, fFin, datosTabla);
        }

        builder.guardar(destino);
    }

    private void exportarGeneral(ExcelReportBuilder builder, String fInicio, String fFin, List<?> datosTabla) {
        Map<String, Object> resumen = informesDAO.obtenerResumenGeneral(fInicio, fFin);

        // Tarjetas KPIs
        builder.agregarTarjetasKpis(
                List.of("Total Préstamos", "Devueltos Tarde", "Libro Más Prestado", "Docente Destacado"),
                List.of(
                        String.valueOf(resumen.getOrDefault("totalPrestamos", datosTabla != null ? datosTabla.size() : 0)),
                        String.valueOf(resumen.getOrDefault("totalPrestamosTarde", 0)),
                        String.valueOf(resumen.getOrDefault("libroTop", "N/A")),
                        String.valueOf(resumen.getOrDefault("docenteTop", "N/A"))
                )
        );

        // Tabla Detallada
        List<String> headers = List.of("Libro", "Estudiante", "Docente", "Motivo", "Fecha Préstamo", "Fecha Límite", "Fecha Devolución", "Estado", "Devuelto Tarde", "Días Atraso");
        List<List<Object>> filas = new ArrayList<>();

        if (datosTabla != null) {
            for (Object obj : datosTabla) {
                if (obj instanceof Prestamo p) {
                    filas.add(List.of(
                            p.getTituloLibro() != null ? p.getTituloLibro() : "",
                            p.getEstudiante() != null ? p.getEstudiante() : "",
                            p.getDocente() != null ? p.getDocente().getNombreCompleto() : "",
                            p.getMotivoPrestamo() != null ? p.getMotivoPrestamo().getNombre() : "",
                            p.getFecha_prestamo() != null ? p.getFecha_prestamo() : "",
                            p.getFecha_limite() != null ? p.getFecha_limite() : "",
                            p.getFecha_devolucion() != null ? p.getFecha_devolucion() : "Pendiente",
                            obtenerNombreEstado(p.getEstado()),
                            p.getEstado() == ReportConfig.ESTADO_DEVUELTO ? (p.getDevuelto_tarde() == 1 ? "Sí" : "No") : "--",
                            p.getEstado() == ReportConfig.ESTADO_DEVUELTO ? p.getDias_atraso() : 0
                    ));
                }
            }
        }

        builder.agregarTablaDetalle("REGISTRO DETALLADO DE PRÉSTAMOS", headers, filas);
    }

    private void exportarPorEstudiante(ExcelReportBuilder builder, Estudiante estudiante, String fInicio, String fFin, List<?> datosTabla) {
        String nombreEstudiante = estudiante != null ? estudiante.getNombreCompleto() : "Estudiante no seleccionado";
        int grado = estudiante != null ? estudiante.getGrado() : 0;
        int total = datosTabla != null ? datosTabla.size() : 0;

        builder.agregarTarjetasKpis(
                List.of("Estudiante", "Grado", "Total Préstamos", "Registros en Periodo"),
                List.of(nombreEstudiante, String.valueOf(grado), String.valueOf(total), String.valueOf(total))
        );

        List<String> headers = List.of("Libro", "Docente", "Motivo", "Fecha Préstamo", "Fecha Límite", "Fecha Devolución", "Estado", "Devuelto Tarde", "Días Atraso");
        List<List<Object>> filas = new ArrayList<>();

        if (datosTabla != null) {
            for (Object obj : datosTabla) {
                if (obj instanceof Prestamo p) {
                    filas.add(List.of(
                            p.getTituloLibro() != null ? p.getTituloLibro() : "",
                            p.getDocente() != null ? p.getDocente().getNombreCompleto() : "",
                            p.getMotivoPrestamo() != null ? p.getMotivoPrestamo().getNombre() : "",
                            p.getFecha_prestamo() != null ? p.getFecha_prestamo() : "",
                            p.getFecha_limite() != null ? p.getFecha_limite() : "",
                            p.getFecha_devolucion() != null ? p.getFecha_devolucion() : "Pendiente",
                            obtenerNombreEstado(p.getEstado()),
                            p.getDevuelto_tarde() == 1 ? "Sí" : "No",
                            p.getDias_atraso()
                    ));
                }
            }
        }

        builder.agregarTablaDetalle("HISTORIAL DE PRÉSTAMOS DEL ESTUDIANTE", headers, filas);
    }

    private void exportarDevueltosTarde(ExcelReportBuilder builder, Integer grado, String fInicio, String fFin, List<?> datosTabla) {
        int totalTarde = datosTabla != null ? datosTabla.size() : 0;
        builder.agregarTarjetasKpis(
                List.of("Préstamos con Retraso", "Grado Filtrado", "Rango Aplicado", "Estado"),
                List.of(String.valueOf(totalTarde), grado != null ? ("Grado " + grado) : "Todos", fInicio != null ? "Con filtro" : "Histórico", "Atención requerida")
        );

        List<String> headers = List.of("Libro", "Estudiante", "Docente", "Fecha Préstamo", "Fecha Límite", "Fecha Devolución", "Días de Atraso");
        List<List<Object>> filas = new ArrayList<>();

        if (datosTabla != null) {
            for (Object obj : datosTabla) {
                if (obj instanceof Prestamo p) {
                    filas.add(List.of(
                            p.getTituloLibro() != null ? p.getTituloLibro() : "",
                            p.getEstudiante() != null ? p.getEstudiante() : "",
                            p.getDocente() != null ? p.getDocente().getNombreCompleto() : "",
                            p.getFecha_prestamo() != null ? p.getFecha_prestamo() : "",
                            p.getFecha_limite() != null ? p.getFecha_limite() : "",
                            p.getFecha_devolucion() != null ? p.getFecha_devolucion() : "",
                            p.getDias_atraso()
                    ));
                }
            }
        }

        builder.agregarTablaDetalle("DETALLE DE PRÉSTAMOS DEVUELTOS FUERA DE PLAZO", headers, filas);
    }

    private void exportarPlataformaGeneral(ExcelReportBuilder builder, String fInicio, String fFin, List<?> datosTabla) {
        Map<String, Object> resumen = informesDAO.obtenerResumenPlataformaGeneral(fInicio, fFin);
        int totalMinutos = ((Number) resumen.getOrDefault("totalMinutos", 0)).intValue();

        builder.agregarTarjetasKpis(
                List.of("Total Usos", "Horas Totales", "Minutos Totales", "Docentes Distintos"),
                List.of(
                        String.valueOf(resumen.getOrDefault("totalRegistros", datosTabla != null ? datosTabla.size() : 0)),
                        String.valueOf(totalMinutos / 60),
                        String.valueOf(totalMinutos),
                        String.valueOf(resumen.getOrDefault("docentesDistintos", 0))
                )
        );

        List<String> headers = List.of("Docente", "Motivo de Uso", "Fecha", "Hora Inicio", "Hora Fin", "Minutos de Uso", "Grado");
        List<List<Object>> filas = new ArrayList<>();

        if (datosTabla != null) {
            for (Object obj : datosTabla) {
                if (obj instanceof RegistroPlataformaDetalle r) {
                    filas.add(List.of(
                            r.getDocente() != null ? r.getDocente() : "",
                            r.getMotivoUso() != null ? r.getMotivoUso() : "",
                            r.getFecha() != null ? r.getFecha() : "",
                            r.getHoraInicio() != null ? r.getHoraInicio() : "",
                            r.getHoraFin() != null ? r.getHoraFin() : "",
                            r.getTotalMinutos(),
                            r.getGrado() > 0 ? r.getGrado() : "--"
                    ));
                }
            }
        }

        builder.agregarTablaDetalle("REGISTROS INDIVIDUALES DE PLATAFORMA VIRTUAL", headers, filas);
    }

    private void exportarPlataformaDocente(ExcelReportBuilder builder, Docente docente, String fInicio, String fFin, List<?> datosTabla) {
        String nombreDocente = docente != null ? docente.getNombreCompleto() : "Docente";
        int totalRegistros = datosTabla != null ? datosTabla.size() : 0;
        int totalMinutos = 0;
        if (datosTabla != null) {
            for (Object obj : datosTabla) {
                if (obj instanceof RegistroPlataformaDetalle r) {
                    totalMinutos += r.getTotalMinutos();
                }
            }
        }

        builder.agregarTarjetasKpis(
                List.of("Docente", "Total Sesiones", "Horas Totales", "Minutos Totales"),
                List.of(nombreDocente, String.valueOf(totalRegistros), String.valueOf(totalMinutos / 60), String.valueOf(totalMinutos))
        );

        List<String> headers = List.of("Motivo de Uso", "Fecha", "Hora Inicio", "Hora Fin", "Minutos", "Grado");
        List<List<Object>> filas = new ArrayList<>();

        if (datosTabla != null) {
            for (Object obj : datosTabla) {
                if (obj instanceof RegistroPlataformaDetalle r) {
                    filas.add(List.of(
                            r.getMotivoUso() != null ? r.getMotivoUso() : "",
                            r.getFecha() != null ? r.getFecha() : "",
                            r.getHoraInicio() != null ? r.getHoraInicio() : "",
                            r.getHoraFin() != null ? r.getHoraFin() : "",
                            r.getTotalMinutos(),
                            r.getGrado() > 0 ? r.getGrado() : "--"
                    ));
                }
            }
        }

        builder.agregarTablaDetalle("HISTORIAL DE SESIONES DEL DOCENTE", headers, filas);
    }

    private void exportarInventario(ExcelReportBuilder builder, List<?> datosTabla) {
        int totalTitulos = datosTabla != null ? datosTabla.size() : 0;
        int totalEjemplares = 0;
        int librosCriticos = 0;

        if (datosTabla != null) {
            for (Object obj : datosTabla) {
                if (obj instanceof InventarioLibroDetalle i) {
                    totalEjemplares += i.getUnidades();
                    if (i.getRecomendadasComprar() > 0) {
                        librosCriticos++;
                    }
                }
            }
        }

        builder.agregarTarjetasKpis(
                List.of("Títulos Registrados", "Ejemplares Disponibles", "Títulos con Reposición", "Estado Catálogo"),
                List.of(String.valueOf(totalTitulos), String.valueOf(totalEjemplares), String.valueOf(librosCriticos), librosCriticos > 0 ? "Revisión necesaria" : "Óptimo")
        );

        List<String> headers = List.of("Libro", "Categoría", "Autor", "Editorial", "Ubicación", "Unidades Disponibles", "Préstamos Activos", "Stock Objetivo", "Recomendadas Comprar", "Estado Stock");
        List<List<Object>> filas = new ArrayList<>();

        if (datosTabla != null) {
            for (Object obj : datosTabla) {
                if (obj instanceof InventarioLibroDetalle i) {
                    filas.add(List.of(
                            i.getTitulo() != null ? i.getTitulo() : "",
                            i.getCategoria() != null ? i.getCategoria() : "",
                            i.getAutor() != null ? i.getAutor() : "",
                            i.getEditorial() != null ? i.getEditorial() : "",
                            i.getUbicacion() != null ? i.getUbicacion() : "",
                            i.getUnidades(),
                            i.getPrestamosActivos(),
                            i.getStockObjetivo(),
                            i.getRecomendadasComprar(),
                            i.getEstadoStock()
                    ));
                }
            }
        }

        builder.agregarTablaDetalle("CATÁLOGO E INVENTARIO PARA REABASTECIMIENTO", headers, filas);
    }

    private String obtenerNombreEstado(int estado) {
        return switch (estado) {
            case 0 -> "Prestado";
            case 1 -> "Devuelto";
            case 2 -> "Pendiente";
            default -> "Desconocido";
        };
    }
}
