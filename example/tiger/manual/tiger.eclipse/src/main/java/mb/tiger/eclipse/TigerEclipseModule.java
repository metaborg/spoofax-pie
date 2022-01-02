package mb.tiger.eclipse;

import dagger.Module;
import dagger.Provides;
import mb.log.api.LoggerFactory;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.editor.EditorCloseJob;
import mb.spoofax.eclipse.editor.EditorUpdateJob;
import mb.spoofax.eclipse.job.LockRule;
import mb.spoofax.eclipse.job.ReadLockRule;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Named;

@Module
public class TigerEclipseModule {
    @Provides @TigerScope
    static EclipseIdentifiers provideEclipseIdentifiers() { return new TigerEclipseIdentifiers(); }

    @Provides @TigerScope @Named("StartupWriteLock")
    static LockRule provideStartupWriteLockRule() { return new LockRule("Tiger startup write lock"); }

    @Provides @TigerScope
    static ReadLockRule provideStartupReadLockRule(@Named("StartupWriteLock") LockRule writeLock) { return new ReadLockRule(writeLock, "Tiger startup read lock"); }

    @Provides @TigerScope
    static EditorUpdateJob.Factory provideEditorUpdateJobFactory(LoggerFactory loggerFactory, PieRunner pieRunner) {
        return (languageComponent, pieComponent, project, file, document, input, editor) -> new EditorUpdateJob(
            loggerFactory,
            pieRunner,
            languageComponent,
            pieComponent,
            project,
            file,
            document,
            input,
            editor
        );
    }

    @Provides @TigerScope
    static EditorCloseJob.Factory provideEditorCloseJobFactory(LoggerFactory loggerFactory, PieRunner pieRunner) {
        return (languageComponent, pieComponent, project, file) -> new EditorCloseJob(
            loggerFactory,
            pieRunner,
            languageComponent,
            pieComponent,
            project,
            file
        );
    }
}
