package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.spoofaxcore.util.JavaParser;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.ClassKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    //    @Test void testPersistentProperties() {
//        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
//        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
//
//        final Properties persistentProperties = new Properties();
//
//        final Shared shared1 = TigerInputs.shared(baseDirectory);
//        final Parser.Input parserCompilerInput1 = TigerInputs.parser(shared1);
//        assertEquals("TigerParseTable", parserCompilerInput1.genTableClass());
//        assertEquals("TigerParser", parserCompilerInput1.genParserClass());
//        shared1.savePersistentProperties(persistentProperties);
//        parserCompilerInput1.savePersistentProperties(persistentProperties);
//
//        final Shared shared2 = TigerInputs.sharedBuilder(baseDirectory)
//            .name("Tigerr") // Change language name.
//            .withPersistentProperties(persistentProperties)
//            .build();
//        final Parser.Input parserCompilerInput2 = TigerInputs.parserBuilder(shared2)
//            .withPersistentProperties(persistentProperties)
//            .build();
//        // Should not affect generated class names.
//        assertEquals("TigerParseTable", parserCompilerInput2.genTableClass());
//        assertEquals("TigerParser", parserCompilerInput2.genParserClass());
//    }

    @ParameterizedTest @EnumSource(value = ClassKind.class, names = {"Manual", "Extended"})
    void testManualRequiresClasses(ClassKind classKind) {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = TigerInputs.shared(baseDirectory);
        assertThrows(IllegalArgumentException.class, () -> {
            TigerInputs.parserBuilder(shared)
                .classKind(classKind)
                .build(); // Class kind is Manual or Extended, but manual class names were not set: check fails.
        });

        TigerInputs.parserBuilder(shared)
            .classKind(classKind)
            .manualParser("my.lang", "MyParser")
            .manualFactory("my.lang", "MyParserFactory")
            .manualParseTaskDef("my.adapter.taskdef", "MyParseTaskDef")
            .manualTokenizeTaskDef("my.adapter.taskdef", "MyTokenizeTaskDef")
            .build();
    }

    @Test void testCompilerDefault() throws IOException {
        final JavaParser javaParser = new JavaParser();
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = TigerInputs.shared(baseDirectory);
        final Parser.Input input = TigerInputs.parser(shared);

        final Charset charset = StandardCharsets.UTF_8;
        final Parser compiler = Parser.fromClassLoaderResources(resourceService, charset);
        final Parser.LanguageProjectOutput output = compiler.compileLanguageProject(input);

        final ResourcePath genPath = input.languageGenDirectory();
        final HierarchicalResource genDirectory = resourceService.getHierarchicalResource(genPath);
        assertTrue(genDirectory.exists());

        final FileAssertions genParseTableFile = new FileAssertions(resourceService.getHierarchicalResource(input.genTable().file(genPath)));
        genParseTableFile.assertName("TigerParseTable.java");
        genParseTableFile.assertExists();
        genParseTableFile.assertContains("class TigerParseTable");
        genParseTableFile.assertJavaParses(javaParser);

        final FileAssertions genParserFile = new FileAssertions(resourceService.getHierarchicalResource(input.genParser().file(genPath)));
        genParserFile.assertName("TigerParser.java");
        genParserFile.assertExists();
        genParserFile.assertContains("class TigerParser");
        genParserFile.assertJavaParses(javaParser);

        final FileAssertions genParserFactoryFile = new FileAssertions(resourceService.getHierarchicalResource(input.genFactory().file(genPath)));
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
        final Parser.Input input = TigerInputs.parserBuilder(shared)
            .classKind(ClassKind.Manual)
            .manualParser("my.lang", "MyParser")
            .manualFactory("my.lang", "MyParserFactory")
            .manualParseTaskDef("my.adapter.taskdef", "MyParseTaskDef")
            .manualTokenizeTaskDef("my.adapter.taskdef", "MyTokenizeTaskDef")
            .build();

        final Charset charset = StandardCharsets.UTF_8;
        final Parser compiler = Parser.fromClassLoaderResources(resourceService, charset);
        final Parser.LanguageProjectOutput output = compiler.compileLanguageProject(input);

        final ResourcePath genPath = input.languageGenDirectory();
        final HierarchicalResource genDirectory = resourceService.getHierarchicalResource(genPath);
        assertFalse(genDirectory.exists());

        final HierarchicalResource genParseTableFile = resourceService.getHierarchicalResource(input.genTable().file(genPath));
        assertFalse(genParseTableFile.exists());

        final HierarchicalResource genParserFile = resourceService.getHierarchicalResource(input.genParser().file(genPath));
        assertFalse(genParserFile.exists());

        final HierarchicalResource genParserFactoryFile = resourceService.getHierarchicalResource(input.genFactory().file(genPath));
        assertFalse(genParserFactoryFile.exists());
    }
}
