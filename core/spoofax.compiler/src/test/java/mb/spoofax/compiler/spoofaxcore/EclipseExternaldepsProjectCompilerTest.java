package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class EclipseExternaldepsProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(shared).build();

        // Compile language and adapter projects.
        compileLanguageAndAdapterProject(shared, languageProject, adapterProject);

        // Compile Eclipse externaldeps project and test generated files.
        final EclipseExternaldepsProjectCompiler.Input input = TigerInputs.eclipseExternaldepsProjectInput(shared, languageProject, adapterProject)
            .languageProjectDependency(GradleDependency.project(":" + languageProject.project().coordinate().artifactId()))
            .adapterProjectDependency(GradleDependency.project(":" + adapterProject.project().coordinate().artifactId()))
            .build();
        eclipseExternaldepsProjectCompiler.generateInitial(input);
        eclipseExternaldepsProjectCompiler.compile(input);
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse.externaldeps"));
    }
}
