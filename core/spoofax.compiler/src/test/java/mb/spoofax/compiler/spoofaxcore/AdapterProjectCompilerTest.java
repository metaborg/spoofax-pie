package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class AdapterProjectCompilerTest extends TestBase {
    @Test
    void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws Exception {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(baseDirectory, shared).build();

        try(MixedSession session = pie.newSession()) {
            // Compile language and adapter projects.
            final AdapterProjectCompiler.Input input = compileLanguageAndAdapterProject(session, shared, languageProject, adapterProject);
            // Test generated files.
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
