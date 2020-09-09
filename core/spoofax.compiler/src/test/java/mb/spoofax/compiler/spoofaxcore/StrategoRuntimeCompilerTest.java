package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

class StrategoRuntimeCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final StrategoRuntimeLanguageCompiler.Input input = TigerInputs.strategoRuntimeLanguageProjectInput(shared, languageProject).build();

        try(MixedSession session = pie.newSession()) {
            session.require(component.getStrategoRuntimeLanguageCompiler().createTask(input));
            fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
                s.assertPublicJavaClass(input.genFactory(), "TigerStrategoRuntimeBuilderFactory");
            });
        }
    }
}
