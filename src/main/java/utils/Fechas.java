package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Fechas {
    public static String fechaActual(){
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static int compararFechas(String fecha1, String fecha2) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate f1 = LocalDate.parse(fecha1, formatter);
        LocalDate f2 = LocalDate.parse(fecha2, formatter);

        return f1.compareTo(f2); // -1 fecha1 es ANTES que fecha2, 0 son iguales, 1 fecha2 es ANTES que fecha1
    }

    public static boolean esDespues(String f1, String f2) {
        return compararFechas(f1, f2) > 0;
    }

    public static boolean esAntes(String f1, String f2) {
        return compararFechas(f1, f2) < 0;
    }


}
