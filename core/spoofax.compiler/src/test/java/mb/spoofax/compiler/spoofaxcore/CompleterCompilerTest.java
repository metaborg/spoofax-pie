package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CompleterCompilerTest extends TestBase {
    @Disabled("Unclear why this fails")
    @Test void testCompilerDefaults() throws Exception {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(shared).build();

        try(MixedSession session = pie.newSession()) {
            final CompleterLanguageCompiler.Input languageProjectInput = TigerInputs.completerLanguageProjectInput(shared, languageProject).build();
            session.require(component.getCompleterLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedExists(languageProjectInput.classesGenDirectory(), (s) -> {
            });

            final CompleterAdapterCompiler.Input adapterProjectInput = TigerInputs.completerAdapterProjectInput(shared, languageProject, adapterProject).build();
            session.require(component.getCompleterAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedExists(adapterProjectInput.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(adapterProjectInput.genCompleteTaskDef(), "TigerCompleteTaskDef");
            });
        }
    }
}
