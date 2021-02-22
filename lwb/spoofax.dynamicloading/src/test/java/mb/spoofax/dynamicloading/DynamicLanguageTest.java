package mb.spoofax.dynamicloading;

import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.style.Color;
import mb.common.style.Style;
import mb.common.style.Styling;
import mb.common.style.TokenStyle;
import mb.common.token.Token;
import mb.common.token.TokenType;
import mb.common.token.Tokens;
import mb.common.util.ListView;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.runtime.tracer.MetricsTracer;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
            System.err.println("Initial dynamic load");
            final DynamicLanguage dynamicLanguage;
            try(final DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
                dynamicLanguage = session.reload("chars", input);
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }

            System.err.println("Initial test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
                metricsTracer.reset();
                // Get tokens and check.
                final Option<? extends Tokens<?>> tokensResult = session.require(languageInstance.createTokenizeTask(charsFilePath));
                assertTrue(tokensResult.isSome());
                final ArrayList<? extends Token<?>> tokens = tokensResult.unwrap().getTokens();
                assertEquals(1, tokens.size());
                final Token<?> token = tokens.get(0);
                assertEquals(TokenType.identifier(), token.getType());
                assertEquals(Region.fromOffsets(0, 7, 0), token.getRegion());
                // Get styling and check.
                final Option<Styling> stylingResult = session.require(languageInstance.createStyleTask(charsFilePath));
                assertTrue(stylingResult.isSome());
                final Styling styling = stylingResult.unwrap();
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
                // Run check task and check.
                final KeyedMessages messages = session.require(languageInstance.createCheckTask(charsProjectPath));
                assertFalse(messages.containsError());
                // Run command and check.
                final Task<CommandFeedback> debugRemoveATask = getTaskForFirstCommand(languageInstance);
                final CommandFeedback debugRemoveAFeedback = session.require(debugRemoveATask);
                assertFalse(debugRemoveAFeedback.hasErrorMessagesOrException());
                final ListView<ShowFeedback> debugRemoveAShowFeedbacks = debugRemoveAFeedback.getShowFeedbacks();
                assertEquals(1, debugRemoveAShowFeedbacks.size());
                assertEquals("Program([Chars(\"abcdefg\")])", debugRemoveAShowFeedbacks.get(0).getText().get()); // remove-a does not work yet.
                // Check executed tasks.
                final MetricsTracer.Report report = metricsTracer.reportAndReset();
                assertTrue(hasTokenizeTaskDefExecuted(report));
                assertTrue(hasParseTaskDefExecuted(report));
                assertTrue(hasStyleTaskDefExecuted(report));
                assertTrue(hasRemoveATaskDefExecuted(report));
                assertTrue(hasDebugRemoveATaskDefExecuted(report));
                assertTrue(hasConstraintAnalysisTaskExecuted(report));
                assertTrue(hasCheckTaskExecuted(report));
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }
        }

        {
            System.err.println("Change styler and reload");
            final DynamicLanguage dynamicLanguage;
            try(final DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
                dynamicLanguage = modifyStyler(session).reload("chars", input);
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }

            System.err.println("Change styler test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                metricsTracer.reset();
                final Option<Styling> result = session.require(languageComponent.getLanguageInstance().createStyleTask(charsFile.getPath()));
                // Check executed tasks.
                final MetricsTracer.Report report = metricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report));
                assertFalse(hasParseTaskDefExecuted(report));
                assertTrue(hasStyleTaskDefExecuted(report));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertFalse(hasDebugRemoveATaskDefExecuted(report));
                assertFalse(hasConstraintAnalysisTaskExecuted(report));
                assertFalse(hasCheckTaskExecuted(report));
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
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }
        }

        {
            System.err.println("Change parser and reload");
            final DynamicLanguage dynamicLanguage;
            final Set<ResourceKey> providedResources;
            try(final DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
                final DynamicLoaderReloadSession reloadSession = modifyParser(session);
                dynamicLanguage = reloadSession.reload("chars", input);
                providedResources = reloadSession.getProvidedResources();
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }

            System.err.println("Change parser test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                metricsTracer.reset();
                session.updateAffectedBy(providedResources);
                // Check executed tasks.
                final MetricsTracer.Report report = metricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report));
                assertTrue(hasParseTaskDefExecuted(report));
                assertFalse(hasStyleTaskDefExecuted(report));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertFalse(hasDebugRemoveATaskDefExecuted(report));
                assertFalse(hasConstraintAnalysisTaskExecuted(report));
                assertFalse(hasCheckTaskExecuted(report));
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }
        }

        {
            System.err.println("Change transformation and reload");
            final DynamicLanguage dynamicLanguage;
            final Set<ResourceKey> providedResources;
            try(final DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
                final DynamicLoaderReloadSession reloadSession = modifyTransformation(session);
                dynamicLanguage = reloadSession.reload("chars", input);
                providedResources = reloadSession.getProvidedResources();
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }

            System.err.println("Change transformation test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                metricsTracer.reset();
                final TopDownSession topDownSession = session.updateAffectedBy(providedResources);
                // Run command and check.
                final Task<CommandFeedback> debugRemoveATask = getTaskForFirstCommand(languageComponent.getLanguageInstance());
                final CommandFeedback debugRemoveAFeedback = topDownSession.require(debugRemoveATask);
                assertFalse(debugRemoveAFeedback.hasErrorMessagesOrException());
                final ListView<ShowFeedback> debugRemoveAShowFeedbacks = debugRemoveAFeedback.getShowFeedbacks();
                assertEquals(1, debugRemoveAShowFeedbacks.size());
                assertEquals("Program([Chars(\"bcdefg\")])", debugRemoveAShowFeedbacks.get(0).getText().get()); // remove-a works now.
                // Check executed tasks.
                final MetricsTracer.Report report = metricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report));
                assertFalse(hasParseTaskDefExecuted(report));
                assertFalse(hasStyleTaskDefExecuted(report));
                assertTrue(hasRemoveATaskDefExecuted(report));
                assertTrue(hasDebugRemoveATaskDefExecuted(report));
                assertTrue(hasConstraintAnalysisTaskExecuted(report)); // Unfortunately this is re-executed, because the Stratego runtime has changed. We cannot know if this affects the constraint analysis or not.
                assertTrue(hasCheckTaskExecuted(report)); // TODO: this is executed because the result of constraint analysis changes, but we are only interested in the messages, which do not change.
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }
        }

        {
            System.err.println("Change command and reload");
            final DynamicLanguage dynamicLanguage;
            final Set<ResourceKey> providedResources;
            try(final DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
                final DynamicLoaderReloadSession reloadSession = modifyCommand(session);
                dynamicLanguage = reloadSession.reload("chars", input);
                providedResources = reloadSession.getProvidedResources();
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }

            System.err.println("Change command test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                metricsTracer.reset();
                final TopDownSession topDownSession = session.updateAffectedBy(providedResources);
                // Run command and check.
                final Task<CommandFeedback> debugRemoveATask = getTaskForFirstCommand(languageComponent.getLanguageInstance());
                final CommandFeedback debugRemoveAFeedback = topDownSession.require(debugRemoveATask);
                assertFalse(debugRemoveAFeedback.hasErrorMessagesOrException());
                final ListView<ShowFeedback> debugRemoveAShowFeedbacks = debugRemoveAFeedback.getShowFeedbacks();
                assertEquals(1, debugRemoveAShowFeedbacks.size());
                assertTrue(debugRemoveAShowFeedbacks.get(0).getName().get().contains("'A' characters removed from"));
                // Check executed tasks.
                final MetricsTracer.Report report = metricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report));
                assertFalse(hasParseTaskDefExecuted(report));
                assertFalse(hasStyleTaskDefExecuted(report));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertTrue(hasDebugRemoveATaskDefExecuted(report));
                assertFalse(hasConstraintAnalysisTaskExecuted(report));
                assertFalse(hasCheckTaskExecuted(report));
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }
        }

        {
            System.err.println("Change analyzer and reload");
            final DynamicLanguage dynamicLanguage;
            final Set<ResourceKey> providedResources;
            try(final DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
                final DynamicLoaderReloadSession reloadSession = modifyAnalyzer(session);
                dynamicLanguage = reloadSession.reload("chars", input);
                providedResources = reloadSession.getProvidedResources();
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }

            System.err.println("Change analyzer test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                metricsTracer.reset();
                final TopDownSession topDownSession = session.updateAffectedBy(providedResources);
                // Run check task and check.
                final KeyedMessages messages = topDownSession.require(languageComponent.getLanguageInstance().createCheckTask(charsProjectPath));
                assertTrue(messages.containsError());
                // Check executed tasks.
                final MetricsTracer.Report report = metricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report));
                assertFalse(hasParseTaskDefExecuted(report));
                assertFalse(hasStyleTaskDefExecuted(report));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertFalse(hasDebugRemoveATaskDefExecuted(report));
                assertTrue(hasConstraintAnalysisTaskExecuted(report));
                assertTrue(hasCheckTaskExecuted(report));
                // TODO: check execution here and in previous steps.
            } catch(ExecException | RuntimeException e) {
                logException(e);
                throw e;
            }
        }

        try(final DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
            System.err.println("Unload language");
            session.unload("chars");
            session.deleteCacheForUnloadedLanguages();
        }
    }

    @Disabled @Test void testCloseLanguage() throws Exception {
        DynamicLanguage lang1a;
        try(DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
            // Load language.
            lang1a = session.reload("chars", input);
            // Dynamic language 1 has not yet been closed.
            assertNotNull(lang1a.getClassLoader());
            assertNotNull(lang1a.getLanguageComponent());
            assertFalse(lang1a.isClosed());
        }

        DynamicLanguage lang1b;
        try(DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
            // Load language again, but nothing has changed.
            lang1b = session.reload("chars", input);
            // Dynamic language 1b has not yet been closed.
            assertNotNull(lang1b.getClassLoader());
            assertNotNull(lang1b.getLanguageComponent());
            assertFalse(lang1b.isClosed());
        }

        DynamicLanguage lang2;
        try(DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
            // Modify language specification and reload language.
            lang2 = modifyStyler(session).reload("chars", input);
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
        }

        DynamicLanguage lang3;
        try(DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
            // Modify language specification and reload language.
            lang3 = modifyParser(session).reload("chars", input);
            // Dynamic language 3 has not yet been closed.
            assertNotNull(lang3.getClassLoader());
            assertNotNull(lang3.getLanguageComponent());
            assertFalse(lang3.isClosed());
            // Dynamic language 2 should be closed.
            assertThrows(IllegalStateException.class, lang2::getClassLoader);
            assertThrows(IllegalStateException.class, lang2::getLanguageComponent);
            assertTrue(lang2.isClosed());
            lang2 = null;
        }

        try(DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
            // Unload dynamic language.
            session.unload("chars");
            // Dynamic language 3 should be closed.
            assertThrows(IllegalStateException.class, lang3::getClassLoader);
            assertThrows(IllegalStateException.class, lang3::getLanguageComponent);
            assertTrue(lang3.isClosed());
            lang3 = null;
        }

        try(DynamicLoaderMixedSession session = dynamicLoader.newSession()) {
            // Cleanup cache.
            session.deleteCacheForUnloadedLanguages();
        }
    }
}
