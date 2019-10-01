package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.fs.FSResource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompilerTest {
    @Test
    void testPersistentProperties() {
        final Properties persistentProperties = new Properties();

        final Coordinates coordinates1 = Coordinates.builder()
            .groupId("org.metaborg")
            .id("tiger")
            .packageId("mb.tiger")
            .name("Tiger")
            .build();
        assertEquals("Tiger", coordinates1.classSuffix());
        coordinates1.savePersistentProperties(persistentProperties);

        final Parser.Input parserInput1 = Parser.Input.builder()
            .coordinates(coordinates1)
            .build();
        assertEquals("TigerParseTable", parserInput1.genTableClass());
        assertEquals("TigerParser", parserInput1.genParserClass());
        parserInput1.savePersistentProperties(persistentProperties);

        final Coordinates basicInput2 = Coordinates.builder()
            .groupId("org.metaborg")
            .id("tiger")
            .packageId("mb.tiger")
            .name("Tigerr")
            .withPersistentProperties(persistentProperties)
            .build();
        assertEquals("Tiger", basicInput2.classSuffix());

        final Parser.Input parserInput2 = Parser.Input.builder()
            .coordinates(basicInput2)
            .withPersistentProperties(persistentProperties)
            .build();
        assertEquals("TigerParseTable", parserInput2.genTableClass());
        assertEquals("TigerParser", parserInput2.genParserClass());
    }

    @Test
    void testParserCompiler() throws IOException {
        final Coordinates coordinates = Coordinates.builder()
            .groupId("org.metaborg")
            .id("tiger")
            .packageId("mb.tiger")
            .name("Tiger")
            .build();
        final Parser.Input parserInput = Parser.Input.builder()
            .coordinates(coordinates)
            .build();
        final Parser.Compiler compiler = Parser.Compiler.fromClassLoaderResources();

        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSResource baseDir = new FSResource(fileSystem.getPath("src/main/java"));
        compiler.compile(parserInput, baseDir, StandardCharsets.UTF_8);
    }
}
