package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResponsiveManagerTest {

    @Test
    void testResponsiveManagerInit() {
        assertDoesNotThrow(() -> ResponsiveManager.aplicarEscena(null));
    }
}
