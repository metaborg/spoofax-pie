package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MockExecContext;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.ClassKind;
import org.junit.jupiter.api.Test;

class ParserCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        final ParserLanguageCompiler.Input languageProjectInput = inputs.parserLanguageCompilerInput();
        component.getParserLanguageCompiler().compile(new MockExecContext(), languageProjectInput);
        fileAssertions.scopedExists(languageProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(languageProjectInput.baseParseTable(), "TigerParseTable");
            s.assertPublicJavaClass(languageProjectInput.baseParser(), "TigerParser");
            s.assertPublicJavaClass(languageProjectInput.baseParserFactory(), "TigerParserFactory");
        });

        final ParserAdapterCompiler.Input adapterProjectInput = inputs.parserAdapterCompilerInput();
        component.getParserAdapterCompiler().compile(new MockExecContext(), adapterProjectInput);
        fileAssertions.scopedExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(adapterProjectInput.baseParseTaskDef(), "TigerParse");
            s.assertPublicJavaClass(adapterProjectInput.tokenizeTaskDef(), "TigerTokenize");
        });
    }

    @Test void testCompilerManual() throws Exception {
        final TigerInputs inputs = defaultInputs();

        inputs.languageProjectCompilerInputBuilder.withParser()
            .classKind(ClassKind.Manual)
            .baseParser("my.lang", "MyParser")
            .baseParserFactory("my.lang", "MyParserFactory");
        final ParserLanguageCompiler.Input languageProjectInput = inputs.parserLanguageCompilerInput();
        component.getParserLanguageCompiler().compile(new MockExecContext(), languageProjectInput);
        fileAssertions.scopedNotExists(languageProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            s.assertNotExists(languageProjectInput.baseParseTable());
            s.assertNotExists(languageProjectInput.baseParser());
            s.assertNotExists(languageProjectInput.baseParserFactory());
        });

        inputs.adapterProjectCompilerInputBuilder.withParser()
            .classKind(ClassKind.Manual)
            .baseParseTaskDef("my.adapter.taskdef", "MyParseTaskDef")
            .baseTokenizeTaskDef("my.adapter.taskdef", "MyTokenizeTaskDef");
        final ParserAdapterCompiler.Input adapterProjectInput = inputs.parserAdapterCompilerInput();
        component.getParserAdapterCompiler().compile(new MockExecContext(), adapterProjectInput);
        fileAssertions.scopedNotExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            s.assertNotExists(adapterProjectInput.baseParseTaskDef());
            s.assertNotExists(adapterProjectInput.tokenizeTaskDef());
        });
    }
}
