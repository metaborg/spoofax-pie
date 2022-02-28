package mb.tiger.eclipse;

import dagger.Component;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.editor.EditorCloseJob;
import mb.spoofax.eclipse.editor.EditorUpdateJob;
import mb.spoofax.eclipse.job.LockRule;
import mb.spoofax.eclipse.job.ReadLockRule;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerModule;
import mb.tiger.spoofax.TigerQualifier;
import mb.tiger.spoofax.TigerResourcesComponent;
import mb.tiger.spoofax.TigerScope;

@TigerScope
@Component(
    modules = {
        TigerModule.class,
        TigerEclipseModule.class
    },
    dependencies = {
        EclipseLoggerComponent.class,
        TigerResourcesComponent.class,
        ResourceServiceComponent.class,
        EclipsePlatformComponent.class
    }
)
public interface TigerEclipseComponent extends EclipseLanguageComponent, TigerComponent {
    TigerEditorTracker getEditorTracker();

    @Override TigerCheckCallback.Factory getCheckCallbackFactory();


    @Override @TigerQualifier EclipseIdentifiers getEclipseIdentifiers();


    @Override @TigerQualifier("StartupWriteLock") LockRule startupWriteLockRule();

    @Override @TigerQualifier ReadLockRule startupReadLockRule();


    @Override @TigerQualifier EditorUpdateJob.Factory editorUpdateJobFactory();

    @Override @TigerQualifier EditorCloseJob.Factory editorCloseJobFactory();


    @Override @TigerQualifier TigerMainMenu getMainMenu();

    @Override @TigerQualifier TigerResourceContextMenu getResourceContextMenu();

    @Override @TigerQualifier TigerEditorContextMenu getEditorContextMenu();
}
