package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generador de horas en formato HH:mm para Spinners
 * Rango: 6:00 (6am) a 14:00 (2pm)
 * Incremento: 10 minutos
 */
public class GeneradorHoras {

    private GeneradorHoras() {
    }

    /**
     * Genera lista de horas desde 6:00 hasta 14:00 con incremento de 10 minutos
     * @return Lista de strings en formato HH:mm
     */
    public static List<String> generarHoras() {
        List<String> horas = new ArrayList<>();
        
        for (int hora = 6; hora <= 14; hora++) {
            for (int minuto = 0; minuto < 60; minuto += 10) {
                String horarioFormato = String.format("%02d:%02d", hora, minuto);
                horas.add(horarioFormato);
            }
        }
        
        return horas;
    }

    /**
     * Calcula la diferencia de horas entre dos horarios en formato HH:mm
     * @param horaInicio Hora de inicio en formato HH:mm (ej: "08:00")
     * @param horaFin Hora de fin en formato HH:mm (ej: "10:30")
     * @return Número de horas (puede incluir decimales, ej: 2.5)
     */
    public static double calcularDiferencia(String horaInicio, String horaFin) {
        try {
            String[] partsInicio = horaInicio.split(":");
            String[] partsFin = horaFin.split(":");
            
            int horaI = Integer.parseInt(partsInicio[0]);
            int minutoI = Integer.parseInt(partsInicio[1]);
            
            int horaF = Integer.parseInt(partsFin[0]);
            int minutoF = Integer.parseInt(partsFin[1]);
            
            int totalMinutosInicio = horaI * 60 + minutoI;
            int totalMinutosFin = horaF * 60 + minutoF;
            
            int diferencia = totalMinutosFin - totalMinutosInicio;
            
            if (diferencia < 0) {
                return 0; // Si la hora fin es antes que la inicio
            }
            
            return diferencia / 60.0; // Convertir a horas
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Calcula la diferencia entre dos horarios en minutos.
     * Es la base exacta para mostrar y guardar duraciones sin perder precisión.
     */
    public static int calcularDiferenciaMinutos(String horaInicio, String horaFin) {
        try {
            String[] partsInicio = horaInicio.split(":");
            String[] partsFin = horaFin.split(":");

            int horaI = Integer.parseInt(partsInicio[0]);
            int minutoI = Integer.parseInt(partsInicio[1]);

            int horaF = Integer.parseInt(partsFin[0]);
            int minutoF = Integer.parseInt(partsFin[1]);

            int totalMinutosInicio = horaI * 60 + minutoI;
            int totalMinutosFin = horaF * 60 + minutoF;

            return Math.max(totalMinutosFin - totalMinutosInicio, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Formatea horas con decimales a formato legible (ej: 2.5 -> "2h 30min")
     * @param horas Horas en formato decimal
     * @return String formateado
     */
    public static String formatearHoras(double horas) {
        int totalMinutos = (int) Math.round(horas * 60);
        return formatearMinutos(totalMinutos);
    }

    /**
     * Formatea una cantidad total de minutos a texto legible (ej: 270 -> "4h 30min").
     */
    public static String formatearMinutos(int totalMinutos) {
        int horasEnteras = totalMinutos / 60;
        int minutos = totalMinutos % 60;
        
        if (minutos == 0) {
            return horasEnteras + "h";
        }
        
        return horasEnteras + "h " + minutos + "min";
    }
}

