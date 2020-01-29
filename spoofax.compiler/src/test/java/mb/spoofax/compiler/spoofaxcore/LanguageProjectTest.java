package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class LanguageProjectTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory);

        // Compile language project and test generated files.
        final LanguageProject.Input input = compileLanguageProject(shared);
        fileAssertions.asserts(input.generatedGradleKtsFile(), (a) -> a.assertContains("mb/tiger"));
        fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
            s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
        });

        // Compile root project, which links together all projects, and build it.
        final RootProject.Output rootProjectOutput = rootProjectCompiler.compile(TigerInputs.rootProjectBuilder(shared)
            .addIncludedProjects(shared.languageProject().coordinate().artifactId())
            .build()
        );
        fileAssertions.asserts(rootProjectOutput.baseDirectory(), (a) -> a.assertGradleBuild("buildAll"));
    }
}
