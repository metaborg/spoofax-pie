package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class StylerTest extends TestBase {
    @Test void testCompilerDefaults() throws IOException {
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));
        final Shared shared = TigerInputs.shared(baseDirectory);
        final Styler.Input input = TigerInputs.styler(shared);

        stylerCompiler.compileLanguageProject(input);
        fileAssertions.scopedExists(input.languageGenDirectory(), (s) -> {
            s.assertPublicJavaClass(input.genRules(), "TigerStylingRules");
            s.assertPublicJavaClass(input.genStyler(), "TigerStyler");
            s.assertPublicJavaClass(input.genFactory(), "TigerStylerFactory");
        });
        stylerCompiler.compileAdapterProject(input);
        fileAssertions.scopedExists(input.adapterGenDirectory(), (s) -> {
            s.assertPublicJavaClass(input.genStyleTaskDef(), "TigerStyle");
        });
    }
}
