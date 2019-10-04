package mb.spoofax.intellij.menu;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Utility functions for working with IntelliJ actions.
 */
public final class ActionUtils {

    /**
     * Adds an action(group) to a parent and registers all its children.
     *
     * @param action             The action to add.
     * @param parentID           The parent ID.
     * @param relativeToActionId The ID relative to which to position this action; or <code>null</code>.
     * @param anchor             The anchor indicating where to position this action; or <code>null</code> for the
     *                           default.
     */
    public static void addAndRegisterActionGroup(AnAction action, String parentID,
                                                 @Nullable String relativeToActionId, @Nullable Anchor anchor) {
        final ActionManager manager = ActionManager.getInstance();
        final DefaultActionGroup parent = (DefaultActionGroup)manager.getAction(parentID);
        parent.add(action, getActionConstraints(relativeToActionId, anchor));
        registerAction(manager, action);
    }

    /**
     * Gets an object that specifies where the action is positioned.
     *
     * @param relativeToActionId The action ID relative to which to position the action; or <code>null</code> to
     *                           position the action at the start or end.
     * @param anchor             The anchor indicating where to position the action; or <code>null</code> to position
     *                           the action after or at the end.
     * @return The {@link Constraints}.
     */
    private static Constraints getActionConstraints(@Nullable String relativeToActionId, @Nullable Anchor anchor) {
        if (relativeToActionId != null && anchor != null) {
            return new Constraints(anchor, relativeToActionId);
        } else if (relativeToActionId != null) {
            return new Constraints(Anchor.AFTER, relativeToActionId);
        } else if (anchor == Anchor.BEFORE || anchor == Anchor.FIRST) {
            return Constraints.FIRST;
        } else {
            return Constraints.LAST;
        }
    }

    /**
     * Registers the action and its children.
     *
     * @param action The action.
     */
    private static void registerAction(ActionManager manager, AnAction action) {
        if (action instanceof AnActionWithId) {
            manager.registerAction(((AnActionWithId)action).getId(), action);
        }
        if (action instanceof DefaultActionGroup) {
            registerActionGroup(manager, (DefaultActionGroup)action);
        }
    }

    /**
     * Registers all actions in the specified group.
     *
     * @param actionGroup The action group.
     */
    private static void registerActionGroup(ActionManager manager, DefaultActionGroup actionGroup) {
        for (final AnAction action : actionGroup.getChildActionsOrStubs()) {
            registerAction(manager, action);
        }
    }

    /**
     * Removes an action(group) from a parent and unregisters all its children.
     *
     * @param action   The action to remove.
     * @param parentID The parent ID.
     */
    public static void removeAndUnregisterActionGroup(AnAction action, String parentID) {
        final ActionManager manager = ActionManager.getInstance();
        final DefaultActionGroup parent = (DefaultActionGroup)manager.getAction(parentID);
        parent.remove(action);
        unregisterAction(manager, action);
    }

    /**
     * Unregisters the action and its children.
     *
     * @param action The action.
     */
    private static void unregisterAction(ActionManager manager, AnAction action) {
        if (action instanceof AnActionWithId) {
            manager.unregisterAction(((AnActionWithId)action).getId());
        }
        if (action instanceof DefaultActionGroup) {
            unregisterActionGroup(manager, (DefaultActionGroup)action);
        }
    }

    /**
     * Unregisters all actions in the specified group.
     *
     * @param actionGroup The action group.
     */
    private static void unregisterActionGroup(
            ActionManager manager,
            DefaultActionGroup actionGroup) {
        for (final AnAction action : actionGroup.getChildActionsOrStubs()) {
            unregisterAction(manager, action);
        }
    }

    /**
     * Gets the editor from which the action was invoked.
     *
     * @param e The action event.
     * @return The editor; or null.
     */
    @Nullable
    public static Editor getEditor(final AnActionEvent e) {
        return e.getData(CommonDataKeys.EDITOR);
    }

    /**
     * Gets the PSI file from which the action was invoked.
     *
     * @param e The action event.
     * @return The PSI file; or null.
     */
    @Nullable
    public static PsiFile getPsiFile(final AnActionEvent e) {
        @Nullable Project project = e.getProject();
        @Nullable Editor editor = getEditor(e);
        if (project == null || editor == null) return null;
        return PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    }

    // https://github.com/zuql/demo/blob/8618a984a37d484dde9186b0db42991256479a22/big-data/hadoop/HadoopIntellijPlugin-master/src/main/java/com/fangyuzhong/intelliJ/hadoop/core/util/ActionUtil.java

