package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.ClassKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ParserCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            final ParserLanguageCompiler.Input languageProjectInput = inputs.parserLanguageCompilerInput();
            session.require(component.getParserLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedExists(languageProjectInput.generatedJavaSourcesDirectory(), (s) -> {
                s.assertPublicJavaClass(languageProjectInput.genTable(), "TigerParseTable");
                s.assertPublicJavaClass(languageProjectInput.genParser(), "TigerParser");
                s.assertPublicJavaClass(languageProjectInput.genParserFactory(), "TigerParserFactory");
            });

            final ParserAdapterCompiler.Input adapterProjectInput = inputs.parserAdapterCompilerInput();
            session.require(component.getParserAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
                s.assertPublicJavaClass(adapterProjectInput.genParseTaskDef(), "TigerParse");
                s.assertPublicJavaClass(adapterProjectInput.tokenizeTaskDef(), "TigerTokenize");
            });
        }
    }

    @Test void testCompilerManual() throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            inputs.languageProjectCompilerInputBuilder.withParser()
                .classKind(ClassKind.Manual)
                .genParser("my.lang", "MyParser")
                .genParserFactory("my.lang", "MyParserFactory");
            final ParserLanguageCompiler.Input languageProjectInput = inputs.parserLanguageCompilerInput();
            session.require(component.getParserLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedNotExists(languageProjectInput.generatedJavaSourcesDirectory(), (s) -> {
                s.assertNotExists(languageProjectInput.genTable());
                s.assertNotExists(languageProjectInput.genParser());
                s.assertNotExists(languageProjectInput.genParserFactory());
            });

            inputs.adapterProjectCompilerInputBuilder.withParser()
                .classKind(ClassKind.Manual)
                .genParseTaskDef("my.adapter.taskdef", "MyParseTaskDef")
                .genTokenizeTaskDef("my.adapter.taskdef", "MyTokenizeTaskDef");
            final ParserAdapterCompiler.Input adapterProjectInput = inputs.parserAdapterCompilerInput();
            session.require(component.getParserAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedNotExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
                s.assertNotExists(adapterProjectInput.genParseTaskDef());
                s.assertNotExists(adapterProjectInput.tokenizeTaskDef());
            });
        }
    }

    @ParameterizedTest @EnumSource(value = ClassKind.class, names = {"Manual"})
    void testManualRequiresClasses(ClassKind classKind) {
        final TigerInputs inputs = defaultInputs();

        inputs.languageProjectCompilerInputBuilder.withParser()
            .classKind(classKind);
        assertThrows(IllegalArgumentException.class, inputs::languageProjectCompilerInput); // Class kind is Manual but manual class names were not set: check fails.
        inputs.clearBuiltInputs();

        inputs.languageProjectCompilerInputBuilder.withParser()
            .classKind(classKind)
            .genParser("my.lang", "MyParser")
            .genParserFactory("my.lang", "MyParserFactory");
        inputs.languageProjectCompilerInput(); // Manual classes are set: no exception.

        inputs.adapterProjectCompilerInputBuilder.withParser()
            .classKind(classKind);
        assertThrows(IllegalArgumentException.class, inputs::adapterProjectCompilerInput); // Class kind is Manual but manual class names were not set: check fails.
        inputs.clearBuiltInputs();

        inputs.adapterProjectCompilerInputBuilder.withParser()
            .classKind(classKind)
            .genParseTaskDef("my.adapter.taskdef", "MyParseTaskDef")
            .genTokenizeTaskDef("my.adapter.taskdef", "MyTokenizeTaskDef");
        inputs.adapterProjectCompilerInput(); // Manual classes are set: no exception.
    }
}
