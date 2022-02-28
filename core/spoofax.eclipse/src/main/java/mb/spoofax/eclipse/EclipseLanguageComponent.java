package mb.spoofax.eclipse;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.eclipse.editor.CheckCallback;
import mb.spoofax.eclipse.editor.EditorCloseJob;
import mb.spoofax.eclipse.editor.EditorUpdateJob;
import mb.spoofax.eclipse.job.LockRule;
import mb.spoofax.eclipse.job.ReadLockRule;
import mb.spoofax.eclipse.menu.EditorContextMenu;
import mb.spoofax.eclipse.menu.MainMenu;
import mb.spoofax.eclipse.menu.ResourceContextMenu;

import javax.inject.Named;

public interface EclipseLanguageComponent extends LanguageComponent {
    EclipseIdentifiers getEclipseIdentifiers();


    @Named("StartupWriteLock") LockRule startupWriteLockRule();

    ReadLockRule startupReadLockRule();


    EditorUpdateJob.Factory editorUpdateJobFactory();

    EditorCloseJob.Factory editorCloseJobFactory();

    CheckCallback.Factory getCheckCallbackFactory();


    MainMenu getMainMenu();

    ResourceContextMenu getResourceContextMenu();

    EditorContextMenu getEditorContextMenu();
}
