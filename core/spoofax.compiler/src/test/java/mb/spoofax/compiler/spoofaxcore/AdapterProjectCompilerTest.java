package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class AdapterProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws Exception {
        final TigerInputs inputs = defaultInputs();

        try(MixedSession session = pie.newSession()) {
            final AdapterProjectCompiler.Input input = compileLanguageAndAdapterProject(session, inputs);
            fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
                s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
                s.assertPublicJavaInterface(input.genComponent(), "TigerComponent");
                s.assertPublicJavaClass(input.genModule(), "TigerModule");
                s.assertPublicJavaClass(input.genInstance(), "TigerInstance");
                s.assertPublicJavaClass(input.genCheckTaskDef(), "TigerCheck");
            });
        }
    }
}
