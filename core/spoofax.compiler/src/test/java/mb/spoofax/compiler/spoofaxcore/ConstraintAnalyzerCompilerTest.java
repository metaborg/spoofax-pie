package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.adapter.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class ConstraintAnalyzerCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            final ConstraintAnalyzerLanguageCompiler.Input languageProjectInput = inputs.constraintAnalyzerLanguageCompilerInput();
            session.require(component.getConstraintAnalyzerLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedExists(languageProjectInput.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(languageProjectInput.genConstraintAnalyzer(), "TigerConstraintAnalyzer");
                s.assertPublicJavaClass(languageProjectInput.genFactory(), "TigerConstraintAnalyzerFactory");
            });

            final ConstraintAnalyzerAdapterCompiler.Input adapterProjectInput = inputs.constraintAnalyzerAdapterCompilerInput();
            session.require(component.getConstraintAnalyzerAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedExists(adapterProjectInput.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(adapterProjectInput.analyzeTaskDef(), "TigerAnalyze");
            });
        }
    }
}
