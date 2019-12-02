package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofaxcore.util.FileAssertions;
import mb.spoofax.compiler.spoofaxcore.util.JavaParser;
import mb.spoofax.compiler.spoofaxcore.util.TigerInputs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintAnalyzerTest {
    @Test void testCompilerDefault() throws IOException {
        final JavaParser javaParser = new JavaParser();
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = TigerInputs.shared(baseDirectory);
        final ConstraintAnalyzer.Input input = TigerInputs.constraintAnalyzer(shared);

        final Charset charset = StandardCharsets.UTF_8;
        final ConstraintAnalyzer compiler = ConstraintAnalyzer.fromClassLoaderResources(resourceService, charset);
        final ConstraintAnalyzer.LanguageProjectOutput output = compiler.compileLanguageProject(input);

        final HierarchicalResource genDirectory = resourceService.getHierarchicalResource(input.genDirectory());
        assertTrue(genDirectory.exists());

        final FileAssertions genConstraintAnalyzerFile = new FileAssertions(resourceService.getHierarchicalResource(input.genConstraintAnalyzerFile()));
        genConstraintAnalyzerFile.assertName("TigerConstraintAnalyzer.java");
        genConstraintAnalyzerFile.assertExists();
        genConstraintAnalyzerFile.assertContains("class TigerConstraintAnalyzer");
        genConstraintAnalyzerFile.assertJavaParses(javaParser);

        final FileAssertions genConstraintAnalyzerFactoryFile = new FileAssertions(resourceService.getHierarchicalResource(input.genFactoryFile()));
        genConstraintAnalyzerFactoryFile.assertName("TigerConstraintAnalyzerFactory.java");
        genConstraintAnalyzerFactoryFile.assertExists();
        genConstraintAnalyzerFactoryFile.assertContains("class TigerConstraintAnalyzerFactory");
        genConstraintAnalyzerFactoryFile.assertJavaParses(javaParser);
    }
}
