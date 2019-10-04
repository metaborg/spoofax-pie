package mb.spoofax.intellij;

import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.menu.EditorContextLanguageAction;
import mb.spoofax.intellij.menu.LanguageMenuBuilder;


@LanguageScope
public interface IntellijLanguageComponent extends LanguageComponent {
    LanguageMenuBuilder getLanguageMenuBuilder();

    EditorContextLanguageAction.Factory getEditorContextLanguageActionFactory();
}
