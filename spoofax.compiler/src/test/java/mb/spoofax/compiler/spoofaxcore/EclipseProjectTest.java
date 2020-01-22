package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.TemplateCompiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class EclipseProjectTest extends TestBase {
    @Test void testCompiler(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory);

        // Compile language project, as adapter project depends on it.
        languageProjectCompiler.compile(TigerInputs.languageProject(shared));

        // Compile adapter project, as Eclipse project depends on it.
        final AdapterProject.Input adapterProjectInput = TigerInputs.adapterProjectBuilder(shared)
            .languageProjectDependency(GradleDependency.project(":" + shared.languageProject().coordinate().artifactId()))
            .build();
        TigerInputs.copyTaskDefsIntoAdapterProject(adapterProjectInput, resourceService);
        adapterProjectCompiler.compile(adapterProjectInput);

        // Compile Eclipse project and test generated files.
        final EclipseProject.Input input = TigerInputs.eclipseProjectBuilder(shared, adapterProjectInput)
            .adapterProjectDependency(GradleDependency.project(":" + shared.adapterProject().coordinate().artifactId()))
            .build();
        final EclipseProject compiler = new EclipseProject(new TemplateCompiler(Shared.class, resourceService, charset));
        compiler.compile(input);
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.coronium.bundle"));
        fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
            s.assertPublicJavaClass(input.genPlugin(), "TigerPlugin");
            s.assertPublicJavaInterface(input.genEclipseComponent(), "TigerEclipseComponent");
            s.assertPublicJavaClass(input.genEclipseModule(), "TigerEclipseModule");
            s.assertPublicJavaClass(input.genEclipseIdentifiers(), "TigerEclipseIdentifiers");
            s.assertPublicJavaClass(input.genEditor(), "TigerEditor");
            s.assertPublicJavaClass(input.genEditorTracker(), "TigerEditorTracker");
            s.assertPublicJavaClass(input.genNature(), "TigerNature");
            s.assertPublicJavaClass(input.genAddNatureHandler(), "TigerAddNatureHandler");
            s.assertPublicJavaClass(input.genRemoveNatureHandler(), "TigerRemoveNatureHandler");
            s.assertPublicJavaClass(input.genProjectBuilder(), "TigerProjectBuilder");
            s.assertPublicJavaClass(input.genMainMenu(), "TigerMainMenu");
            s.assertPublicJavaClass(input.genEditorContextMenu(), "TigerEditorContextMenu");
            s.assertPublicJavaClass(input.genResourceContextMenu(), "TigerResourceContextMenu");
            s.assertPublicJavaClass(input.genRunCommandHandler(), "TigerRunCommandHandler");
            s.assertPublicJavaClass(input.genObserveHandler(), "TigerObserveHandler");
            s.assertPublicJavaClass(input.genUnobserveHandler(), "TigerUnobserveHandler");
        });

        // Compile root project, which links together language and adapter project, and build it.
        final RootProject.Output rootProjectOutput = rootProjectCompiler.compile(TigerInputs.rootProjectBuilder(shared)
            .addIncludedProjects(
                shared.languageProject().coordinate().artifactId(),
                shared.adapterProject().coordinate().artifactId(),
                shared.eclipseProject().coordinate().artifactId()
            )
            .build()
        );
        fileAssertions.asserts(rootProjectOutput.baseDirectory(), (a) -> a.assertGradleBuild("buildAll"));
    }
}
