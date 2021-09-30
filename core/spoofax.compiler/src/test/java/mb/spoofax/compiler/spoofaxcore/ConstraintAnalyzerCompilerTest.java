package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MockExecContext;
import mb.spoofax.compiler.adapter.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class ConstraintAnalyzerCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        final ConstraintAnalyzerLanguageCompiler.Input languageProjectInput = inputs.constraintAnalyzerLanguageCompilerInput();
        component.getConstraintAnalyzerLanguageCompiler().compile(new MockExecContext(), languageProjectInput);
        fileAssertions.scopedExists(languageProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(languageProjectInput.baseConstraintAnalyzer(), "TigerConstraintAnalyzer");
            s.assertPublicJavaClass(languageProjectInput.baseConstraintAnalyzerFactory(), "TigerConstraintAnalyzerFactory");
        });

        final ConstraintAnalyzerAdapterCompiler.Input adapterProjectInput = inputs.constraintAnalyzerAdapterCompilerInput();
        component.getConstraintAnalyzerAdapterCompiler().compile(new MockExecContext(), adapterProjectInput);
        fileAssertions.scopedExists(adapterProjectInput.generatedJavaSourcesDirectory(), (s) -> {
            s.assertPublicJavaClass(adapterProjectInput.analyzeTaskDef(), "TigerAnalyze");
        });
    }
}
