package mb.spoofax.core.language;

import mb.pie.api.PieSession;

@LanguageScope
public interface LanguageComponent {
    LanguageInstance getLanguageInstance();

    PieSession newPieSession();
}
