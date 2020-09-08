package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class EclipseProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws Exception {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(baseDirectory, shared).build();

        try(MixedSession session = pie.newSession()) {
            // Compile language and adapter projects.
            final LanguageProjectCompiler.Input languageProjectInput = compileLanguageProject(session, shared, languageProject);
            final AdapterProjectCompiler.Input adapterProjectInput = compileAdapterProject(session, shared, languageProject, adapterProject);

            // Compile Eclipse externaldeps project, as Eclipse project depends on it.
            final EclipseExternaldepsProjectCompiler.Input eclipseExternalDepsInput = TigerInputs
                .eclipseExternaldepsProjectInput(baseDirectory, shared, languageProject, adapterProject)
                .languageProjectDependency(GradleDependency.project(":" + languageProject.project().coordinate().artifactId()))
                .adapterProjectDependency(GradleDependency.project(":" + adapterProject.project().coordinate().artifactId()))
                .build();
            session.require(component.getEclipseExternaldepsProjectCompiler().createTask(eclipseExternalDepsInput));

            // Compile Eclipse project and test generated files.
            final EclipseProjectCompiler.Input input = TigerInputs.eclipseProjectInput(baseDirectory, shared, languageProjectInput, adapterProjectInput)
                .eclipseExternaldepsDependency(GradleDependency.project(":" + eclipseExternalDepsInput.project().coordinate().artifactId()))
                .build();

            session.require(component.getEclipseProjectCompiler().createTask(input));
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
}
