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
import mb.spoofax.lwb.dynamicloading.component.DynamicComponent;
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
            final DynamicComponent dynamicComponent;
            try(final MixedSession session = newSession()) {
                dynamicComponent = requireDynamicLoad(session, rootDirectoryPath);
                previousInput = requireCompileLanguageInput(session, rootDirectoryPath);
                final KeyedMessages sptMessages = requireSptCheck(session, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Initial test");
            final LanguageComponent languageComponent = dynamicComponent.getLanguageComponent().unwrap();
            try(final MixedSession session = dynamicComponent.getPieComponent().newSession()) {
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
                assertTrue(hasTokenizeTaskDefExecuted(report, previousInput));
                assertTrue(hasParseTaskDefExecuted(report, previousInput));
                assertTrue(hasStyleTaskDefExecuted(report, previousInput));
                assertTrue(hasRemoveATaskDefExecuted(report));
                assertTrue(hasDebugRemoveATaskDefExecuted(report));
                assertTrue(hasConstraintAnalysisTaskExecuted(report, previousInput));
                assertTrue(hasCheckTaskExecuted(report, previousInput));
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Change styler and reload");
            final DynamicComponent dynamicComponent;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = modifyStyler(session, previousInput);
                dynamicComponent = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = requireCompileLanguageInput(topDownSession, rootDirectoryPath);
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Change styler test");
            final LanguageComponent languageComponent = dynamicComponent.getLanguageComponent().unwrap();
            try(final MixedSession session = dynamicComponent.getPieComponent().newSession()) {
                languageMetricsTracer.reset();
                final Option<Styling> result = session.require(languageComponent.getLanguageInstance().createStyleTask(charsFile.getPath(), rootDirectoryPath));
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report, previousInput));
                assertFalse(hasParseTaskDefExecuted(report, previousInput));
                assertTrue(hasStyleTaskDefExecuted(report, previousInput));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertFalse(hasDebugRemoveATaskDefExecuted(report));
                assertFalse(hasConstraintAnalysisTaskExecuted(report, previousInput));
                assertFalse(hasCheckTaskExecuted(report, previousInput));
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
            final DynamicComponent dynamicComponent;
            final Set<ResourceKey> providedResources;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = modifyParser(session, previousInput);
                dynamicComponent = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = requireCompileLanguageInput(topDownSession, rootDirectoryPath);
                providedResources = topDownSession.getProvidedResources();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Change parser test");
            try(final MixedSession session = dynamicComponent.getPieComponent().newSession()) {
                languageMetricsTracer.reset();
                session.updateAffectedBy(providedResources);
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report, previousInput));
                assertTrue(hasParseTaskDefExecuted(report, previousInput));
                assertFalse(hasStyleTaskDefExecuted(report, previousInput));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertFalse(hasDebugRemoveATaskDefExecuted(report));
                assertFalse(hasConstraintAnalysisTaskExecuted(report, previousInput));
                assertFalse(hasCheckTaskExecuted(report, previousInput));
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Change transformation and reload");
            final DynamicComponent dynamicComponent;
            final Set<ResourceKey> providedResources;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = modifyTransformation(session, previousInput);
                dynamicComponent = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = requireCompileLanguageInput(topDownSession, rootDirectoryPath);
                providedResources = topDownSession.getProvidedResources();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Change transformation test");
            final LanguageComponent languageComponent = dynamicComponent.getLanguageComponent().unwrap();
            try(final MixedSession session = dynamicComponent.getPieComponent().newSession()) {
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
                assertFalse(hasTokenizeTaskDefExecuted(report, previousInput));
                assertFalse(hasParseTaskDefExecuted(report, previousInput));
                assertFalse(hasStyleTaskDefExecuted(report, previousInput));
                assertTrue(hasRemoveATaskDefExecuted(report));
                assertTrue(hasDebugRemoveATaskDefExecuted(report));
                assertTrue(hasConstraintAnalysisTaskExecuted(report, previousInput)); // Unfortunately this is re-executed, because the Stratego runtime has changed. We cannot know if this affects the constraint analysis or not.
                assertTrue(hasCheckTaskExecuted(report, previousInput)); // TODO: this executes because the analyze task is executed because it depends on a Stratego runtime. However, the messages that the analyze task returns should be unchanged, so we need to put an output stamper on that to prevent re-execution.
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Change command and reload");
            final DynamicComponent dynamicComponent;
            final Set<ResourceKey> providedResources;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = modifyCommand(session, previousInput);
                dynamicComponent = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = requireCompileLanguageInput(topDownSession, rootDirectoryPath);
                providedResources = topDownSession.getProvidedResources();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Change command test");
            final LanguageComponent languageComponent = dynamicComponent.getLanguageComponent().unwrap();
            try(final MixedSession session = dynamicComponent.getPieComponent().newSession()) {
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
                assertFalse(hasTokenizeTaskDefExecuted(report, previousInput));
                assertFalse(hasParseTaskDefExecuted(report, previousInput));
                assertFalse(hasStyleTaskDefExecuted(report, previousInput));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertTrue(hasDebugRemoveATaskDefExecuted(report));
                assertFalse(hasConstraintAnalysisTaskExecuted(report, previousInput));
                assertFalse(hasCheckTaskExecuted(report, previousInput));
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Hover test");
            final DynamicComponent dynamicComponent;
            final Set<ResourceKey> providedResources;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = session.updateAffectedBy(Collections.emptySet());
                dynamicComponent = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = requireCompileLanguageInput(topDownSession, rootDirectoryPath);
                providedResources = topDownSession.getProvidedResources();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            final LanguageComponent languageComponent = dynamicComponent.getLanguageComponent().unwrap();
            try(final MixedSession session = dynamicComponent.getPieComponent().newSession()) {
                languageMetricsTracer.reset();
                final TopDownSession topDownSession = session.updateAffectedBy(providedResources);
                // Run hover task and check.
                final Option<HoverResult> hoverResult = topDownSession.require(languageComponent.getLanguageInstance().createHoverTask(rootDirectoryPath, charsFile.getPath(), Region.atOffset(2)));
                assertTrue(hoverResult.isSome());
                assertEquals(hoverResult.get().getText(), "Type: \"Chars\"");
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report, previousInput));
                assertFalse(hasParseTaskDefExecuted(report, previousInput));
                assertFalse(hasStyleTaskDefExecuted(report, previousInput));
                assertFalse(hasRemoveATaskDefExecuted(report));
                assertFalse(hasDebugRemoveATaskDefExecuted(report));
                assertTrue(hasConstraintAnalysisTaskExecuted(report, previousInput));
                assertFalse(hasCheckTaskExecuted(report, previousInput)); // hover doesn't require check
                assertTrue(hasHoverTaskExecuted(report, previousInput));
                // TODO: check execution here and in previous steps.
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        {
            System.out.println("Change analyzer and reload");
            final DynamicComponent dynamicComponent;
            final Set<ResourceKey> providedResources;
            try(final MixedSession session = newSession()) {
                final TopDownSession topDownSession = modifyAnalyzer(session, previousInput);
                dynamicComponent = getDynamicLoadOutput(topDownSession, rootDirectoryPath);
                previousInput = requireCompileLanguageInput(topDownSession, rootDirectoryPath);
                providedResources = topDownSession.getProvidedResources();
                final KeyedMessages sptMessages = getSptCheckOutput(topDownSession, rootDirectoryPath);
                assertNoErrors(sptMessages);
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }

            System.out.println("Change analyzer test");
            final LanguageComponent languageComponent = dynamicComponent.getLanguageComponent().unwrap();
            try(final MixedSession session = dynamicComponent.getPieComponent().newSession()) {
                languageMetricsTracer.reset();
                final TopDownSession topDownSession = session.updateAffectedBy(providedResources);
                // Run check task and check.
                final KeyedMessages messages = topDownSession.require(languageComponent.getLanguageInstance().createCheckTask(charsProjectPath));
                assertTrue(messages.containsError());
                // Check executed tasks.
                final MetricsTracer.Report report = languageMetricsTracer.reportAndReset();
                assertFalse(hasTokenizeTaskDefExecuted(report, previousInput));
                assertFalse(hasParseTaskDefExecuted(report, previousInput));
                assertFalse(hasStyleTaskDefExecuted(report, previousInput));
                assertTrue(hasRemoveATaskDefExecuted(report)); // Re-executed because Statix change generates new Stratego code which re-executes this task.
                assertFalse(hasDebugRemoveATaskDefExecuted(report)); // Not re-executed because RemoveA task produced the same output.
                assertTrue(hasConstraintAnalysisTaskExecuted(report, previousInput));
                assertTrue(hasCheckTaskExecuted(report, previousInput));
                // TODO: check execution here and in previous steps.
            } catch(Exception e) {
                printThrowable(e);
                throw e;
            }
        }

        try(final MixedSession session = newSession()) {
            System.out.println("Unload language");
            dynamicComponentManager.unloadFromCompiledSources(rootDirectoryPath);
            previousInput = null;
            session.deleteUnobservedTasks(t -> true, (t, r) -> false);
        }
    }

    @Disabled @Test void testDynamicLanguage() throws Exception {
        CompileLanguageInput previousInput;

        DynamicComponent component1a;
        try(MixedSession session = newSession()) {
            // Load language.
            component1a = requireDynamicLoad(session, rootDirectoryPath);
            previousInput = requireCompileLanguageInput(session, rootDirectoryPath);
            // Dynamic language 1 has not yet been closed.
            assertNotNull(component1a.getClassLoader());
            assertNotNull(component1a.getLanguageComponent());
            assertFalse(component1a.isClosed());
        }

        DynamicComponent component1b;
        try(MixedSession session = newSession()) {
            // Load language again, but nothing has changed.
            component1b = requireDynamicLoad(session, rootDirectoryPath);
            previousInput = requireCompileLanguageInput(session, rootDirectoryPath);
            // Dynamic language 1b has not yet been closed.
            assertNotNull(component1b.getClassLoader());
            assertNotNull(component1b.getLanguageComponent());
            assertFalse(component1b.isClosed());
        }

        DynamicComponent component2;
        try(MixedSession session = newSession()) {
            // Modify language specification and reload language.
            component2 = getDynamicLoadOutput(modifyStyler(session, previousInput), rootDirectoryPath);
            previousInput = requireCompileLanguageInput(session, rootDirectoryPath);
            // Dynamic language 2 has not yet been closed.
            assertNotNull(component2.getClassLoader());
            assertNotNull(component2.getLanguageComponent());
            assertFalse(component2.isClosed());
            // Dynamic language 1a and 1b should be closed.
            assertThrows(IllegalStateException.class, component1a::getClassLoader);
            assertThrows(IllegalStateException.class, component1a::getLanguageComponent);
            assertTrue(component1a.isClosed());
            component1a = null;
            assertThrows(IllegalStateException.class, component1b::getClassLoader);
            assertThrows(IllegalStateException.class, component1b::getLanguageComponent);
            assertTrue(component1b.isClosed());
            component1b = null;
        }

        DynamicComponent component3;
        try(MixedSession session = newSession()) {
            // Modify language specification and reload language.
            component3 = getDynamicLoadOutput(modifyParser(session, previousInput), rootDirectoryPath);
            previousInput = requireCompileLanguageInput(session, rootDirectoryPath);
            // Dynamic language 3 has not yet been closed.
            assertNotNull(component3.getClassLoader());
            assertNotNull(component3.getLanguageComponent());
            assertFalse(component3.isClosed());
            // Dynamic language 2 should be closed.
            assertThrows(IllegalStateException.class, component2::getClassLoader);
            assertThrows(IllegalStateException.class, component2::getLanguageComponent);
            assertTrue(component2.isClosed());
            component2 = null;
        }

        // Unload dynamic language.
        dynamicComponentManager.unloadFromCompiledSources(rootDirectoryPath);
        previousInput = null;
        // Dynamic language 3 should be closed.
        assertThrows(IllegalStateException.class, component3::getClassLoader);
        assertThrows(IllegalStateException.class, component3::getLanguageComponent);
        assertTrue(component3.isClosed());
        component3 = null;

        try(MixedSession session = newSession()) {
            // Cleanup cache.
            session.deleteUnobservedTasks(t -> true, (t, r) -> false);
        }
    }
}
