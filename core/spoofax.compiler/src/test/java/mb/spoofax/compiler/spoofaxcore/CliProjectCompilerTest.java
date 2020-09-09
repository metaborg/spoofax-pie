package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.platform.CliProjectCompiler;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class CliProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws Exception {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(baseDirectory, shared).build();

        try(MixedSession session = pie.newSession()) {
            // Compile language and adapter projects.
            final AdapterProjectCompiler.Input adapterProjectInput = compileLanguageAndAdapterProject(session, shared, languageProject, adapterProject);

            // Compile CLI project and test generated files.
            final CliProjectCompiler.Input input = TigerInputs.cliProjectInput(baseDirectory, shared, adapterProjectInput)
                .adapterProjectDependency(GradleDependency.project(":" + adapterProject.project().coordinate().artifactId()))
                .build();
            session.require(component.getCliProjectCompiler().createTask(input));
            fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
                s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
                s.assertPublicJavaClass(input.genMain(), "Main");
            });
        }
    }
}
