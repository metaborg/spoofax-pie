package mb.spoofax.core.language;

import mb.pie.api.PieSession;

@LanguageScope
public interface LanguageComponent {
    LanguageInstance getLanguageInstance();

    // TODO: Replace by Provider<PieSession>.get() or PieSessionManager.createSession()
    PieSession newPieSession();
}
