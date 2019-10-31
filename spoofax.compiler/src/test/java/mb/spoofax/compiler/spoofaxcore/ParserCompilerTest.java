package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.ClassKind;
import mb.spoofax.compiler.util.ResourceDeps;
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
        final Properties persistentProperties = new Properties();

        final Shared shared1 = CommonInputs.tigerShared();
        final JavaProject languageProject1 = CommonInputs.tigerLanguageProjectCompilerInput(shared1).project();
        final ParserCompilerInput parserCompilerInput1 = CommonInputs.tigerParserCompilerInput(shared1, languageProject1);
        assertEquals("TigerParseTable", parserCompilerInput1.genTableClass());
        assertEquals("TigerParser", parserCompilerInput1.genParserClass());
        shared1.savePersistentProperties(persistentProperties);
        parserCompilerInput1.savePersistentProperties(persistentProperties);

        final Shared shared2 = CommonInputs.tigerSharedBuilder()
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
        final Shared shared = CommonInputs.tigerShared();
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
        final Shared shared = CommonInputs.tigerShared();
        final JavaProject languageProject = CommonInputs.tigerLanguageProjectCompilerInput(shared).project();
        final ParserCompilerInput input = CommonInputs.tigerParserCompilerInput(shared, languageProject);

        final ParserCompiler compiler = ParserCompiler.fromClassLoaderResources();
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSResource baseDir = new FSResource(fileSystem.getPath("src/main/java"));
        final Charset charset = StandardCharsets.UTF_8;
        final ResourceDeps resourceDeps = compiler.compile(input, baseDir, charset);

        final HierarchicalResource packageDir = compiler.getPackageDir(input, baseDir);
        assertTrue(packageDir.exists());

        final HierarchicalResource genParseTableFile = compiler.getGenParseTableFile(input, packageDir);
        assertEquals("TigerParseTable.java", genParseTableFile.getLeaf());
        assertTrue(genParseTableFile.exists());
        assertTrue(resourceDeps.providedResources().contains(genParseTableFile));
        assertTrue(genParseTableFile.readString(charset).contains("class TigerParseTable"));

        final HierarchicalResource genParserFile = compiler.getParserFile(input, packageDir);
        assertEquals("TigerParser.java", genParserFile.getLeaf());
        assertTrue(genParserFile.exists());
        assertTrue(resourceDeps.providedResources().contains(genParserFile));
        assertTrue(genParserFile.readString(charset).contains("class TigerParser"));

        final HierarchicalResource genParserFactoryFile = compiler.getParserFactoryFile(input, packageDir);
        assertEquals("TigerParserFactory.java", genParserFactoryFile.getLeaf());
        assertTrue(genParserFactoryFile.exists());
        assertTrue(resourceDeps.providedResources().contains(genParserFactoryFile));
        assertTrue(genParserFactoryFile.readString(charset).contains("class TigerParserFactory"));
    }

    @Test
    void testCompilerManual() throws IOException {
        final Shared shared = CommonInputs.tigerShared();
        final JavaProject languageProject = CommonInputs.tigerLanguageProjectCompilerInput(shared).project();
        final ParserCompilerInput input = CommonInputs.tigerParserCompilerInputBuilder(shared, languageProject)
            .classKind(ClassKind.Manual)
            .manualParserClass("MyParser")
            .manualParserFactoryClass("MyParserFactory")
            .build();

        final ParserCompiler compiler = ParserCompiler.fromClassLoaderResources();
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSResource baseDir = new FSResource(fileSystem.getPath("src/main/java"));
        final Charset charset = StandardCharsets.UTF_8;
        final ResourceDeps resourceDeps = compiler.compile(input, baseDir, charset);
        assertTrue(resourceDeps.requiredResources().isEmpty());

        final HierarchicalResource packageDir = compiler.getPackageDir(input, baseDir);
        assertFalse(packageDir.exists());

        final HierarchicalResource genParseTableFile = compiler.getGenParseTableFile(input, packageDir);
        assertFalse(genParseTableFile.exists());

        final HierarchicalResource genParserFile = compiler.getParserFile(input, packageDir);
        assertFalse(genParserFile.exists());

        final HierarchicalResource genParserFactoryFile = compiler.getParserFactoryFile(input, packageDir);
        assertFalse(genParserFactoryFile.exists());
    }
}
