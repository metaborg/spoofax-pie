package mb.spoofax.lwb.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.common.editor.HoverResult;
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
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DynamicLoadingTest extends CharsTestBase {
    @BeforeEach void setup(@TempDir Path temporaryDirectoryPath) throws IOException {
        super.setup(temporaryDirectoryPath);
    }

    @AfterEach void teardown() throws Exception {
        super.teardown();
    }

    @Test void testLanguageCompilationAndDynamicLoading() throws Exception {
        CompileLanguageInput previousInput;

        {
            System.out.println("Initial dynamic load");
            final DynamicLanguage dynamicLanguage;
            try(final MixedSession session = newSession()) {
                dynamicLanguage = requireDynamicLoad(session, rootDirectoryPath);
                previousInput = dynamicLanguage.getCompileInput();
                final KeyedMessages sptMessages = requireSptCheck(session, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Initial test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
                languageMetricsTracer.reset();
                // Get tokens and check.
                final Option<? extends Tokens<?>> tokensResult = session.require(languageInstance.createTokenizeTask(charsFilePath));
                assertTrue(tokensResult.isSome());
                final ArrayList<? extends Token<?>> tokens = tokensResult.unwrap().getTokens();
                assertEquals(1, tokens.size());
                final Token<?> token = tokens.get(0);
                assertEquals(TokenType.identifier(), token.getType());
                assertEquals(Region.fromOffsets(0, 7, 0), token.getRegion());
                // Get styling and check.
                final Option<Styling> stylingResult = session.require(languageInstance.createStyleTask(charsFilePath, rootDirectoryPath));
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
                final Task<CommandFeedback> debugRemoveATask = getTaskForRemoveACommand(languageInstance);
                final CommandFeedback debugRemoveAFeedback = session.require(debugRemoveATask);
                assertFalse(debugRemoveAFeedback.hasErrorMessagesOrException());
                final ListView<ShowFeedback> debugRemoveAShowFeedbacks = debugRemoveAFeedback.getShowFeedbacks();
                assertEquals(1, debugRemoveAShowFeedbacks.size());
                assertEquals("Program([Chars(\"abcdefg\")])", debugRemoveAShowFeedbacks.get(0).getText().get()); // remove-a does not work yet.
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertTrue(hasTokenizeTaskDefExecuted(report, dynamicLanguage));
                assertTrue(hasParseTaskDefExecuted(report, dynamicLanguage));
                assertTrue(hasStyleTaskDefExecuted(report, dynamicLanguage));
                assertTrue(hasRemoveATaskDefExecuted(report));
                assertTrue(hasDebugRemoveATaskDefExecuted(report));
                assertTrue(hasConstraintAnalysisTaskExecuted(report, dynamicLanguage));
                assertTrue(hasCheckTaskExecuted(report, dynamicLanguage));
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Change styler and reload");
            final DynamicLanguage dynamicLanguage;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = modifyStyler(session, previousInput);
                dynamicLanguage = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = dynamicLanguage.getCompileInput();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Change styler test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                languageMetricsTracer.reset();
                final Option<Styling> result = session.require(languageComponent.getLanguageInstance().createStyleTask(charsFile.getPath(), rootDirectoryPath));
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasParseTaskDefExecuted(report, dynamicLanguage));
                assertTrue(hasStyleTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertFalse(hasDebugRemoveATaskDefExecuted(report));
                assertFalse(hasConstraintAnalysisTaskExecuted(report, dynamicLanguage));
                assertFalse(hasCheckTaskExecuted(report, dynamicLanguage));
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
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Change parser and reload");
            final DynamicLanguage dynamicLanguage;
            final Set<ResourceKey> providedResources;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = modifyParser(session, previousInput);
                dynamicLanguage = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = dynamicLanguage.getCompileInput();
                providedResources = topDownSession.getProvidedResources();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Change parser test");
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                languageMetricsTracer.reset();
                session.updateAffectedBy(providedResources);
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report, dynamicLanguage));
                assertTrue(hasParseTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasStyleTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertFalse(hasDebugRemoveATaskDefExecuted(report));
                assertFalse(hasConstraintAnalysisTaskExecuted(report, dynamicLanguage));
                assertFalse(hasCheckTaskExecuted(report, dynamicLanguage));
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Change transformation and reload");
            final DynamicLanguage dynamicLanguage;
            final Set<ResourceKey> providedResources;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = modifyTransformation(session, previousInput);
                dynamicLanguage = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = dynamicLanguage.getCompileInput();
                providedResources = topDownSession.getProvidedResources();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Change transformation test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                languageMetricsTracer.reset();
                final TopDownSession topDownSession = session.updateAffectedBy(providedResources);
                // Run command and check.
                final Task<CommandFeedback> debugRemoveATask = getTaskForRemoveACommand(languageComponent.getLanguageInstance());
                final CommandFeedback debugRemoveAFeedback = topDownSession.require(debugRemoveATask);
                assertFalse(debugRemoveAFeedback.hasErrorMessagesOrException());
                final ListView<ShowFeedback> debugRemoveAShowFeedbacks = debugRemoveAFeedback.getShowFeedbacks();
                assertEquals(1, debugRemoveAShowFeedbacks.size());
                assertEquals("Program([Chars(\"bcdefg\")])", debugRemoveAShowFeedbacks.get(0).getText().get()); // remove-a works now.
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasParseTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasStyleTaskDefExecuted(report, dynamicLanguage));
                assertTrue(hasRemoveATaskDefExecuted(report));
                assertTrue(hasDebugRemoveATaskDefExecuted(report));
                assertTrue(hasConstraintAnalysisTaskExecuted(report, dynamicLanguage)); // Unfortunately this is re-executed, because the Stratego runtime has changed. We cannot know if this affects the constraint analysis or not.
                assertTrue(hasCheckTaskExecuted(report, dynamicLanguage)); // TODO: this executes because the analyze task is executed because it depends on a Stratego runtime. However, the messages that the analyze task returns should be unchanged, so we need to put an output stamper on that to prevent re-execution.
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Change command and reload");
            final DynamicLanguage dynamicLanguage;
            final Set<ResourceKey> providedResources;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = modifyCommand(session, previousInput);
                dynamicLanguage = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = dynamicLanguage.getCompileInput();
                providedResources = topDownSession.getProvidedResources();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Change command test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                languageMetricsTracer.reset();
                final TopDownSession topDownSession = session.updateAffectedBy(providedResources);
                // Run command and check.
                final Task<CommandFeedback> debugRemoveATask = getTaskForRemoveACommand(languageComponent.getLanguageInstance());
                final CommandFeedback debugRemoveAFeedback = topDownSession.require(debugRemoveATask);
                assertFalse(debugRemoveAFeedback.hasErrorMessagesOrException());
                final ListView<ShowFeedback> debugRemoveAShowFeedbacks = debugRemoveAFeedback.getShowFeedbacks();
                assertEquals(1, debugRemoveAShowFeedbacks.size());
                assertTrue(debugRemoveAShowFeedbacks.get(0).getName().get().contains("'A' characters removed from"));
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasParseTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasStyleTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertTrue(hasDebugRemoveATaskDefExecuted(report));
                assertFalse(hasConstraintAnalysisTaskExecuted(report, dynamicLanguage));
                assertFalse(hasCheckTaskExecuted(report, dynamicLanguage));
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Hover test");
            final DynamicLanguage dynamicLanguage;
            final Set<ResourceKey> providedResources;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = session.updateAffectedBy(Collections.emptySet());
                dynamicLanguage = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = dynamicLanguage.getCompileInput();
                providedResources = topDownSession.getProvidedResources();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                languageMetricsTracer.reset();
                final TopDownSession topDownSession = session.updateAffectedBy(providedResources);
                // Run hover task and check.
                final Option<HoverResult> hoverResult = topDownSession.require(languageComponent.getLanguageInstance().createHoverTask(rootDirectoryPath, charsFile.getPath(), Region.atOffset(2)));
                assertTrue(hoverResult.isSome());
                assertEquals(hoverResult.get().getText(), "Type: \"Chars\"");
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasParseTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasStyleTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertFalse(hasDebugRemoveATaskDefExecuted(report));
                assertTrue(hasConstraintAnalysisTaskExecuted(report, dynamicLanguage));
                assertFalse(hasCheckTaskExecuted(report, dynamicLanguage)); // hover doesn't require check
                assertTrue(hasHoverTaskExecuted(report, dynamicLanguage));
                // TODO: check execution here and in previous steps.
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Change analyzer and reload");
            final DynamicLanguage dynamicLanguage;
            final Set<ResourceKey> providedResources;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = modifyAnalyzer(session, previousInput);
                dynamicLanguage = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = dynamicLanguage.getCompileInput();
                providedResources = topDownSession.getProvidedResources();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Change analyzer test");
            final LanguageComponent languageComponent = dynamicLanguage.getLanguageComponent();
            try(final MixedSession session = dynamicLanguage.getPieComponent().newSession()) {
                languageMetricsTracer.reset();
                final TopDownSession topDownSession = session.updateAffectedBy(providedResources);
                // Run check task and check.
                final KeyedMessages messages = topDownSession.require(languageComponent.getLanguageInstance().createCheckTask(charsProjectPath));
                assertTrue(messages.containsError());
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasParseTaskDefExecuted(report, dynamicLanguage));
                assertFalse(hasStyleTaskDefExecuted(report, dynamicLanguage));
                assertTrue(hasRemoveATaskDefExecuted(report)); // Re-executed because Statix change generates new Stratego code which re-executes this task.
                assertFalse(hasDebugRemoveATaskDefExecuted(report)); // Not re-executed because RemoveA task produced the same output.
                assertTrue(hasConstraintAnalysisTaskExecuted(report, dynamicLanguage));
                assertTrue(hasCheckTaskExecuted(report, dynamicLanguage));
                // TODO: check execution here and in previous steps.
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        try(final MixedSession session = newSession()) {
            System.out.println("Unload language");
            dynamicLanguageRegistry.unload(rootDirectoryPath);
            previousInput = null;
            session.deleteUnobservedTasks(t -> true, (t, r) -> false);
        }
    }

    @Disabled @Test void testDynamicLanguage() throws Exception {
        CompileLanguageInput previousInput;

        DynamicLanguage lang1a;
        try(MixedSession session = newSession()) {
            // Load language.
            lang1a = requireDynamicLoad(session, rootDirectoryPath);
            previousInput = lang1a.getCompileInput();
            // Dynamic language 1 has not yet been closed.
            assertNotNull(lang1a.getClassLoader());
            assertNotNull(lang1a.getLanguageComponent());
            assertFalse(lang1a.isClosed());
        }

        DynamicLanguage lang1b;
        try(MixedSession session = newSession()) {
            // Load language again, but nothing has changed.
            lang1b = requireDynamicLoad(session, rootDirectoryPath);
            previousInput = lang1b.getCompileInput();
            // Dynamic language 1b has not yet been closed.
            assertNotNull(lang1b.getClassLoader());
            assertNotNull(lang1b.getLanguageComponent());
            assertFalse(lang1b.isClosed());
        }

        DynamicLanguage lang2;
        try(MixedSession session = newSession()) {
            // Modify language specification and reload language.
            lang2 = getDynamicLoadOutput(modifyStyler(session, previousInput), rootDirectoryPath);
            previousInput = lang2.getCompileInput();
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
        try(MixedSession session = newSession()) {
            // Modify language specification and reload language.
            lang3 = getDynamicLoadOutput(modifyParser(session, previousInput), rootDirectoryPath);
            previousInput = lang3.getCompileInput();
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

        // Unload dynamic language.
        dynamicLanguageRegistry.unload(rootDirectoryPath);
        previousInput = null;
        // Dynamic language 3 should be closed.
        assertThrows(IllegalStateException.class, lang3::getClassLoader);
        assertThrows(IllegalStateException.class, lang3::getLanguageComponent);
        assertTrue(lang3.isClosed());
        lang3 = null;

        try(MixedSession session = newSession()) {
            // Cleanup cache.
            session.deleteUnobservedTasks(t -> true, (t, r) -> false);
        }
    }
}
