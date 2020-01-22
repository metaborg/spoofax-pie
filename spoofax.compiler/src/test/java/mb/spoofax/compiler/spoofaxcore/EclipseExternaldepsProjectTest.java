package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class EclipseExternaldepsProjectTest extends TestBase {
    @Test void testCompiler(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory);

        // Compile language project, as adapter project and Eclipse externaldeps project depends on it.
        languageProjectCompiler.compile(TigerInputs.languageProject(shared));

        // Compile adapter project, as Eclipse externaldeps project depends on it.
        final AdapterProject.Input adapterProjectInput = TigerInputs.adapterProjectBuilder(shared)
            .languageProjectDependency(GradleDependency.project(":" + shared.languageProject().coordinate().artifactId()))
            .build();
        TigerInputs.copyTaskDefsIntoAdapterProject(adapterProjectInput, resourceService);
        adapterProjectCompiler.compile(adapterProjectInput);

        // Compile Eclipse project and test generated files.
        final EclipseExternaldepsProject.Input input = TigerInputs.eclipseExternaldepsProjectBuilder(shared)
            .languageProjectDependency(GradleDependency.project(":" + shared.languageProject().coordinate().artifactId()))
            .adapterProjectDependency(GradleDependency.project(":" + shared.adapterProject().coordinate().artifactId()))
            .build();
        eclipseExternaldepsProjectCompiler.compile(input);
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.coronium.embedding"));

        // Compile root project, which links together all projects, and build it.
        final RootProject.Output rootProjectOutput = rootProjectCompiler.compile(TigerInputs.rootProjectBuilder(shared)
            .addIncludedProjects(
                shared.languageProject().coordinate().artifactId(),
                shared.adapterProject().coordinate().artifactId(),
                shared.eclipseExternaldepsProject().coordinate().artifactId()
            )
            .build()
        );
        fileAssertions.asserts(rootProjectOutput.baseDirectory(), (a) -> a.assertGradleBuild("buildAll"));
    }
}
