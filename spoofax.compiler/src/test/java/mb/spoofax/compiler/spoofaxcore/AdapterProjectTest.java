package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class AdapterProjectTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory);

        // Compile language project, as adapter project depends on it.
        final LanguageProject.Input languageProjectInput = TigerInputs.languageProject(shared);
        languageProjectCompiler.generateBuildGradleKts(languageProjectInput);
        languageProjectCompiler.compile(languageProjectInput);

        // Compile adapter project and test generated files.
        final AdapterProject.Input input = TigerInputs.adapterProjectBuilder(shared)
            .languageProjectDependency(GradleDependency.project(":" + shared.languageProject().coordinate().artifactId()))
            .build();
        TigerInputs.copyTaskDefsIntoAdapterProject(input, resourceService);
        adapterProjectCompiler.generateBuildGradleKts(input);
        adapterProjectCompiler.compile(input);
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.gradle.config.java-library"));
        fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
            s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
            s.assertPublicJavaInterface(input.genComponent(), "TigerComponent");
            s.assertPublicJavaClass(input.genModule(), "TigerModule");
            s.assertPublicJavaClass(input.genInstance(), "TigerInstance");
            s.assertPublicJavaClass(input.genCheckTaskDef(), "TigerCheck");
        });

        // Compile root project, which links together all projects, and build it.
        final RootProject.Output rootProjectOutput = rootProjectCompiler.compile(TigerInputs.rootProjectBuilder(shared)
            .addIncludedProjects(
                shared.languageProject().coordinate().artifactId(),
                shared.adapterProject().coordinate().artifactId()
            )
            .build()
        );
        fileAssertions.asserts(rootProjectOutput.baseDirectory(), (a) -> a.assertGradleBuild("buildAll"));
    }
}
