package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.ResourceDeps;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserCompilerTest {
    @Test
    void testPersistentProperties() {
        final Properties persistentProperties = new Properties();

        final Coordinates coordinates1 = CommonInputs.tigerCoordinates();
        coordinates1.savePersistentProperties(persistentProperties);
        final ParserInput parserInput1 = ParserInput.builder()
            .coordinates(coordinates1)
            .build();
        assertEquals("TigerParseTable", parserInput1.genTableClass());
        assertEquals("TigerParser", parserInput1.genParserClass());
        parserInput1.savePersistentProperties(persistentProperties);

        final Coordinates coordinates2 = CommonInputs.tigerCoordinatesBuilder()
            .name("Tigerr")
            .withPersistentProperties(persistentProperties)
            .build();
        final ParserInput parserInput2 = ParserInput.builder()
            .coordinates(coordinates2)
            .withPersistentProperties(persistentProperties)
            .build();
        assertEquals("TigerParseTable", parserInput2.genTableClass());
        assertEquals("TigerParser", parserInput2.genParserClass());
    }

    @Test
    void testCompiler() throws IOException {
        final Coordinates coordinates = CommonInputs.tigerCoordinates();
        final ParserInput input = ParserInput.builder()
            .coordinates(coordinates)
            .build();
        final ParserCompiler compiler = ParserCompiler.fromClassLoaderResources();
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSResource baseDir = new FSResource(fileSystem.getPath("src/main/java"));
        final Charset charset = StandardCharsets.UTF_8;
        final ResourceDeps resourceDeps = compiler.compile(input, baseDir, charset);
        final HierarchicalResource packageDir = compiler.getPackageDir(input, baseDir);
        assertTrue(packageDir.exists());
        final HierarchicalResource parseTableFile = compiler.getParseTableFile(packageDir);
        assertTrue(parseTableFile.exists());
        assertTrue(resourceDeps.providedResources().contains(parseTableFile));
        assertTrue(parseTableFile.readString(charset).contains("class TigerParseTable"));
        final HierarchicalResource parserFile = compiler.getParserFile(packageDir);
        assertTrue(parserFile.exists());
        assertTrue(resourceDeps.providedResources().contains(parserFile));
        assertTrue(parserFile.readString(charset).contains("class TigerParser"));
        final HierarchicalResource parserFactoryFile = compiler.getParserFactoryFile(packageDir);
        assertTrue(parserFactoryFile.exists());
        assertTrue(resourceDeps.providedResources().contains(parserFactoryFile));
        assertTrue(parserFactoryFile.readString(charset).contains("class TigerParserFactory"));
    }
}
