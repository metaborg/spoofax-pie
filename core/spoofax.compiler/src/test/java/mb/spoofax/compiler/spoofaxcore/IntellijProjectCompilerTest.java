package mb.spoofax.compiler.spoofaxcore;

import mb.pie.api.MixedSession;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.platform.IntellijProjectCompiler;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.GradleDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class IntellijProjectCompilerTest extends TestBase {
    @Test void testCompilerDefaults(@TempDir Path temporaryDirectoryPath) throws Exception {
        final FSPath baseDirectory = new FSPath(temporaryDirectoryPath);
        final Shared shared = TigerInputs.shared().build();
        final LanguageProject languageProject = TigerInputs.languageProject(baseDirectory, shared).build();
        final AdapterProject adapterProject = TigerInputs.adapterProject(baseDirectory, shared).build();

        try(MixedSession session = pie.newSession()) {
            // Compile language and adapter projects.
            final AdapterProjectCompiler.Input adapterProjectInput = compileLanguageAndAdapterProject(session, shared, languageProject, adapterProject);

            // Compile IntelliJ project and test generated files.
            final IntellijProjectCompiler.Input input = TigerInputs.intellijProjectInput(baseDirectory, shared, adapterProjectInput)
                .adapterProjectDependency(GradleDependency.project(":" + adapterProject.project().coordinate().artifactId()))
                .build();
            session.require(component.getIntellijProjectCompiler().createTask(input));
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
}
