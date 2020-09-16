package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class LanguageProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            final LanguageProjectCompiler.Input input = compileLanguageProject(session, inputs);
            fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
                s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
            });
        }
    }
}
