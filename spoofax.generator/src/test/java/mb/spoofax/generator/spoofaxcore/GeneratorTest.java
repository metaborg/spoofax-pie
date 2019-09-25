package mb.spoofax.generator.spoofaxcore;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeneratorTest {
    @Test
    void testPersistentProperties() {
        final Properties persistentProperties = new Properties();

        final BasicInput basicInput1 = BasicInput.builder()
            .groupId("org.metaborg")
            .id("tiger")
            .packageId("mb.tiger")
            .name("Tiger")
            .build();
        assertEquals("Tiger", basicInput1.classSuffix());
        basicInput1.savePersistentProperties(persistentProperties);

        final ParserInput parserInput1 = ParserInput.builder()
            .basicInput(basicInput1)
            .build();
        assertEquals("TigerParseTable", parserInput1.tableGenClass());
        assertEquals("TigerParser", parserInput1.parserGenClass());
        parserInput1.savePersistentProperties(persistentProperties);

        final BasicInput basicInput2 = BasicInput.builder()
            .groupId("org.metaborg")
            .id("tiger")
            .packageId("mb.tiger")
            .name("Tigerr")
            .withPersistentProperties(persistentProperties)
            .build();
        assertEquals("Tiger", basicInput2.classSuffix());

        final ParserInput parserInput2 = ParserInput.builder()
            .basicInput(basicInput2)
            .withPersistentProperties(persistentProperties)
            .build();
        assertEquals("TigerParseTable", parserInput2.tableGenClass());
        assertEquals("TigerParser", parserInput2.parserGenClass());
    }
}
