package mb.spoofax.compiler.spoofaxcore;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SharedTest {
    @Test
    void testPersistentProperties() {
        final Properties persistentProperties = new Properties();

        final Shared shared1 = CommonInputs.tigerShared();
        assertEquals("Tiger", shared1.classSuffix());
        shared1.savePersistentProperties(persistentProperties);

        final Shared shared2 = CommonInputs.tigerSharedBuilder()
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();
        // Should not affect class suffix.
        assertEquals("Tiger", shared2.classSuffix());
    }
}
