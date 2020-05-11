package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class CliProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(shared).build();

        // Compile language and adapter projects.
        final AdapterProjectCompiler.Input adapterProjectInput = compileLanguageAndAdapterProject(shared, languageProject, adapterProject);

        // Compile CLI project and test generated files.
        final CliProjectCompiler.Input input = TigerInputs.cliProjectInput(shared, adapterProjectInput)
            .adapterProjectDependency(GradleDependency.project(":" + adapterProject.project().coordinate().artifactId()))
            .build();
        cliProjectCompiler.generateInitial(input);
        cliProjectCompiler.compile(input);
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.spoofax.compiler.gradle.spoofaxcore.cli"));
        fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
            s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
            s.assertPublicJavaClass(input.genMain(), "Main");
        });
    }
}
