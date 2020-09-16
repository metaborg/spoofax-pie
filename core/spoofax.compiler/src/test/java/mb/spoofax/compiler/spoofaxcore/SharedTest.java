package mb.spoofax.compiler.spoofaxcore;

import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.spoofaxcore.tiger.TigerInputs;
import mb.spoofax.compiler.util.Shared;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class SharedTest extends TestBase {
    @Test void testPersistentProperties() {
        final Properties persistentProperties = new Properties();

        final Shared shared1 = TigerInputs.sharedBuilder().build();
        final TigerInputs inputs1 = defaultInputs(shared1);
        assertEquals("Tiger", shared1.defaultClassPrefix());
        final LanguageProject languageProject1 = inputs1.languageProjectCompilerInput().languageProject();
        final AdapterProject adapterProject1 = inputs1.adapterProjectCompilerInput().adapterProject();
        shared1.savePersistentProperties(persistentProperties);

        // Create shared configuration with different language name.
        final Shared shared2 = TigerInputs.sharedBuilder()
            .name("Tigerr") // Change language name.
            .withPersistentProperties(persistentProperties)
            .build();
        final TigerInputs inputs2 = defaultInputs(shared2);

        // Should not affect class suffix.
        assertEquals(shared1.defaultClassPrefix(), shared2.defaultClassPrefix());

        // Should not affect language project.
        final LanguageProject languageProject2 = inputs2.languageProjectCompilerInput().languageProject();
        assertEquals(languageProject1.project().coordinate(), languageProject2.project().coordinate());

        // Should not affect adapter project.
        final AdapterProject adapterProject2 = inputs2.adapterProjectCompilerInput().adapterProject();
        assertEquals(adapterProject1.project().coordinate(), adapterProject2.project().coordinate());
    }
}
