package mb.spoofax.generator;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpoofaxCoreGeneratorTest {
    @Test
    public void test() {
        final SpoofaxCoreGeneratorInput input1 = SpoofaxCoreGeneratorInput.builder()
            .groupId("org.metaborg")
            .id("tiger")
            .packageId("mb.tiger")
            .name("Tiger")
            .build();
        assertEquals("Tiger", input1.classSuffix());
        assertEquals("TigerParseTable", input1.parseTableGeneratedClass());
        final Properties persistentProperties = input1.savePersistentProperties();

        final SpoofaxCoreGeneratorInput input2 = SpoofaxCoreGeneratorInput.builder()
            .groupId("org.metaborg")
            .id("tiger")
            .packageId("mb.tiger")
            .name("Tigerr")
            .withPersistentProperties(persistentProperties)
            .build();
        assertEquals("Tiger", input2.classSuffix());
        assertEquals("TigerParseTable", input2.parseTableGeneratedClass());
    }
}
