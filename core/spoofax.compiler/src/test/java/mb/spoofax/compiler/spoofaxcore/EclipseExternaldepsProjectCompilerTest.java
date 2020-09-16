package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.platform.EclipseExternaldepsProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class EclipseExternaldepsProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults() throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            compileLanguageAndAdapterProject(session, inputs);
            final EclipseExternaldepsProjectCompiler.Input input = inputs.eclipseExternaldepsProjectInput().build();
            session.require(component.getEclipseExternaldepsProjectCompiler().createTask(input));
        }
    }
}
