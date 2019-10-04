package mb.tiger.intellij.editor;

import com.intellij.icons.AllIcons;
import com.intellij.ide.scratch.ScratchFileActions;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import mb.spoofax.intellij.editor.EditorUtils;
import mb.spoofax.intellij.editor.SpoofaxIntellijFile;
import mb.spoofax.intellij.menu.ActionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


// https://github.com/JetBrains/kotlin/blob/f419d2eb30e848e42c279d27300a9c164e72811f/idea/idea-jvm/src/org/jetbrains/kotlin/idea/scratch/ui/ScratchTopPanel.kt
/**
 * The panel on top of the editor with some extra buttons and functionality.
 */
public final class TigerEditorTopPanel {
    private final SpoofaxIntellijFile spoofaxFile;
    private final ActionToolbar toolbar;

    public TigerEditorTopPanel(SpoofaxIntellijFile spoofaxFile) {
        this.spoofaxFile = spoofaxFile;

        this.toolbar = ActionUtils.createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, true, createToolbarGroup());
    }

    public ActionToolbar getToolbar() {
        return this.toolbar;
    }

    private static DefaultActionGroup createToolbarGroup() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new ShowAstAction());
        group.addSeparator();
        group.add(new PrettyPrintAction());
        return group;
    }

    // https://github.com/JetBrains/kotlin/blob/37e3c41b57c92f7e03951eba22bd588840fe3088/idea/idea-jvm/src/org/jetbrains/kotlin/idea/scratch/actions/RunScratchAction.kt
    private static class ShowAstAction extends SpoofaxAction {

        public ShowAstAction() {
            super("Show AST", "Shows the Abstract Syntax Tree.", AllIcons.Actions.Lightning);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            @Nullable Project project = e.getProject();
            if (project == null) return;
            @Nullable SpoofaxIntellijFile spoofaxFile = EditorUtils.getSpoofaxFileFromSelectedEditor(project);
            if (spoofaxFile == null) return;

            // Do something with it
        }

    }

    private static class PrettyPrintAction extends SpoofaxAction {

        public PrettyPrintAction() {
            super("Pretty Print", "Pretty-prints the Abstract Syntax Tree.", AllIcons.Actions.Show);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            @Nullable Project project = e.getProject();
            if (project == null) return;
            @Nullable SpoofaxIntellijFile spoofaxFile = EditorUtils.getSpoofaxFileFromSelectedEditor(project);
            if (spoofaxFile == null) return;

            // Do something with it
        }

    }

    // https://github.com/JetBrains/kotlin/blob/f419d2eb30e848e42c279d27300a9c164e72811f/idea/idea-jvm/src/org/jetbrains/kotlin/idea/scratch/actions/ScratchAction.kt
    private static abstract class SpoofaxAction extends AnAction {

        public SpoofaxAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
            super(text, description, icon);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            @Nullable Editor editor = e.getData(CommonDataKeys.EDITOR);
            @Nullable Project project = e.getProject();
            @Nullable SpoofaxIntellijFile spoofaxFile;
            if (editor != null) {
                TextEditor textEditor = TextEditorProvider.getInstance().getTextEditor(editor);
                spoofaxFile = EditorUtils.getSpoofaxFile(textEditor);
            } else if (project != null) {
                spoofaxFile = EditorUtils.getSpoofaxFileFromSelectedEditor(project);
            } else {
                spoofaxFile = null;
            }
            e.getPresentation().setVisible(spoofaxFile != null);
        }

    }
}
