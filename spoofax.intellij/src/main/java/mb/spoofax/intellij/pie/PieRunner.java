package mb.spoofax.intellij.pie;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import mb.common.util.ListView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.*;
import mb.pie.api.exec.CancelToken;
import mb.pie.runtime.exec.Stats;
import mb.spoofax.core.language.command.*;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.spoofax.core.language.command.arg.RawArgsBuilder;
import mb.spoofax.intellij.IntellijLanguageComponent;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import mb.spoofax.intellij.resource.ResourceUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;


@Singleton
public final class PieRunner {

    private final Logger logger;
    private final Pie pie;
    private final ArgConverters argConverters;
    private final ResourceUtil resourceUtil;

    @Inject
    public PieRunner(
            LoggerFactory loggerFactory,
            Pie pie,
            ArgConverters argConverters,
            ResourceUtil resourceUtil) {
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
     * @param languageComponent The language component.
     */
    public void requireCommand(
            IntellijLanguageComponent languageComponent,
            CommandRequest<?> commandRequest,
            Project project,
            ListView<CommandContext> contexts,
            PieSession session,
            CancelToken cancelToken) throws ExecException, InterruptedException {
        switch(commandRequest.executionType) {
            case ManualOnce:
                for(CommandContext context : contexts) {
                    final Task<CommandOutput> task = createCommandTask(commandRequest, context);
                    final CommandOutput output = requireWithoutObserving(task, session, cancelToken);
                    processOutput(project, output, true, null);
                }
                break;
            case ManualContinuous:
                for(CommandContext context : contexts) {
                    final Task<CommandOutput> task = createCommandTask(commandRequest, context);
                    final CommandOutput output = require(task, session, cancelToken);
                    processOutput(project, output, true, (p) -> {
                        // POTI: this opens a new PIE session, which may be used concurrently with other sessions, which
                        // may not be (thread-)safe.
                        try(final PieSession newSession = languageComponent.newPieSession()) {
                            unobserve(task, pie, newSession, cancelToken);
                        }
                        pie.removeCallback(task);
                    });
                    pie.setCallback(task, (o) -> processOutput(project, o, false, null));
                }
                break;
            case AutomaticContinuous:
                for(CommandContext context : contexts) {
                    final Task<CommandOutput> task = createCommandTask(commandRequest, context);
                    require(task, session, cancelToken);
                    // Feedback for AutomaticContinuous is ignored intentionally: do not want to suddenly open new
                    // editors when a resource is saved.
                }
                break;
        }
    }

    private void processOutput(Project project, CommandOutput output, boolean activate, Consumer<Object> onClose) {
        for(CommandFeedback feedback : output.feedback) {
            processFeedback(project, feedback, activate, onClose);
        }
    }

    private void processFeedback(Project project, CommandFeedback feedback, boolean activate, Consumer<Object> onClose) {
        CommandFeedbacks.caseOf(feedback)
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

    public <A extends Serializable> Task<CommandOutput> createCommandTask(CommandRequest<A> commandRequest, CommandContext context) {
        final CommandDef<A> def = commandRequest.def;
        final RawArgsBuilder builder = new RawArgsBuilder(def.getParamDef(), argConverters);
        if(commandRequest.initialArgs != null) {
            builder.setArgsFrom(commandRequest.initialArgs);
        }
        final RawArgs rawArgs = builder.build(context);
        final A args = def.fromRawArgs(rawArgs);
        final CommandInput<A> input = new CommandInput<>(args);
        return def.createTask(input);
    }

    public <T extends Serializable> T requireWithoutObserving(Task<T> task, PieSession session, CancelToken cancelToken) throws ExecException, InterruptedException {
        logger.trace("Require (without observing) '{}'", task);
        Stats.reset();
        final T result = session.requireWithoutObserving(task, cancelToken);
        logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
        return result;
    }

    public <T extends Serializable> T require(Task<T> task, PieSession session, CancelToken cancelToken) throws ExecException, InterruptedException {
        logger.trace("Require '{}'", task);
        Stats.reset();
        final T result = session.require(task, cancelToken);
        logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
        return result;
    }

    public void unobserve(Task<?> task, Pie pie, PieSession session, CancelToken _cancelToken) {
        final TaskKey key = task.key();
        if(!pie.isObserved(key)) return;
        logger.trace("Unobserving '{}'", key);
        session.unobserve(key);
    }

}
