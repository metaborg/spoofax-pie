package mb.spoofax.dynamicloading;

import mb.common.option.Option;
import mb.common.style.Color;
import mb.common.style.Style;
import mb.common.style.Styling;
import mb.common.style.TokenStyle;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.runtime.tracer.MetricsTracer;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DynamicLanguageTest extends CharsTestBase {
    @BeforeEach void setup(@TempDir Path temporaryDirectoryPath) throws IOException {
        super.setup(temporaryDirectoryPath);
    }

    @AfterEach void teardown() throws Exception {
        super.teardown();
    }

    @Test void testLanguageSpecificationChanges() throws Exception {
        {
            final DynamicLanguage dynamicLanguage = dynamicLoader.load("chars", input);
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = languageComponent.getPie().newSession()) {
                final Option<Styling> result = session.require(languageComponent.getLanguageInstance().createStyleTask(charsFile.getPath()));
                // Check styling.
                assertTrue(result.isSome());
                final Styling styling = result.unwrap();
                final ArrayList<TokenStyle> stylingPerToken = styling.getStylePerToken();
                assertEquals(1, stylingPerToken.size());
                final Style style = stylingPerToken.get(0).getStyle();
                assertNotNull(style.getColor());
                assertNull(style.getBackgroundColor());
                assertEquals(new Color(0, 0, 150), style.getColor());
                assertTrue(style.isBold());
                assertFalse(style.isItalic());
                assertFalse(style.isUnderscore());
                assertFalse(style.isStrikeout());
            } catch(ExecException e) {
                logAndRethrow(e);
            }
        }

        {
            // Change the language specification: change the styler.
            modifyStyler();
            final DynamicLanguage dynamicLanguage = dynamicLoader.load("chars", input);
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = languageComponent.getPie().newSession()) {
                metricsTracer.reset();
                final Option<Styling> result = session.require(languageComponent.getLanguageInstance().createStyleTask(charsFile.getPath()));
                final MetricsTracer.Report report = metricsTracer.reportAndReset();
                // Check that styling task has been executed, and parser task has not.
                assertTrue(report.executedPerTaskDefinition.containsKey(adapterProjectInput.styler().get().styleTaskDef().qualifiedId()));
                assertFalse(report.executedPerTaskDefinition.containsKey(adapterProjectInput.parser().get().parseTaskDef().qualifiedId()));
                // Check styling.
                assertTrue(result.isSome());
                final Styling styling = result.unwrap();
                final ArrayList<TokenStyle> stylingPerToken = styling.getStylePerToken();
                assertEquals(1, stylingPerToken.size());
                final Style style = stylingPerToken.get(0).getStyle();
                assertNotNull(style.getColor());
                assertNull(style.getBackgroundColor());
                assertEquals(new Color(255, 255, 0), style.getColor());
                assertFalse(style.isBold());
                assertTrue(style.isItalic());
                assertFalse(style.isUnderscore());
                assertFalse(style.isStrikeout());
            } catch(ExecException e) {
                logAndRethrow(e);
            }
        }

        {
            // Change the language specification: change the parser.
            final Set<ResourceKey> providedResources = modifyParser();
            final DynamicLanguage dynamicLanguage = dynamicLoader.load("chars", input);
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = languageComponent.getPie().newSession()) {
                metricsTracer.reset();
                session.updateAffectedBy(providedResources);
                final MetricsTracer.Report report = metricsTracer.reportAndReset();
                // Check that parser task has been executed, and styler task has not.
                assertTrue(report.executedPerTaskDefinition.containsKey(adapterProjectInput.parser().get().parseTaskDef().qualifiedId()));
                assertFalse(report.executedPerTaskDefinition.containsKey(adapterProjectInput.styler().get().styleTaskDef().qualifiedId()));
            } catch(ExecException e) {
                logAndRethrow(e);
            }
        }
    }

    @Test void testCloseLanguage() throws Exception {
        // Load language.
        DynamicLanguage lang1a = dynamicLoader.load("chars", input);
        // Dynamic language 1 has not yet been closed.
        assertNotNull(lang1a.getClassLoader());
        assertNotNull(lang1a.getLanguageComponent());
        assertFalse(lang1a.isClosed());
        // Load language again, but nothing has changed.
        DynamicLanguage lang1b = dynamicLoader.load("chars", input);
        // Dynamic language 1b has not yet been closed.
        assertNotNull(lang1b.getClassLoader());
        assertNotNull(lang1b.getLanguageComponent());
        assertFalse(lang1b.isClosed());
        // Modify language specification.
        modifyStyler();
        // Reload language.
        DynamicLanguage lang2 = dynamicLoader.load("chars", input);
        // Dynamic language 2 has not yet been closed.
        assertNotNull(lang2.getClassLoader());
        assertNotNull(lang2.getLanguageComponent());
        assertFalse(lang2.isClosed());
        // Dynamic language 1a and 1b should be closed.
        assertThrows(IllegalStateException.class, lang1a::getClassLoader);
        assertThrows(IllegalStateException.class, lang1a::getLanguageComponent);
        assertTrue(lang1a.isClosed());
        lang1a = null;
        assertThrows(IllegalStateException.class, lang1b::getClassLoader);
        assertThrows(IllegalStateException.class, lang1b::getLanguageComponent);
        assertTrue(lang1b.isClosed());
        lang1b = null;
        // Modify language specification
        modifyParser();
        // Reload language.
        DynamicLanguage lang3 = dynamicLoader.load("chars", input);
        // Dynamic language 3 has not yet been closed.
        assertNotNull(lang3.getClassLoader());
        assertNotNull(lang3.getLanguageComponent());
        assertFalse(lang3.isClosed());
        // Dynamic language 2 should be closed.
        assertThrows(IllegalStateException.class, lang2::getClassLoader);
        assertThrows(IllegalStateException.class, lang2::getLanguageComponent);
        assertTrue(lang2.isClosed());
        lang2 = null;
        // Unload dynamic language.
        dynamicLoader.unload("chars");
        // Dynamic language 3 should be closed.
        assertThrows(IllegalStateException.class, lang3::getClassLoader);
        assertThrows(IllegalStateException.class, lang3::getLanguageComponent);
        assertTrue(lang3.isClosed());
        lang3 = null;
        // Cleanup cache.
        dynamicLoader.deleteCacheForUnloadedLanguages();
    }
}
