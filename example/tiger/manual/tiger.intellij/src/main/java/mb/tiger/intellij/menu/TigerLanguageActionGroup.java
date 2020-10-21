package mb.tiger.intellij.menu;

import mb.spoofax.intellij.menu.LanguageActionGroup;
import mb.tiger.intellij.TigerFile;

@SuppressWarnings("ComponentNotRegistered")
public final class TigerLanguageActionGroup extends LanguageActionGroup {

    public TigerLanguageActionGroup() {
        super("Tiger", true);
    }

    @Override protected Class<?> getPsiFileClass() {
        return TigerFile.class;
    }

}
