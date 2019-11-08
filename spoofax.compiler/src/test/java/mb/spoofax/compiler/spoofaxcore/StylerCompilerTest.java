package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofaxcore.util.CommonInputs;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.spoofaxcore.util.JavaParser;
import mb.spoofax.compiler.util.JavaProject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.*;

class StylerCompilerTest {
    @Test void testCompilerDefault() throws IOException {
        final JavaParser javaParser = new JavaParser();
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = CommonInputs.tigerShared(baseDirectory);
        final JavaProject languageProject = CommonInputs.tigerLanguageProjectCompilerInput(shared).project();
        final StylerCompiler.Input input = CommonInputs.tigerStylerCompilerInput(shared, languageProject);

        final StylerCompiler compiler = StylerCompiler.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final StylerCompiler.Output output = compiler.compile(input, charset);

        final HierarchicalResource packageDirectory = resourceService.getHierarchicalResource(output.packageDirectory());
        assertTrue(packageDirectory.exists());

        final FileAssertions genStylingRulesFile = new FileAssertions(resourceService.getHierarchicalResource(output.genStylingRulesFile()));
        genStylingRulesFile.assertName("TigerStylingRules.java");
        genStylingRulesFile.assertExists();
        genStylingRulesFile.assertContains("class TigerStylingRules");
        genStylingRulesFile.assertJavaParses(javaParser);

        final FileAssertions genStylerFile = new FileAssertions(resourceService.getHierarchicalResource(output.genStylerFile()));
        genStylerFile.assertName("TigerStyler.java");
        genStylerFile.assertExists();
        genStylerFile.assertContains("class TigerStyler");
        genStylerFile.assertJavaParses(javaParser);

        final FileAssertions genStylerFactoryFile = new FileAssertions(resourceService.getHierarchicalResource(output.genStylerFactoryFile()));
        genStylerFactoryFile.assertName("TigerStylerFactory.java");
        genStylerFactoryFile.assertExists();
        genStylerFactoryFile.assertContains("class TigerStylerFactory");
        genStylerFactoryFile.assertJavaParses(javaParser);
    }
}
