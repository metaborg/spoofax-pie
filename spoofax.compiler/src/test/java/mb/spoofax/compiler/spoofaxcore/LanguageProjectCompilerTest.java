package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class LanguageProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();

        // Compile language project and test generated files.
        final LanguageProjectCompiler.Input input = compileLanguageProject(shared, languageProject);
//        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.spoofax.compiler.gradle.spoofaxcore.language"));
        fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
            s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
        });
    }
}
