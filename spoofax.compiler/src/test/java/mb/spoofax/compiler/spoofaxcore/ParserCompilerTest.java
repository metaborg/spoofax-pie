package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.ResourceDependencies;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ParserCompilerTest {
    @Test
    void testPersistentProperties() {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("tiger"));

        final Properties persistentProperties = new Properties();

        final Shared shared1 = CommonInputs.tigerShared(baseDirectory);
        final JavaProject languageProject1 = CommonInputs.tigerLanguageProjectCompilerInput(shared1).project();
        final ParserCompilerInput parserCompilerInput1 = CommonInputs.tigerParserCompilerInput(shared1, languageProject1);
        assertEquals("TigerParseTable", parserCompilerInput1.genTableClass());
        assertEquals("TigerParser", parserCompilerInput1.genParserClass());
        shared1.savePersistentProperties(persistentProperties);
        parserCompilerInput1.savePersistentProperties(persistentProperties);

        final Shared shared2 = CommonInputs.tigerSharedBuilder(baseDirectory)
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();
        final JavaProject languageProject2 = CommonInputs.tigerLanguageProjectCompilerInput(shared2).project();
        final ParserCompilerInput parserCompilerInput2 = CommonInputs.tigerParserCompilerInputBuilder(shared2, languageProject2)
            .withPersistentProperties(persistentProperties)
            .build();
        // Should not affect generated class names.
        assertEquals("TigerParseTable", parserCompilerInput2.genTableClass());
        assertEquals("TigerParser", parserCompilerInput2.genParserClass());
    }

    @ParameterizedTest
    @EnumSource(value = ClassKind.class, names = {"Manual", "Extended"})
    void testManualRequiresClasses(ClassKind classKind) {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("tiger"));

        final Shared shared = CommonInputs.tigerShared(baseDirectory);
        final JavaProject languageProject = CommonInputs.tigerLanguageProjectCompilerInput(shared).project();
        assertThrows(IllegalArgumentException.class, () -> {
            CommonInputs.tigerParserCompilerInputBuilder(shared, languageProject)
                .classKind(classKind)
                .build(); // Class kind is Manual or Extended, but manual class names were not set: check fails.
        });

        CommonInputs.tigerParserCompilerInputBuilder(shared, languageProject)
            .classKind(classKind)
            .manualParserClass("MyParser")
            .manualParserFactoryClass("MyParserFactory")
            .build();
    }

    @Test
    void testCompilerDefault() throws IOException {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("tiger"));

        final Shared shared = CommonInputs.tigerShared(baseDirectory);
        final JavaProject languageProject = CommonInputs.tigerLanguageProjectCompilerInput(shared).project();
        final ParserCompilerInput input = CommonInputs.tigerParserCompilerInput(shared, languageProject);

        final ParserCompiler compiler = ParserCompiler.fromClassLoaderResources(resourceService);
        final FSResource baseDir = new FSResource(fileSystem.getPath("src/main/java"));
        final Charset charset = StandardCharsets.UTF_8;
        final ResourceDependencies resourceDependencies = compiler.compile(input, charset);

        final HierarchicalResource packageDirectory = compiler.getPackageDirectory(input);
        assertTrue(packageDirectory.exists());

        final HierarchicalResource genParseTableFile = compiler.getGenParseTableFile(input);
        assertEquals("TigerParseTable.java", genParseTableFile.getLeaf());
        assertTrue(genParseTableFile.exists());
        assertTrue(resourceDependencies.providedResources().contains(genParseTableFile));
        assertTrue(genParseTableFile.readString(charset).contains("class TigerParseTable"));

        final HierarchicalResource genParserFile = compiler.getParserFile(input);
        assertEquals("TigerParser.java", genParserFile.getLeaf());
        assertTrue(genParserFile.exists());
        assertTrue(resourceDependencies.providedResources().contains(genParserFile));
        assertTrue(genParserFile.readString(charset).contains("class TigerParser"));

        final HierarchicalResource genParserFactoryFile = compiler.getParserFactoryFile(input);
        assertEquals("TigerParserFactory.java", genParserFactoryFile.getLeaf());
        assertTrue(genParserFactoryFile.exists());
        assertTrue(resourceDependencies.providedResources().contains(genParserFactoryFile));
        assertTrue(genParserFactoryFile.readString(charset).contains("class TigerParserFactory"));
    }

    @Test
    void testCompilerManual() throws IOException {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("tiger"));

        final Shared shared = CommonInputs.tigerShared(baseDirectory);
        final JavaProject languageProject = CommonInputs.tigerLanguageProjectCompilerInput(shared).project();
        final ParserCompilerInput input = CommonInputs.tigerParserCompilerInputBuilder(shared, languageProject)
            .classKind(ClassKind.Manual)
            .manualParserClass("MyParser")
            .manualParserFactoryClass("MyParserFactory")
            .build();

        final ParserCompiler compiler = ParserCompiler.fromClassLoaderResources(resourceService);
        final FSResource baseDir = new FSResource(fileSystem.getPath("src/main/java"));
        final Charset charset = StandardCharsets.UTF_8;
        final ResourceDependencies resourceDependencies = compiler.compile(input, charset);
        assertTrue(resourceDependencies.requiredResources().isEmpty());

        final HierarchicalResource packageDir = compiler.getPackageDirectory(input);
        assertFalse(packageDir.exists());

        final HierarchicalResource genParseTableFile = compiler.getGenParseTableFile(input);
        assertFalse(genParseTableFile.exists());

        final HierarchicalResource genParserFile = compiler.getParserFile(input);
        assertFalse(genParserFile.exists());

        final HierarchicalResource genParserFactoryFile = compiler.getParserFactoryFile(input);
        assertFalse(genParserFactoryFile.exists());
    }
}
