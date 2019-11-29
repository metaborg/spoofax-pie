package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.util.TigerInputs;
import mb.spoofax.compiler.util.GradleProject;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SharedTest {
    @Test void testPersistentProperties() {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Properties persistentProperties = new Properties();

        final Shared shared1 = TigerInputs.shared(baseDirectory);
        assertEquals("Tiger", shared1.classSuffix());
        final GradleProject languageProject1 = shared1.languageProject();
        assertEquals(shared1.defaultGroupId(), languageProject1.coordinate().groupId());
        assertEquals(shared1.defaultArtifactId() + ".lang", languageProject1.coordinate().artifactId());
        assertEquals(shared1.defaultVersion(), languageProject1.coordinate().version());
        assertEquals(shared1.basePackageId() + ".lang", languageProject1.packageId());
        shared1.savePersistentProperties(persistentProperties);

        final Shared shared2 = TigerInputs.sharedBuilder(baseDirectory)
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();
        // Should not affect class suffix.
        assertEquals("Tiger", shared2.classSuffix());
        final GradleProject languageProject2 = shared2.languageProject();
        // Should not affect language project.
        assertEquals(shared2.defaultGroupId(), languageProject2.coordinate().groupId());
        assertEquals(shared2.defaultArtifactId() + ".lang", languageProject2.coordinate().artifactId());
        assertEquals(shared2.defaultVersion(), languageProject2.coordinate().version());
        assertEquals(shared2.basePackageId() + ".lang", languageProject2.packageId());
    }
}
