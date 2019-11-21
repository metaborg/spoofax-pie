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

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintAnalyzerTest {
    @Test void testCompilerDefault() throws IOException {
        final JavaParser javaParser = new JavaParser();
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = CommonInputs.tigerShared(baseDirectory);
        final JavaProject languageProject = CommonInputs.tigerLanguageProjectCompilerInput(shared).project();
        final ConstraintAnalyzer.Input input = CommonInputs.tigerConstraintAnalyzerCompilerInput(shared, languageProject);

        final ConstraintAnalyzer compiler = ConstraintAnalyzer.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final ConstraintAnalyzer.Output output = compiler.compile(input, charset);

        final HierarchicalResource genSourcesJavaDirectory = resourceService.getHierarchicalResource(output.genSourcesJavaDirectory());
        assertTrue(genSourcesJavaDirectory.exists());

        final FileAssertions genConstraintAnalyzerFile = new FileAssertions(resourceService.getHierarchicalResource(output.genConstraintAnalyzerFile()));
        genConstraintAnalyzerFile.assertName("TigerConstraintAnalyzer.java");
        genConstraintAnalyzerFile.assertExists();
        genConstraintAnalyzerFile.assertContains("class TigerConstraintAnalyzer");
        genConstraintAnalyzerFile.assertJavaParses(javaParser);

        final FileAssertions genConstraintAnalyzerFactoryFile = new FileAssertions(resourceService.getHierarchicalResource(output.genFactoryFile()));
        genConstraintAnalyzerFactoryFile.assertName("TigerConstraintAnalyzerFactory.java");
        genConstraintAnalyzerFactoryFile.assertExists();
        genConstraintAnalyzerFactoryFile.assertContains("class TigerConstraintAnalyzerFactory");
        genConstraintAnalyzerFactoryFile.assertJavaParses(javaParser);
    }
}
