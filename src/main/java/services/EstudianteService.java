package services;

import database.EstudiantesDAO;
import model.Estudiante;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstudianteService {

    private final EstudiantesDAO estudiantesDAO;

    public EstudianteService() {
        this.estudiantesDAO = new EstudiantesDAO();
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
            exito = true; // Nothing to do, but not a failure.
        }

        return new CsvResult(exito, aInsertar.size(), aActualizar.size(), sinCambios, 0);
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase();
    }

    private String textoSeguro(String valor) {
        return normalizarTexto(valor);
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
