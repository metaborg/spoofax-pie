package mb.spoofax.compiler.spoofaxcore;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.resource.fs.FSPath;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class SharedTest {
    @Test void testPersistentProperties() {
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        final FSPath baseDirectory = new FSPath(fileSystem.getPath("repo"));

        final Properties persistentProperties = new Properties();

        final Shared shared1 = TigerInputs.shared().build();
        assertEquals("Tiger", shared1.defaultClassPrefix());
        final LanguageProject languageProject1 = TigerInputs.languageProject(baseDirectory, shared1).build();
        final AdapterProject adapterProject1 = TigerInputs.adapterProject(baseDirectory, shared1).build();
        shared1.savePersistentProperties(persistentProperties);

        // Create shared configuration with different language name.
        final Shared shared2 = TigerInputs.shared()
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();

        // Should not affect class suffix.
        assertEquals(shared1.defaultClassPrefix(), shared2.defaultClassPrefix());

        // Should not affect language project.
        final LanguageProject languageProject2 = TigerInputs.languageProject(baseDirectory, shared2).build();
        assertEquals(languageProject1.project().coordinate(), languageProject2.project().coordinate());

        // Should not affect adapter project.
        final AdapterProject adapterProject2 = TigerInputs.adapterProject(baseDirectory, shared2).build();
        assertEquals(adapterProject1.project().coordinate(), adapterProject2.project().coordinate());
    }
}
