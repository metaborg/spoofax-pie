package mb.tiger.intellij.menu;

import mb.spoofax.intellij.menu.LanguageActionGroup;
import mb.spoofax.intellij.psi.SpoofaxFile;
import mb.tiger.intellij.TigerFile;
import org.checkerframework.checker.nullness.qual.Nullable;


@SuppressWarnings("ComponentNotRegistered")
public final class TigerLanguageActionGroup extends LanguageActionGroup {

    public TigerLanguageActionGroup() {
        super("Tiger", true);
    }
//    public TigerLanguageActionGroup(@Nullable String shortName, boolean popup) {
//        super(shortName, popup);
//    }

    @Override
    protected Class<?> getPsiFileClass() {
        return TigerFile.class;
    }

}
