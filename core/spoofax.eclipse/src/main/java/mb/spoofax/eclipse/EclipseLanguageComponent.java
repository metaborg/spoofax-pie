package mb.spoofax.eclipse;

import dagger.Lazy;
import mb.pie.api.Pie;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.job.LockRule;
import mb.spoofax.eclipse.job.ReadLockRule;

import javax.inject.Named;

@LanguageScope
public interface EclipseLanguageComponent extends LanguageComponent {
    EclipseIdentifiers getEclipseIdentifiers();

    @Named("StartupWriteLock") LockRule startupWriteLockRule();

    ReadLockRule startupReadLockRule();

    Lazy<Pie> getPieProvider(); // TODO: Remove
}
