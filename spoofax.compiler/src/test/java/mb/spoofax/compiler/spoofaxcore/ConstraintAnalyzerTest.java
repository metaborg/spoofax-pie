package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ConstraintAnalyzerTest extends TestBase {
    @Test void testCompilerDefaults() throws IOException {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory);
        final ConstraintAnalyzer.Input input = TigerInputs.constraintAnalyzer(shared);

        constraintAnalyzerCompiler.compileLanguageProject(input);
        fileAssertions.scopedExists(input.languageClassesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(input.genConstraintAnalyzer(), "TigerConstraintAnalyzer");
            s.assertPublicJavaClass(input.genFactory(), "TigerConstraintAnalyzerFactory");
        });
        constraintAnalyzerCompiler.compileAdapterProject(input);
        fileAssertions.scopedExists(input.adapterClassesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(input.analyzeTaskDef(), "TigerAnalyze");
        });
    }
}
