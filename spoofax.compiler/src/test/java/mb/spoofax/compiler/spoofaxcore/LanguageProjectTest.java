package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofaxcore.util.TigerInputs;
import mb.spoofax.compiler.util.JavaProject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageProjectTest {
    @Test void testPersistentProperties() {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Properties persistentProperties = new Properties();

        final Shared shared1 = TigerInputs.shared(baseDirectory);
        final LanguageProject.Input languageProjectInput1 = TigerInputs.languageProject(shared1);
        final JavaProject languageProject1 = languageProjectInput1.project();
        assertEquals(shared1.defaultGroupId(), languageProject1.coordinate().groupId());
        assertEquals(shared1.defaultArtifactId() + ".lang", languageProject1.coordinate().artifactId());
        assertEquals(shared1.defaultVersion(), languageProject1.coordinate().version());
        assertEquals(shared1.basePackageId(), languageProject1.packageId());
        shared1.savePersistentProperties(persistentProperties);

        final Shared shared2 = TigerInputs.sharedBuilder(baseDirectory)
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();
        final LanguageProject.Input languageProjectInput2 = TigerInputs.languageProject(shared2);
        final JavaProject languageProject2 = languageProjectInput2.project();
        // Should not affect language project.
        assertEquals(shared2.defaultGroupId(), languageProject2.coordinate().groupId());
        assertEquals(shared2.defaultArtifactId() + ".lang", languageProject2.coordinate().artifactId());
        assertEquals(shared2.defaultVersion(), languageProject2.coordinate().version());
        assertEquals(shared2.basePackageId(), languageProject2.packageId());
    }

    @Test void testCompiler() throws IOException {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final LanguageProject.Input input = TigerInputs.languageProject(TigerInputs.shared(baseDirectory));

        final LanguageProject compiler = LanguageProject.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final LanguageProject.Output output = compiler.compile(input, charset);

        final HierarchicalResource settingsGradleKtsFile = resourceService.getHierarchicalResource(output.settingsGradleKtsFile());
        assertTrue(settingsGradleKtsFile.exists());

        final HierarchicalResource buildGradleKtsFile = resourceService.getHierarchicalResource(output.buildGradleKtsFile());
        assertTrue(buildGradleKtsFile.exists());
        assertTrue(buildGradleKtsFile.readString(charset).contains("mb/tiger"));
    }
}
