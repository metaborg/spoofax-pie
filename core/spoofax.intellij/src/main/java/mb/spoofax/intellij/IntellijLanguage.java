package mb.spoofax.intellij;

import com.intellij.lang.Language;

public class IntellijLanguage extends Language {
    protected IntellijLanguage(IntellijLanguageComponent languageComponent) {
        super(languageComponent.getLanguageInstance().getDisplayName());
    }
}
