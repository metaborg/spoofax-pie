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
        final GradleProject adapterProject1 = shared1.adapterProject();
        shared1.savePersistentProperties(persistentProperties);

        // Create shared configuration with different language name.
        final Shared shared2 = TigerInputs.sharedBuilder(baseDirectory)
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();

        // Should not affect class suffix.
        assertEquals(shared1.classSuffix(), shared2.classSuffix());

        // Should not affect language project.
        assertEquals(languageProject1.coordinate(), shared2.languageProject().coordinate());

        // Should not affect adapter project.
        assertEquals(adapterProject1.coordinate(), shared2.adapterProject().coordinate());
    }
}
