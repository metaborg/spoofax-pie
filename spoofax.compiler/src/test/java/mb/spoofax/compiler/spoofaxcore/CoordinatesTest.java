package mb.spoofax.compiler.spoofaxcore;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoordinatesTest {
    @Test
    void testPersistentProperties() {
        final Properties persistentProperties = new Properties();

        final Coordinates coordinates1 = CommonInputs.tigerCoordinatesBuilder()
            .name("Tiger")
            .build();
        assertEquals("Tiger", coordinates1.classSuffix());
        coordinates1.savePersistentProperties(persistentProperties);

        final Coordinates coordinates2 = CommonInputs.tigerCoordinatesBuilder()
            .name("Tigerr")
            .withPersistentProperties(persistentProperties)
            .build();
        assertEquals("Tiger", coordinates2.classSuffix());
    }
}
