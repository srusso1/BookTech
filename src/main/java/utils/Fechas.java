package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Fechas {
    public static String fechaActual(){
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
