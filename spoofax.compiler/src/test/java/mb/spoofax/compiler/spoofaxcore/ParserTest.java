package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.ClassKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest extends TestBase {
    @ParameterizedTest @EnumSource(value = ClassKind.class, names = {"Manual", "Extended"})
    void testManualRequiresClasses(ClassKind classKind) {
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

    @Test void testCompilerDefaults() throws IOException {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory);
        final Parser.Input input = TigerInputs.parser(shared);

        parserCompiler.compileLanguageProject(input);
        fileAssertions.scopedExists(input.languageClassesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(input.genTable(), "TigerParseTable");
            s.assertPublicJavaClass(input.genParser(), "TigerParser");
            s.assertPublicJavaClass(input.genFactory(), "TigerParserFactory");
        });
        parserCompiler.compileAdapterProject(input);
        fileAssertions.scopedExists(input.adapterClassesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(input.genParseTaskDef(), "TigerParse");
            s.assertPublicJavaClass(input.tokenizeTaskDef(), "TigerTokenize");
        });
    }

    @Test void testCompilerManual() throws IOException {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory);
        final Parser.Input input = TigerInputs.parserBuilder(shared)
            .classKind(ClassKind.Manual)
            .manualParser("my.lang", "MyParser")
            .manualFactory("my.lang", "MyParserFactory")
            .manualParseTaskDef("my.adapter.taskdef", "MyParseTaskDef")
            .manualTokenizeTaskDef("my.adapter.taskdef", "MyTokenizeTaskDef")
            .build();

        parserCompiler.compileLanguageProject(input);
        fileAssertions.scopedNotExists(input.languageClassesGenDirectory(), (s) -> {
            s.assertNotExists(input.genTable());
            s.assertNotExists(input.genParser());
            s.assertNotExists(input.genFactory());
        });
        parserCompiler.compileAdapterProject(input);
        fileAssertions.scopedNotExists(input.adapterClassesGenDirectory(), (s) -> {
            s.assertNotExists(input.genParseTaskDef());
            s.assertNotExists(input.tokenizeTaskDef());
        });
    }
}
