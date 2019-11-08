package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofaxcore.util.CommonInputs;
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

class ParserCompilerTest {
    @Test void testPersistentProperties() {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Properties persistentProperties = new Properties();

        final Shared shared1 = CommonInputs.tigerShared(baseDirectory);
        final JavaProject languageProject1 = CommonInputs.tigerLanguageProjectCompilerInput(shared1).project();
        final ParserCompiler.Input parserCompilerInput1 = CommonInputs.tigerParserCompilerInput(shared1, languageProject1);
        assertEquals("TigerParseTable", parserCompilerInput1.genParseTableClass());
        assertEquals("TigerParser", parserCompilerInput1.genParserClass());
        shared1.savePersistentProperties(persistentProperties);
        parserCompilerInput1.savePersistentProperties(persistentProperties);

        final Shared shared2 = CommonInputs.tigerSharedBuilder(baseDirectory)
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();
        final JavaProject languageProject2 = CommonInputs.tigerLanguageProjectCompilerInput(shared2).project();
        final ParserCompiler.Input parserCompilerInput2 = CommonInputs.tigerParserCompilerInputBuilder(shared2, languageProject2)
            .withPersistentProperties(persistentProperties)
            .build();
        // Should not affect generated class names.
        assertEquals("TigerParseTable", parserCompilerInput2.genParseTableClass());
        assertEquals("TigerParser", parserCompilerInput2.genParserClass());
    }

    @ParameterizedTest @EnumSource(value = ClassKind.class, names = {"Manual", "Extended"})
    void testManualRequiresClasses(ClassKind classKind) {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

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

    @Test void testCompilerDefault() throws IOException {
        final JavaParser javaParser = new JavaParser();
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = CommonInputs.tigerShared(baseDirectory);
        final JavaProject languageProject = CommonInputs.tigerLanguageProjectCompilerInput(shared).project();
        final ParserCompiler.Input input = CommonInputs.tigerParserCompilerInput(shared, languageProject);

        final ParserCompiler compiler = ParserCompiler.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final ParserCompiler.Output output = compiler.compile(input, charset);

        final HierarchicalResource packageDirectory = resourceService.getHierarchicalResource(output.packageDirectory());
        assertTrue(packageDirectory.exists());

        final FileAssertions genParseTableFile = new FileAssertions(resourceService.getHierarchicalResource(output.genParseTableFile()));
        genParseTableFile.assertName("TigerParseTable.java");
        genParseTableFile.assertExists();
        genParseTableFile.assertContains("class TigerParseTable");
        genParseTableFile.assertJavaParses(javaParser);

        final FileAssertions genParserFile = new FileAssertions(resourceService.getHierarchicalResource(output.genParserFile()));
        genParserFile.assertName("TigerParser.java");
        genParserFile.assertExists();
        genParserFile.assertContains("class TigerParser");
        genParserFile.assertJavaParses(javaParser);

        final FileAssertions genParserFactoryFile = new FileAssertions(resourceService.getHierarchicalResource(output.genParserFactoryFile()));
        genParserFactoryFile.assertName("TigerParserFactory.java");
        genParserFactoryFile.assertExists();
        genParserFactoryFile.assertContains("class TigerParserFactory");
        genParserFactoryFile.assertJavaParses(javaParser);
    }

    @Test void testCompilerManual() throws IOException {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = CommonInputs.tigerShared(baseDirectory);
        final JavaProject languageProject = CommonInputs.tigerLanguageProjectCompilerInput(shared).project();
        final ParserCompiler.Input input = CommonInputs.tigerParserCompilerInputBuilder(shared, languageProject)
            .classKind(ClassKind.Manual)
            .manualParserClass("MyParser")
            .manualParserFactoryClass("MyParserFactory")
            .build();

        final ParserCompiler compiler = ParserCompiler.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final ParserCompiler.Output output = compiler.compile(input, charset);

        final HierarchicalResource packageDir = resourceService.getHierarchicalResource(output.packageDirectory());
        assertFalse(packageDir.exists());

        final HierarchicalResource genParseTableFile = resourceService.getHierarchicalResource(output.genParseTableFile());
        assertFalse(genParseTableFile.exists());

        final HierarchicalResource genParserFile = resourceService.getHierarchicalResource(output.genParserFile());
        assertFalse(genParserFile.exists());

        final HierarchicalResource genParserFactoryFile = resourceService.getHierarchicalResource(output.genParserFactoryFile());
        assertFalse(genParserFactoryFile.exists());
    }
}
