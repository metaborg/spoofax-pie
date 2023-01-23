package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MockExecContext;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.ClassKind;
import org.junit.jupiter.api.Test;

class StrategoRuntimeCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        final StrategoRuntimeLanguageCompiler.Input input = inputs.strategoRuntimeLanguageCompilerInput();
        component.getStrategoRuntimeLanguageCompiler().compile(new MockExecContext(), input);
        fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(input.baseStrategoRuntimeBuilderFactory(), "TigerStrategoRuntimeBuilderFactory");
        });
    }

    @Test void testCompilerGeneratedCustom() throws Exception {
        final TigerInputs inputs = defaultInputs();

        inputs.languageProjectCompilerInputBuilder.withStrategoRuntime()
            .classKind(ClassKind.Generated)
            .baseStrategoRuntimeBuilderFactory("my.lang", "MyStrategoRuntimeBuilderFactory");
        final StrategoRuntimeLanguageCompiler.Input input = inputs.strategoRuntimeLanguageCompilerInput();
        component.getStrategoRuntimeLanguageCompiler().compile(new MockExecContext(), input);
        fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(input.baseStrategoRuntimeBuilderFactory(), "MyStrategoRuntimeBuilderFactory");
        });
    }

    @Test void testCompilerManual() throws Exception {
        final TigerInputs inputs = defaultInputs();

        inputs.languageProjectCompilerInputBuilder.withStrategoRuntime()
            .classKind(ClassKind.Manual)
            .baseStrategoRuntimeBuilderFactory("my.lang", "MyStrategoRuntimeBuilderFactory");
        final StrategoRuntimeLanguageCompiler.Input input = inputs.strategoRuntimeLanguageCompilerInput();
        component.getStrategoRuntimeLanguageCompiler().compile(new MockExecContext(), input);
        fileAssertions.scopedExists(input.generatedJavaSourcesDirectory(), (s) -> {
            s.assertNotExists(input.baseStrategoRuntimeBuilderFactory());
        });
    }
}
