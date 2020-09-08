package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class StylerCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(baseDirectory, shared).build();

        try(MixedSession session = pie.newSession()) {
            final StylerLanguageCompiler.Input languageProjectInput = TigerInputs.stylerLanguageProjectInput(shared, languageProject).build();
            session.require(component.getStylerLanguageCompiler().createTask(languageProjectInput));
            fileAssertions.scopedExists(languageProjectInput.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(languageProjectInput.genRules(), "TigerStylingRules");
                s.assertPublicJavaClass(languageProjectInput.genStyler(), "TigerStyler");
                s.assertPublicJavaClass(languageProjectInput.genFactory(), "TigerStylerFactory");
            });

            final StylerAdapterCompiler.Input adapterProjectInput = TigerInputs.stylerAdapterProjectInput(shared, languageProject, adapterProject).build();
            session.require(component.getStylerAdapterCompiler().createTask(adapterProjectInput));
            fileAssertions.scopedExists(adapterProjectInput.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(adapterProjectInput.genStyleTaskDef(), "TigerStyle");
            });
        }
    }
}
