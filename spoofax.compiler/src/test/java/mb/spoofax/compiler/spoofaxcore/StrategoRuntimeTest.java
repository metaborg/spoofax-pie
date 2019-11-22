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

import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategoRuntimeTest {
    @Test void testCompilerDefault() throws IOException {
        final JavaParser javaParser = new JavaParser();
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Shared shared = TigerInputs.shared(baseDirectory);
        final JavaProject languageProject = TigerInputs.languageProject(shared).project();
        final StrategoRuntime.Input input = TigerInputs.strategoRuntime(shared, languageProject);

        final StrategoRuntime compiler = StrategoRuntime.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final StrategoRuntime.Output output = compiler.compile(input, charset);

        final HierarchicalResource packageDirectory = resourceService.getHierarchicalResource(output.genSourcesJavaDirectory());
        assertTrue(packageDirectory.exists());

        final FileAssertions genFactoryFile = new FileAssertions(resourceService.getHierarchicalResource(output.genFactoryFile()));
        genFactoryFile.assertName("TigerStrategoRuntimeBuilderFactory.java");
        genFactoryFile.assertExists();
        genFactoryFile.assertContains("class TigerStrategoRuntimeBuilderFactory");
        genFactoryFile.assertJavaParses(javaParser);
    }
}
