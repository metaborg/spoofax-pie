package mb.spoofax.core.language;

import mb.pie.api.MixedSession;

@LanguageScope
public interface LanguageComponent {
    LanguageInstance getLanguageInstance();

    MixedSession newPieSession();
}
