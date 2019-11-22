package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofaxcore.util.TigerInputs;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.spoofaxcore.util.JavaParser;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.JavaProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test void testPersistentProperties() {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Properties persistentProperties = new Properties();

        final Shared shared1 = TigerInputs.shared(baseDirectory);
        final JavaProject languageProject1 = TigerInputs.languageProject(shared1).project();
        final Parser.Input parserCompilerInput1 = TigerInputs.parser(shared1, languageProject1);
        assertEquals("TigerParseTable", parserCompilerInput1.genTableClass());
        assertEquals("TigerParser", parserCompilerInput1.genParserClass());
        shared1.savePersistentProperties(persistentProperties);
        parserCompilerInput1.savePersistentProperties(persistentProperties);

        final Shared shared2 = TigerInputs.sharedBuilder(baseDirectory)
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();
        final JavaProject languageProject2 = TigerInputs.languageProject(shared2).project();
        final Parser.Input parserCompilerInput2 = TigerInputs.parserBuilder(shared2, languageProject2)
            .withPersistentProperties(persistentProperties)
            .build();
        // Should not affect generated class names.
        assertEquals("TigerParseTable", parserCompilerInput2.genTableClass());
        assertEquals("TigerParser", parserCompilerInput2.genParserClass());
    }

    @ParameterizedTest @EnumSource(value = ClassKind.class, names = {"Manual", "Extended"})
    void testManualRequiresClasses(ClassKind classKind) {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = TigerInputs.shared(baseDirectory);
        final JavaProject languageProject = TigerInputs.languageProject(shared).project();
        assertThrows(IllegalArgumentException.class, () -> {
            TigerInputs.parserBuilder(shared, languageProject)
                .classKind(classKind)
                .build(); // Class kind is Manual or Extended, but manual class names were not set: check fails.
        });

        TigerInputs.parserBuilder(shared, languageProject)
            .classKind(classKind)
            .manualParserClass("MyParser")
            .manualFactoryClass("MyParserFactory")
            .build();
    }

    @Test void testCompilerDefault() throws IOException {
        final JavaParser javaParser = new JavaParser();
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = TigerInputs.shared(baseDirectory);
        final JavaProject languageProject = TigerInputs.languageProject(shared).project();
        final Parser.Input input = TigerInputs.parser(shared, languageProject);

        final Parser compiler = Parser.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final Parser.Output output = compiler.compile(input, charset);

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genSourcesJavaDirectory());
        assertTrue(genSourcesJavaDirectory.exists());

        final FileAssertions genParseTableFile = new FileAssertions(resourceService.getHierarchicalResource(output.genTableFile()));
        genParseTableFile.assertName("TigerParseTable.java");
        genParseTableFile.assertExists();
        genParseTableFile.assertContains("class TigerParseTable");
        genParseTableFile.assertJavaParses(javaParser);

        final FileAssertions genParserFile = new FileAssertions(resourceService.getHierarchicalResource(output.genParserFile()));
        genParserFile.assertName("TigerParser.java");
        genParserFile.assertExists();
        genParserFile.assertContains("class TigerParser");
        genParserFile.assertJavaParses(javaParser);

        final FileAssertions genParserFactoryFile = new FileAssertions(resourceService.getHierarchicalResource(output.genFactoryFile()));
        genParserFactoryFile.assertName("TigerParserFactory.java");
        genParserFactoryFile.assertExists();
        genParserFactoryFile.assertContains("class TigerParserFactory");
        genParserFactoryFile.assertJavaParses(javaParser);
    }

    @Test void testCompilerManual() throws IOException {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = TigerInputs.shared(baseDirectory);
        final JavaProject languageProject = TigerInputs.languageProject(shared).project();
        final Parser.Input input = TigerInputs.parserBuilder(shared, languageProject)
            .classKind(ClassKind.Manual)
            .manualParserClass("MyParser")
            .manualFactoryClass("MyParserFactory")
            .build();

        final Parser compiler = Parser.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final Parser.Output output = compiler.compile(input, charset);

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genSourcesJavaDirectory());
        assertFalse(genSourcesJavaDirectory.exists());

        final HierarchicalResource genParseTableFile = resourceService.getHierarchicalResource(output.genTableFile());
        assertFalse(genParseTableFile.exists());

        final HierarchicalResource genParserFile = resourceService.getHierarchicalResource(output.genParserFile());
        assertFalse(genParserFile.exists());

        final HierarchicalResource genParserFactoryFile = resourceService.getHierarchicalResource(output.genFactoryFile());
        assertFalse(genParserFactoryFile.exists());
    }
}
