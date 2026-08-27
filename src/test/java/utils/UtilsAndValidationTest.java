package utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsAndValidationTest {

    @Test
    @DisplayName("Verifica la conversión y comparación de formatos de fecha")
    void testFechasUtil() {
        LocalDate fecha = LocalDate.of(2026, 8, 27);
        String iso = Fechas.convertirAISO(fecha);
        assertThat(iso).isEqualTo("2026-08-27");

        String ui = Fechas.convertirAUI(iso);
        assertThat(ui).isEqualTo("27/08/2026");

        assertThat(Fechas.esDespues("2026-08-28", "2026-08-27")).isTrue();
        assertThat(Fechas.esDespues("2026-08-26", "2026-08-27")).isFalse();
        assertThat(Fechas.esDespues("2026-08-27", "2026-08-27")).isFalse();

        assertThat(Fechas.fechaActualISO()).isNotEmpty();
    }

    @Test
    @DisplayName("Verifica el generador de intervalos de horas para la biblioteca virtual")
    void testGeneradorHoras() {
        List<String> horas = GeneradorHoras.generarHoras();
        assertThat(horas).isNotEmpty();
        assertThat(horas).contains("07:00", "10:00", "14:00");
    }

    @Test
    @DisplayName("Verifica que las rutas de recursos FXML y CSS existan y sean válidas")
    void testPathsValidos() {
        assertThat(getClass().getResource(Paths.LOGIN)).isNotNull();
        assertThat(getClass().getResource(Paths.DASHBOARD_BIBLIOTECARIO)).isNotNull();
        assertThat(getClass().getResource(Paths.DASHBOARD_RECTORIA)).isNotNull();
        assertThat(getClass().getResource(Paths.INVENTARIO_RECTORIA)).isNotNull();
        assertThat(getClass().getResource(Paths.INFORMES_RECTORIA)).isNotNull();
        assertThat(getClass().getResource(Paths.ESTADISTICAS_RECTORIA)).isNotNull();
    }
}
