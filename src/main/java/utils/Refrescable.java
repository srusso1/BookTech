package utils;

public interface Refrescable {
    /**
     * Llamado por ManagerView cuando una vista en caché vuelve a ser mostrada.
     * Úsalo para recargar datos desde la base de datos de manera asíncrona.
     */
    void refresh();
}
