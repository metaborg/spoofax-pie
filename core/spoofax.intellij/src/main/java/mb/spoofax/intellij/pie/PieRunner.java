package mb.spoofax.intellij.pie;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.style.Styling;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.common.util.UncheckedException;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.pie.api.TaskKey;
import mb.pie.api.TopDownSession;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.NullCancelableToken;
import mb.pie.runtime.exec.Stats;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.command.HierarchicalResourceType;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spoofax.core.platform.Platform;
import mb.spoofax.intellij.IntellijLanguageComponent;
import mb.spoofax.intellij.resource.ResourceUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Singleton
public class PieRunner {

    private final Logger logger;
    private final Pie pie;
    private final ArgConverters argConverters;
    private final ResourceUtil resourceUtil;

    @Inject
    public PieRunner(
        LoggerFactory loggerFactory,
        @Platform Pie pie,
        ArgConverters argConverters,
        ResourceUtil resourceUtil
    ) {
        this.logger = loggerFactory.create(getClass());
        this.pie = pie;
        this.argConverters = argConverters;
        this.resourceUtil = resourceUtil;
    }

    /**
     * Requires a command, which executes it.
     * @param commandRequest The command request to execute.
     * @param contexts The resource contexts in which the command is execute.
     * @param project The project in which the command is executed.
     * @param session The PIE session in which to execute the command.
     */
    public void requireCommand(
        Pie pie,
        CommandRequest<?> commandRequest,
        Project project,
        ListView<CommandContext> contexts,
        MixedSession session,
        CancelToken cancelToken) throws ExecException, InterruptedException {
        switch(commandRequest.executionType()) {
            case ManualOnce:
                for(CommandContext context : contexts) {
                    final Task<CommandFeedback> task = createCommandTask(commandRequest, context);
                    final CommandFeedback feedback = requireWithoutObserving(task, session, cancelToken);
                    processFeedbacks(project, feedback, true, null);
                }
                break;
            case ManualContinuous:
                for(CommandContext context : contexts) {
                    final Task<CommandFeedback> task = createCommandTask(commandRequest, context);
                    final CommandFeedback feedback = require(task, session, cancelToken);
                    processFeedbacks(project, feedback, true, (p) -> {
                        // POTI: this opens a new PIE session, which may be used concurrently with other sessions, which
                        // may not be (thread-)safe.
                        try(final MixedSession newSession = pie.newSession()) {
                            unobserve(task, pie, newSession, cancelToken);
                        }
                        pie.removeCallback(task);
                    });
                    pie.setCallback(task, (o) -> processFeedbacks(project, o, false, null));
                }
                break;
            case AutomaticContinuous:
                for(CommandContext context : contexts) {
                    final Task<CommandFeedback> task = createCommandTask(commandRequest, context);
                    require(task, session, cancelToken);
                    // Feedback for AutomaticContinuous is ignored intentionally: do not want to suddenly open new
                    // editors when a resource is saved.
                }
                break;
        }
    }

    private void processFeedbacks(Project project, CommandFeedback feedback, boolean activate, Consumer<Object> onClose) {
        for(ShowFeedback showFeedback : feedback.getShowFeedbacks()) {
            processShowFeedback(project, showFeedback, activate, onClose);
        }
    }

    private void processShowFeedback(Project project, ShowFeedback showFeedback, boolean activate, Consumer<Object> onClose) {
        showFeedback.caseOf()
            .showFile((file, region) -> {
                VirtualFile virtualFile = this.resourceUtil.getVirtualFile(file);
//                    int startOffset = (region != null ? region.getStartOffset() : -1);
//                    OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile, startOffset);
//                    ApplicationManager.getApplication().invokeLater(() -> {
//                        Editor editor = FileEditorManagerEx.getInstanceEx(project).openTextEditor(descriptor, activate);
//                        // TODO: Listen to closed event
//                    });
                // TODO: Select
                openEditor(project, virtualFile, true);

                return Optional.empty(); // Return value is required.
            })
            .showText((text, name, region) -> {
                FileType fileType = PlainTextFileType.INSTANCE; // TODO: Determine FileType
                PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(name, fileType, text, 0, true, false);
//                    @Nullable Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
//                    assert document != null;

//                    TextEditorProvider textEditorProvider = TextEditorProvider.getInstance();
//                    EditorFactory editorFactory = EditorFactory.getInstance();
//                    Document document1 = editorFactory.createDocument(text);
                // TODO: Editor should be read-only?
                openEditor(project, psiFile.getVirtualFile(), true);


                // TODO: Listen to closed event

                return Optional.empty(); // Return value is required.
            });
    }

    private void openEditor(Project project, VirtualFile file, boolean focus) {
        ApplicationManager.getApplication().invokeLater(() -> {
            // TODO: Use name
            // TODO: Select, use region
//                        TextEditorProvider.getInstance().createEditor()
//                        Editor viewer = editorFactory.createViewer(document, project, EditorKind.MAIN_EDITOR);
//                        TextEditor textEditor = textEditorProvider.getTextEditor(viewer);
            FileEditorManagerEx.getInstanceEx(project).openFile(file, true);
//                        Disposer.register(mainEditor, Disposable { editorFactory.releaseEditor(viewer) })
            // TODO: Listen to closed event
        });
    }

    public <A extends Serializable> Task<CommandFeedback> createCommandTask(CommandRequest<A> commandRequest, CommandContext context) {
        return commandRequest.def().createTask(commandRequest, context, argConverters);
    }

    public <T extends Serializable> T requireWithoutObserving(Task<T> task, MixedSession session, CancelToken cancelToken) throws ExecException, InterruptedException {
        logger.trace("Require (without observing) '{}'", task);
        Stats.reset();
        final T result = session.requireWithoutObserving(task, cancelToken);
        logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
        return result;
    }

    public <T extends Serializable> T require(Task<T> task, MixedSession session, CancelToken cancelToken) throws ExecException, InterruptedException {
        logger.trace("Require '{}'", task);
        Stats.reset();
        final T result = session.require(task, cancelToken);
        logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
        return result;
    }

    public void unobserve(Task<?> task, Pie pie, MixedSession session, CancelToken _cancelToken) {
        final TaskKey key = task.key();
        if(!pie.isObserved(key)) return;
        logger.trace("Unobserving '{}'", key);
        session.unobserve(key);
    }

}
