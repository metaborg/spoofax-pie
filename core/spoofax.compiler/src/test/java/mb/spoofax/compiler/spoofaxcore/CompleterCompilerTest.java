package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class CompleterCompilerTest extends TestBase {
    @Disabled("Unclear why this fails")
    @Test void testCompilerDefaults() throws IOException {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(shared).build();

        final CompleterLanguageCompiler.Input languageProjectInput = TigerInputs.completerLanguageProjectInput(shared, languageProject).build();
        completerCompiler.compileLanguageProject(languageProjectInput);
        fileAssertions.scopedExists(languageProjectInput.classesGenDirectory(), (s) -> {
            //s.assertPublicJavaClass(languageProjectInput.genCompleter(), "TigerCompleter");
            // ...
        });

        final CompleterLanguageCompiler.AdapterProjectInput adapterProjectInput = TigerInputs.completerAdapterProjectInput(shared, languageProject, adapterProject).build();
        completerCompiler.compileAdapterProject(adapterProjectInput);
        fileAssertions.scopedExists(adapterProjectInput.classesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(adapterProjectInput.genCompleteTaskDef(), "TigerCompleteTaskDef");
        });
    }
}
