package services;

import database.EstudiantesDAO;
import model.Estudiante;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstudianteService {

    private static final List<String> ESTRUCTURA_CSV = List.of(
            "identificacion", "grado", "apellido_1", "apellido_2", "nombre_1", "nombre_2", "genero");

    private final EstudiantesDAO estudiantesDAO;

    public EstudianteService() {
        this.estudiantesDAO = new EstudiantesDAO();
    }

    public ParseResult parsearArchivoCsv(File file) throws IOException, IllegalArgumentException {
        List<String> lineas = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV est vaco");
        }

        int indiceEncabezado = -1;
        List<String> encabezado = List.of();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isEmpty()) {
                continue;
            }
            encabezado = normalizarEncabezados(parseCsvLine(linea));
            indiceEncabezado = i;
            break;
        }

        if (indiceEncabezado < 0 || !estructuraValida(encabezado)) {
            throw new IllegalArgumentException("Estructura CSV invlida. Debe ser exactamente:\n" + String.join(", ", ESTRUCTURA_CSV));
        }

        List<Estudiante> estudiantesPendientesCsv = new ArrayList<>();
        int errores = 0;

        for (int i = indiceEncabezado + 1; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isEmpty()) {
                continue;
            }

            try {
                List<String> columnas = parseCsvLine(linea);
                if (columnas.size() != ESTRUCTURA_CSV.size()) {
                    errores++;
                    continue;
                }

                long identificacion = Long.parseLong(columnas.get(0).trim());
                int grado = Integer.parseInt(columnas.get(1).trim());
                String nombre2 = columnas.get(5) == null ? "" : columnas.get(5).trim();

                Estudiante estudiante = new Estudiante(
                        identificacion,
                        grado,
                        columnas.get(2),
                        columnas.get(3),
                        columnas.get(4),
                        nombre2,
                        columnas.get(6));

                estudiantesPendientesCsv.add(estudiante);
            } catch (Exception ex) {
                errores++;
            }
        }
        
        return new ParseResult(estudiantesPendientesCsv, errores);
    }

    private List<String> normalizarEncabezados(List<String> encabezados) {
        List<String> salida = new ArrayList<>();
        for (int i = 0; i < encabezados.size(); i++) {
            String valor = encabezados.get(i);
            if (i == 0) {
                valor = valor.replace("\uFEFF", "");
            }
            salida.add(valor.trim().toLowerCase());
        }
        return salida;
    }

    private boolean estructuraValida(List<String> encabezados) {
        if (encabezados.size() != ESTRUCTURA_CSV.size()) {
            return false;
        }
        for (int i = 0; i < ESTRUCTURA_CSV.size(); i++) {
            if (!ESTRUCTURA_CSV.get(i).equals(encabezados.get(i))) {
                return false;
            }
        }
        return true;
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }


    public CsvResult procesarYGuardarLote(List<Estudiante> pendientesCsv) {
        List<Estudiante> existentes = estudiantesDAO.obtenerEstudiantes();
        Map<Long, Estudiante> mapaExistentes = new HashMap<>();
        for (Estudiante e : existentes) {
            mapaExistentes.put(e.getIdentificacion(), e);
        }

        List<Estudiante> aInsertar = new ArrayList<>();
        List<Estudiante> aActualizar = new ArrayList<>();
        int sinCambios = 0;

        for (Estudiante csvEst : pendientesCsv) {
            Estudiante existente = mapaExistentes.get(csvEst.getIdentificacion());
            if (existente == null) {
                aInsertar.add(csvEst);
            } else {
                boolean cambio = existente.getGrado() != csvEst.getGrado()
                        || !textoSeguro(existente.getApellido_1()).equals(textoSeguro(csvEst.getApellido_1()))
                        || !textoSeguro(existente.getApellido_2()).equals(textoSeguro(csvEst.getApellido_2()))
                        || !textoSeguro(existente.getNombre_1()).equals(textoSeguro(csvEst.getNombre_1()))
                        || !textoSeguro(existente.getNombre_2()).equals(textoSeguro(csvEst.getNombre_2()))
                        || !textoSeguro(existente.getGenero()).equals(textoSeguro(csvEst.getGenero()));

                if (cambio) {
                    csvEst.setId(existente.getId());
                    aActualizar.add(csvEst);
                } else {
                    sinCambios++;
                }
            }
        }

        boolean exito = false;
        if (!aInsertar.isEmpty() || !aActualizar.isEmpty()) {
            exito = estudiantesDAO.procesarLote(aInsertar, aActualizar);
        } else {
            exito = true; 
        }

        return new CsvResult(exito, aInsertar.size(), aActualizar.size(), sinCambios, 0);
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase();
    }

    private String textoSeguro(String valor) {
        return normalizarTexto(valor);
    }

    public static class ParseResult {
        public final List<Estudiante> estudiantes;
        public final int errores;
        public ParseResult(List<Estudiante> estudiantes, int errores) {
            this.estudiantes = estudiantes;
            this.errores = errores;
        }
    }

    public static class CsvResult {
        public final boolean exito;
        public final int insertados;
        public final int actualizados;
        public final int sinCambios;
        public final int errores;

        public CsvResult(boolean exito, int insertados, int actualizados, int sinCambios, int errores) {
            this.exito = exito;
            this.insertados = insertados;
            this.actualizados = actualizados;
            this.sinCambios = sinCambios;
            this.errores = errores;
        }
    }
}