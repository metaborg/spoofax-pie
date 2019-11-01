package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SharedTest {
    @Test
    void testPersistentProperties() {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("tiger"));

        final Properties persistentProperties = new Properties();

        final Shared shared1 = CommonInputs.tigerShared(baseDirectory);
        assertEquals("Tiger", shared1.classSuffix());
        shared1.savePersistentProperties(persistentProperties);

        final Shared shared2 = CommonInputs.tigerSharedBuilder(baseDirectory)
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();
        // Should not affect class suffix.
        assertEquals("Tiger", shared2.classSuffix());
    }
}
