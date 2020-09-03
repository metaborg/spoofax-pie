package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class EclipseProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(shared).build();

        // Compile language and adapter projects.
        final LanguageProjectCompiler.Input languageProjectInput = compileLanguageProject(shared, languageProject);
        final AdapterProjectCompiler.Input adapterProjectInput = compileAdapterProject(shared, languageProject, adapterProject);

        // Compile Eclipse externaldeps project, as Eclipse project depends on it.
        final EclipseExternaldepsProjectCompiler.Input eclipseExternalDepsInput = TigerInputs
            .eclipseExternaldepsProjectInput(shared, languageProject, adapterProject)
            .languageProjectDependency(GradleDependency.project(":" + languageProject.project().coordinate().artifactId()))
            .adapterProjectDependency(GradleDependency.project(":" + adapterProject.project().coordinate().artifactId()))
            .build();
        eclipseExternaldepsProjectCompiler.compile(eclipseExternalDepsInput);

        // Compile Eclipse project and test generated files.
        final EclipseProjectCompiler.Input input = TigerInputs.eclipseProjectInput(shared, languageProjectInput, adapterProjectInput)
            .eclipseExternaldepsDependency(GradleDependency.project(":" + eclipseExternalDepsInput.project().coordinate().artifactId()))
            .build();
        eclipseProjectCompiler.generateInitial(input);
        eclipseProjectCompiler.compile(input);
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.spoofax.compiler.gradle.spoofaxcore.eclipse"));
        fileAssertions.asserts(input.pluginXmlFile(), (s) -> s.assertAll("plugin.xml", "<plugin>"));
        fileAssertions.asserts(input.manifestMfFile(), (s) -> s.assertAll("MANIFEST.MF", "Export-Package"));
        fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
            s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
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
    }
}
