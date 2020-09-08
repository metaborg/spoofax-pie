package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class EclipseExternaldepsProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws Exception {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(baseDirectory, shared).build();

        try(MixedSession session = pie.newSession()) {
            // Compile language and adapter projects.
            compileLanguageAndAdapterProject(session, shared, languageProject, adapterProject);

            // Compile Eclipse externaldeps project and test generated files.
            final EclipseExternaldepsProjectCompiler.Input input = TigerInputs.eclipseExternaldepsProjectInput(baseDirectory, shared, languageProject, adapterProject)
                .languageProjectDependency(GradleDependency.project(":" + languageProject.project().coordinate().artifactId()))
                .adapterProjectDependency(GradleDependency.project(":" + adapterProject.project().coordinate().artifactId()))
                .build();
            session.require(component.getEclipseExternaldepsProjectCompiler().createTask(input));
        }
    }
}
