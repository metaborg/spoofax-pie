package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofaxcore.util.TigerInputs;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.spoofaxcore.util.JavaParser;
import mb.spoofax.compiler.util.JavaProject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

class StylerTest {
    @Test void testCompilerDefault() throws IOException {
        final JavaParser javaParser = new JavaParser();
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = TigerInputs.shared(baseDirectory);
        final JavaProject languageProject = TigerInputs.languageProject(shared).project();
        final Styler.Input input = TigerInputs.styler(shared, languageProject);

        final Styler compiler = Styler.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final Styler.Output output = compiler.compile(input, charset);

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genSourcesJavaDirectory());
        assertTrue(genSourcesJavaDirectory.exists());

        final FileAssertions genStylingRulesFile = new FileAssertions(resourceService.getHierarchicalResource(output.genRulesFile()));
        genStylingRulesFile.assertName("TigerStylingRules.java");
        genStylingRulesFile.assertExists();
        genStylingRulesFile.assertContains("class TigerStylingRules");
        genStylingRulesFile.assertJavaParses(javaParser);

        final FileAssertions genStylerFile = new FileAssertions(resourceService.getHierarchicalResource(output.genStylerFile()));
        genStylerFile.assertName("TigerStyler.java");
        genStylerFile.assertExists();
        genStylerFile.assertContains("class TigerStyler");
        genStylerFile.assertJavaParses(javaParser);

        final FileAssertions genStylerFactoryFile = new FileAssertions(resourceService.getHierarchicalResource(output.genFactoryFile()));
        genStylerFactoryFile.assertName("TigerStylerFactory.java");
        genStylerFactoryFile.assertExists();
        genStylerFactoryFile.assertContains("class TigerStylerFactory");
        genStylerFactoryFile.assertJavaParses(javaParser);
    }
}
