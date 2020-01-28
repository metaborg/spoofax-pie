package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class IntellijProjectTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory);

        // Compile language and adapter projects.
        final AdapterProject.Input adapterProjectInput = compileLanguageAndAdapterProject(shared);

        // Compile IntelliJ project and test generated files.
        final IntellijProject.Input input = TigerInputs.intellijProjectBuilder(shared, adapterProjectInput)
            .adapterProjectDependency(GradleDependency.project(":" + shared.adapterProject().coordinate().artifactId()))
            .build();
        intellijProjectCompiler.compile(input);
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertAll("build.gradle.kts", "org.jetbrains.intellij"));
        fileAssertions.asserts(input.genPluginXmlFile(), (a) -> a.assertAll("plugin.xml", "<idea-plugin>"));
        fileAssertions.scopedExists(input.classesGenDirectory(), (s) -> {
            s.asserts(input.packageInfo(), (a) -> a.assertAll("package-info.java", "@DefaultQualifier(NonNull.class)"));
            s.assertPublicJavaClass(input.genPlugin(), "TigerPlugin");
            s.assertPublicJavaInterface(input.genComponent(), "TigerIntellijComponent");
            s.assertPublicJavaClass(input.genModule(), "TigerIntellijModule");
            s.assertPublicJavaClass(input.genPlugin(), "TigerPlugin");
            s.assertPublicJavaClass(input.genLoader(), "TigerLoader");
            s.assertPublicJavaClass(input.genLanguage(), "TigerLanguage");
            s.assertPublicJavaClass(input.genFileType(), "TigerFileType");
            s.assertPublicJavaClass(input.genFileElementType(), "TigerFileElementType");
            s.assertPublicJavaClass(input.genFileTypeFactory(), "TigerFileTypeFactory");
            s.assertPublicJavaClass(input.genSyntaxHighlighterFactory(), "TigerSyntaxHighlighterFactory");
            s.assertPublicJavaClass(input.genParserDefinition(), "TigerParserDefinition");
        });

        // Compile root project, which links together all projects, and build it.
        final RootProject.Output rootProjectOutput = rootProjectCompiler.compile(TigerInputs.rootProjectBuilder(shared)
            .addIncludedProjects(
                shared.languageProject().coordinate().artifactId(),
                shared.adapterProject().coordinate().artifactId(),
                shared.intellijProject().coordinate().artifactId()
            )
            .build()
        );
        fileAssertions.asserts(rootProjectOutput.baseDirectory(), (a) -> a.assertGradleBuild("buildAll"));
    }
}
