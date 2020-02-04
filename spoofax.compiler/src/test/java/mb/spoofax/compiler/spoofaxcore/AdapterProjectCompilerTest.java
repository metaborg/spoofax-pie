package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class AdapterProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(shared).build();

        // Compile language and adapter projects.
        final AdapterProjectCompiler.Input input = compileLanguageAndAdapterProject(shared, languageProject, adapterProject);
        // Test generated files.
//        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.spoofax.compiler.gradle.spoofaxcore.adapter"));
        fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
            s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
            s.assertPublicJavaInterface(input.genComponent(), "TigerComponent");
            s.assertPublicJavaClass(input.genModule(), "TigerModule");
            s.assertPublicJavaClass(input.genInstance(), "TigerInstance");
            s.assertPublicJavaClass(input.genCheckTaskDef(), "TigerCheck");
        });
    }
}
