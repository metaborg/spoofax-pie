package mb.spoofax.intellij.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import dagger.Component;
import mb.spoofax.intellij.psi.SpoofaxFile;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * An action group for a language.
 */
public abstract class LanguageActionGroup extends DefaultActionGroup {

    public LanguageActionGroup(@Nullable String shortName, boolean popup) {
        super(shortName, popup);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // TODO
//        @Nullable PsiFile psiFile = ActionUtils.getPsiFile(e);
//        boolean isOurLanguage = psiFile != null && psiFile.getClass() == getPsiFileClass();
//        // Hide the menu when it's not our language
//        e.getPresentation().setVisible(isOurLanguage);
    }

    protected abstract Class<?> getPsiFileClass();
}