    /**
     * Creates an action toolbar for the action group with the specified name.
     *
     * @param place           The place that is passed to the event when the action is executed.
     * @param horizontal      Whether the toolbar is oriented horizontally or vertically.
     * @param actionGroupName The name of the action group.
     * @return The action toolbar.
     */
    public static ActionToolbar createActionToolbar(String place, boolean horizontal, String actionGroupName) {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup actionGroup = (ActionGroup)actionManager.getAction(actionGroupName);
        return actionManager.createActionToolbar(place, actionGroup, horizontal);
    }

    /**
     * Creates an action toolbar for the specified action group.
     *
     * @param place       The place that is passed to the event when the action is executed.
     * @param horizontal  Whether the toolbar is oriented horizontally or vertically.
     * @param actionGroup The action group.
     * @return The action toolbar.
     */
    public static ActionToolbar createActionToolbar(String place, boolean horizontal, ActionGroup actionGroup) {
        ActionManager actionManager = ActionManager.getInstance();
        return actionManager.createActionToolbar(place, actionGroup, horizontal);
    }

    /**
     * Creates an action toolbar for the specified actions.
     *
     * @param place      The place that is passed to the event when the action is executed.
     * @param horizontal Whether the toolbar is oriented horizontally or vertically.
     * @param actions    The actions.
     * @return The action toolbar.
     */
    public static ActionToolbar createActionToolbar(String place, boolean horizontal, AnAction... actions) {
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (AnAction action : actions) {
            if (action instanceof Separator) {
                actionGroup.addSeparator();
            } else {
                actionGroup.add(action);
            }
        }
        return actionManager.createActionToolbar(place, actionGroup, horizontal);
    }
//
//    /**
//     * Determines whether all active files are of the specified language.
//     *
//     * @param e        The event arguments.
//     * @param language The language implementation to check.
//     * @return <code>true</code> when all active files are of the specified language;
//     * otherwise, <code>false</code>.
//     */
//    public boolean isActiveFileLanguage(final AnActionEvent e, final ILanguageImpl language) {
//        final List<FileObject> files = getActiveFiles(e);
//        if (files.isEmpty())
//            return false;
//        for (final FileObject file : files) {
//            if (!this.identifierService.identify(file, language))
//                return false;
//        }
//        return true;
//    }
//
//    /**
//     * Gets a list of files currently selected.
//     *
//     * @param e The event arguments.
//     * @return A list of files.
//     */
//    public List<FileObject> getActiveFiles(final AnActionEvent e) {
//        @Nullable final VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
//        if (files == null || files.length == 0)
//            return Collections.emptyList();
//
//        final ArrayList<FileObject> result = new ArrayList<>(files.length);
//        for (final VirtualFile file : files) {
//            if (file.isDirectory())
//                continue;
//            result.add(this.resourceService.resolve(file));
//        }
//        return result;
//    }
//
//    /**
//     * Gets a list of files currently selected.
//     *
//     * @param e The event arguments.
//     * @return A list of files.
//     */
//    public List<TransformResource> getActiveResources(final AnActionEvent e) {
//        @Nullable final PsiFile[] files = getSelectedPsiFiles(e);
//        if (files == null || files.length == 0)
//            return Collections.emptyList();
//        final ArrayList<TransformResource> result = new ArrayList<>(files.length);
//        for (final PsiFile file : files) {
//            if (file.isDirectory())
//                continue;
//            final FileObject resource = this.resourceService.resolve(file.getVirtualFile());
//            @Nullable final IProject project = this.projectService.get(file);
//            @Nullable final Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
//            if (project == null || document == null) {
//                this.logger.debug("Resource ignored because it has no project or document: {}", resource);
//                continue;
//            }
//            result.add(new TransformResource(resource, project, document.getText()));
//        }
//        return result;
//    }
//
//    /**
//     * Gets the {@link PsiFile} objects for each open file.
//     *
//     * @param e The event.
//     * @return The PSI files; or <code>null</code>.
//     */
//    @Nullable
//    private PsiFile[] getSelectedPsiFiles(final AnActionEvent e) {
//        @Nullable final com.intellij.openapi.project.Project project = e.getData(CommonDataKeys.PROJECT);
//        if (project == null)
//            return null;
//
//        @Nullable final VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
//        if (files == null)
//            return null;
//
//        final PsiFile[] psiFiles = new PsiFile[files.length];
//        for (int i = 0; i < files.length; i++) {
//            psiFiles[i] = PsiManager.getInstance(project).findFile(files[i]);
//            if (psiFiles[i] == null) {
//                // If one of the files wasn't found in the project, it's probably a file
//                // in a different project. No support for mixing projects like that.
//                throw LoggerUtils2.exception(this.logger, RuntimeException.class,
//                        "Couldn't determine PsiFile for VirtualFile: {}", files[i]);
//            }
//        }
//        return psiFiles;
//    }

}
