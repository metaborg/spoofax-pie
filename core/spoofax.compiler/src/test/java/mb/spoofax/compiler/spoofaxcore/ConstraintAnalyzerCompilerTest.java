package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class ConstraintAnalyzerCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(baseDirectory, shared).build();

        try(MixedSession session = pie.newSession()) {
            final ConstraintAnalyzerLanguageCompiler.Input languageProjectInput = TigerInputs.constraintAnalyzerLanguageProjectInput(shared, languageProject).build();
            session.require(component.getConstraintAnalyzerLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedExists(languageProjectInput.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(languageProjectInput.genConstraintAnalyzer(), "TigerConstraintAnalyzer");
                s.assertPublicJavaClass(languageProjectInput.genFactory(), "TigerConstraintAnalyzerFactory");
            });

            final ConstraintAnalyzerAdapterCompiler.Input adapterProjectInput = TigerInputs.constraintAnalyzerAdapterProjectInput(shared, languageProject, adapterProject).build();
            session.require(component.getConstraintAnalyzerAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedExists(adapterProjectInput.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(adapterProjectInput.analyzeTaskDef(), "TigerAnalyze");
            });
        }
    }
}
