package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.util.ResourceDependencies;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageProjectCompilerTest {
    @Test
    void testPersistentProperties() {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("tiger"));

        final Properties persistentProperties = new Properties();

        final Shared shared1 = CommonInputs.tigerShared(baseDirectory);
        final LanguageProjectCompilerInput languageProjectInput1 = CommonInputs.tigerLanguageProjectCompilerInput(shared1);
        final JavaProject languageProject1 = languageProjectInput1.project();
        assertEquals(shared1.defaultGroupId(), languageProject1.coordinate().groupId());
        assertEquals(shared1.defaultArtifactId(), languageProject1.coordinate().artifactId());
        assertEquals(shared1.defaultVersion(), languageProject1.coordinate().version());
        assertEquals(shared1.basePackageId(), languageProject1.packageId());
        shared1.savePersistentProperties(persistentProperties);

        final Shared shared2 = CommonInputs.tigerSharedBuilder(baseDirectory)
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();
        final LanguageProjectCompilerInput languageProjectInput2 = CommonInputs.tigerLanguageProjectCompilerInput(shared2);
        final JavaProject languageProject2 = languageProjectInput2.project();
        // Should not affect language project.
        assertEquals(shared2.defaultGroupId(), languageProject2.coordinate().groupId());
        assertEquals(shared2.defaultArtifactId(), languageProject2.coordinate().artifactId());
        assertEquals(shared2.defaultVersion(), languageProject2.coordinate().version());
        assertEquals(shared2.basePackageId(), languageProject2.packageId());
    }

    @Test
    void testCompiler() throws IOException {
        final ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("tiger"));

        final LanguageProjectCompilerInput input = CommonInputs.tigerLanguageProjectCompilerInput(CommonInputs.tigerShared(baseDirectory));

        final LanguageProjectCompiler compiler = LanguageProjectCompiler.fromClassLoaderResources(resourceService);
        final Charset charset = StandardCharsets.UTF_8;
        final ResourceDependencies resourceDependencies = compiler.compile(input, charset);

        final HierarchicalResource buildGradleKtsFile = compiler.getBuildGradleKtsFile(input);
        assertTrue(buildGradleKtsFile.exists());
        assertTrue(resourceDependencies.providedResources().contains(buildGradleKtsFile));
        assertTrue(buildGradleKtsFile.readString(charset).contains("mb/tiger"));
    }
}
