package mb.spoofax.eclipse.editor;

import mb.common.codecompletion.CodeCompletionItem;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.editing.TextEdit;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.api.Interactivity;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.api.UncheckedExecException;
import mb.pie.dagger.PieComponent;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Spoofax Eclipse content assist (code completion) processor.
 */
public final class SpoofaxContentAssistProcessor implements IContentAssistProcessor {

    private final Logger logger;

    private final SpoofaxEditorBase editorBase;
    private final @Nullable LanguageComponent languageComponent;
    private final @Nullable PieComponent pieComponent;

    /**
     * Initializes a new instance of the {@link SpoofaxContentAssistProcessor} class.
     *
     * @param editorBase the editor on which code completion was invoked
     * @param languageComponent the language component
     * @param pieComponent the PIE component
     * @param loggerFactory the logger factory
     */
    public SpoofaxContentAssistProcessor(
        SpoofaxEditorBase editorBase,
        @Nullable LanguageComponent languageComponent,
        @Nullable PieComponent pieComponent,
        LoggerFactory loggerFactory
    ) {
        this.editorBase = editorBase;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;

        this.logger = loggerFactory.create(getClass());
    }


    @Override public @Nullable ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
        final Option<CodeCompletionResult> optCodeCompletionResult = getCodeCompletionResult(offset);
        if (optCodeCompletionResult.isNone()) {
            // No completions.
            return null;
        }
        final CodeCompletionResult codeCompletionResult = optCodeCompletionResult.unwrap();

        List<ICompletionProposal> eclipseProposals = IntStream.range(0, codeCompletionResult.getProposals().size()).mapToObj(
            i -> proposalToEclipseProposal(codeCompletionResult.getProposals().get(i), codeCompletionResult, i)).collect(Collectors.toList());
        return eclipseProposals.toArray(new ICompletionProposal[0]);
    }

    /**
     * Invokes the code completion task and returns the code completion result.
     *
     * @param offset the offset at which to invoke code completion
     * @return an option of the code completion result; or none if it failed
     */
    private Option<CodeCompletionResult> getCodeCompletionResult(final int offset) {
        if(languageComponent == null || pieComponent == null || editorBase.file == null) {
            return Option.ofNone();
        }

        final ResourceKey fileKey = new EclipseResourcePath(editorBase.file);
        final @Nullable ResourcePath projectRoot = editorBase.project != null ? new EclipseResourcePath(editorBase.project) : null;
        final Region selection = Region.atOffset(offset);

        return Option.ofOptional(pieComponent.getPie().tryNewSession()).flatMap(trySession -> { // Skip code completion if another session exists.
            try(final MixedSession session = trySession) {
                final TopDownSession topDownSession = session.updateAffectedBy(Collections.emptySet(), Collections.singleton(Interactivity.Interactive));
                return topDownSession.requireWithoutObserving(
                    languageComponent.getLanguageInstance().createCodeCompletionTask(selection, fileKey, projectRoot)
                );
            } catch(ExecException e) {
                // Bubble error up to Eclipse, which will handle it and show a dialog.
                throw new UncheckedExecException("Code completion failed unexpectedly.", e);
            } catch(InterruptedException e) {
                return Option.ofNone();
            }
        });
    }

    /**
     * Converts a Spoofax code completion proposal into an Eclipse code completion proposal.
     *
     * @param proposal the Spoofax code completion proposal to convert
     * @param result the code completion result
     * @param priority the priority of the element
     * @return the Eclipse code completion proposal
     */
    private ICompletionProposal proposalToEclipseProposal(CodeCompletionItem proposal, CodeCompletionResult result, int priority) {
        // We assume there is exactly one edit. FIXME: Be able to deal with no edits, or multiple edits.
        final TextEdit textEdit = proposal.getEdits().get(0);
        final String replacementString = textEdit.getNewText();
        final int replacementOffset = textEdit.getRegion().getStartOffset();
        final int replacementLength = textEdit.getRegion().getLength();
        final int cursorPosition = textEdit.getRegion().getEndOffset(); // Put the cursor at the end of the inserted text
        final @Nullable Image image = null; // TODO: Image
        final @Nullable String displayString = proposal.getLabel();
        final @Nullable IContextInformation contextInformation = null;
        // FIXME: not sure if this belongs here:
        final @Nullable String additionalProposalInfo = proposal.getParameters() + (proposal.getLocation().isEmpty() ? "" : " " + proposal.getLocation() + (proposal.getType().isEmpty() ? " " + proposal.getDescription() : " : " +proposal.getType()));
        return new CompletionProposal(
            replacementString,
            replacementOffset,
            replacementLength,
            cursorPosition,
            image,
            displayString,
            contextInformation,
            additionalProposalInfo
        );
    }

    @Override public @Nullable IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        // Not implemented.
        return null;
    }

    @Override public @Nullable char[] getCompletionProposalAutoActivationCharacters() {
        // Not implemented.
        return null;
    }

    @Override public @Nullable char[] getContextInformationAutoActivationCharacters() {
        // Not implemented.
        return null;
    }

    @Override public @Nullable String getErrorMessage() {
        // Not implemented.
        return null;
    }

    @Override public @Nullable IContextInformationValidator getContextInformationValidator() {
        // Not implemented.
        return null;
    }
}
