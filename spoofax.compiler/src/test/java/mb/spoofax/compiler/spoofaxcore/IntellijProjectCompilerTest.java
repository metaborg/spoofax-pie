package mb.spoofax.compiler.spoofaxcore;

import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

class IntellijProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws IOException {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared(baseDirectory).build();
        final LanguageProject languageProject = TigerInputs.languageProject(shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(shared).build();

        // Compile language and adapter projects.
        final AdapterProjectCompiler.Input adapterProjectInput = compileLanguageAndAdapterProject(shared, languageProject, adapterProject);

        // Compile IntelliJ project and test generated files.
        final IntellijProjectCompiler.Input input = TigerInputs.intellijProjectInput(shared, adapterProjectInput)
            .adapterProjectDependency(GradleDependency.project(":" + adapterProject.project().coordinate().artifactId()))
            .build();
        intellijProjectCompiler.generateInitial(input);
        intellijProjectCompiler.generateGradleFiles(input);
        intellijProjectCompiler.compile(input);
        fileAssertions.asserts(input.buildGradleKtsFile(), (a) -> a.assertContains("org.metaborg.spoofax.compiler.gradle.spoofaxcore.intellij"));
        fileAssertions.asserts(input.generatedGradleKtsFile(), (a) -> a.assertContains("org.jetbrains.intellij"));
        fileAssertions.asserts(input.pluginXmlFile(), (a) -> a.assertAll("plugin.xml", "<idea-plugin>"));
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
    }
}
