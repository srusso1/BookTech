package model;

/**
 * DTO para el informe de inventario con foco en reposicion de libros.
 */
public class InventarioLibroDetalle {

    private final int idLibro;
    private final String titulo;
    private final String categoria;
    private final String autor;
    private final String editorial;
    private final String ubicacion;
    private final int unidades;
    private final int prestamosActivos;
    private final int prestamosHistoricos;
    private final int stockObjetivo;
    private final int recomendadasComprar;

    public InventarioLibroDetalle(
            int idLibro,
            String titulo,
            String categoria,
            String autor,
            String editorial,
            String ubicacion,
            int unidades,
            int prestamosActivos,
            int prestamosHistoricos,
            int stockObjetivo,
            int recomendadasComprar
    ) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.categoria = categoria;
        this.autor = autor;
        this.editorial = editorial;
        this.ubicacion = ubicacion;
        this.unidades = unidades;
        this.prestamosActivos = prestamosActivos;
        this.prestamosHistoricos = prestamosHistoricos;
        this.stockObjetivo = stockObjetivo;
        this.recomendadasComprar = recomendadasComprar;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getAutor() {
        return autor;
    }

    public String getEditorial() {
        return editorial;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public int getUnidades() {
        return unidades;
    }

    public int getPrestamosActivos() {
        return prestamosActivos;
    }

    public int getPrestamosHistoricos() {
        return prestamosHistoricos;
    }

    public int getStockObjetivo() {
        return stockObjetivo;
    }

    public int getRecomendadasComprar() {
        return recomendadasComprar;
    }

    public String getEstadoStock() {
        if (recomendadasComprar >= 3 || unidades <= 1) {
            return "Critico";
        }
        if (recomendadasComprar > 0) {
            return "Bajo";
        }
        return "Adecuado";
    }
}

