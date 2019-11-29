package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofaxcore.util.TigerInputs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RootProjectTest {
    @Test void testCompilerDefault() throws IOException {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final RootProject.Input input = TigerInputs.rootProject(TigerInputs.shared(baseDirectory));

        final RootProject compiler = RootProject.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final RootProject.Output output = compiler.compile(input, charset);

        final HierarchicalResource settingsGradleKtsFile = resourceService.getHierarchicalResource(output.settingsGradleKtsFile());
        assertTrue(settingsGradleKtsFile.exists());
        assertTrue(settingsGradleKtsFile.readString(charset).contains(input.name()));

        final HierarchicalResource buildGradleKtsFile = resourceService.getHierarchicalResource(output.buildGradleKtsFile());
        assertTrue(buildGradleKtsFile.exists());
        assertTrue(buildGradleKtsFile.readString(charset).contains("org.metaborg.gradle.config.root-project"));
    }
}
