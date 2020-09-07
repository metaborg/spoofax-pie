package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ConstraintAnalyzerCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws IOException {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(shared).build();

        final ConstraintAnalyzerLanguageCompiler.Input languageProjectInput = TigerInputs.constraintAnalyzerLanguageProjectInput(shared, languageProject).build();
        constraintAnalyzerCompiler.compileLanguageProject(languageProjectInput);
        fileAssertions.scopedExists(languageProjectInput.classesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(languageProjectInput.genConstraintAnalyzer(), "TigerConstraintAnalyzer");
            s.assertPublicJavaClass(languageProjectInput.genFactory(), "TigerConstraintAnalyzerFactory");
        });

        final ConstraintAnalyzerLanguageCompiler.AdapterProjectInput adapterProjectInput = TigerInputs.constraintAnalyzerAdapterProjectInput(shared, languageProject, adapterProject).build();
        constraintAnalyzerCompiler.compileAdapterProject(adapterProjectInput);
        fileAssertions.scopedExists(adapterProjectInput.classesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(adapterProjectInput.analyzeTaskDef(), "TigerAnalyze");
        });
    }
}
