package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class StylerCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws IOException {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(shared).build();

        final StylerLanguageCompiler.Input languageProjectInput = TigerInputs.stylerLanguageProjectInput(shared, languageProject).build();
        stylerCompiler.compileLanguageProject(languageProjectInput);
        fileAssertions.scopedExists(languageProjectInput.classesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(languageProjectInput.genRules(), "TigerStylingRules");
            s.assertPublicJavaClass(languageProjectInput.genStyler(), "TigerStyler");
            s.assertPublicJavaClass(languageProjectInput.genFactory(), "TigerStylerFactory");
        });

        final StylerLanguageCompiler.AdapterProjectInput adapterProjectInput = TigerInputs.stylerAdapterProjectInput(shared, languageProject, adapterProject).build();
        stylerCompiler.compileAdapterProject(adapterProjectInput);
        fileAssertions.scopedExists(adapterProjectInput.classesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(adapterProjectInput.genStyleTaskDef(), "TigerStyle");
        });
    }
}
