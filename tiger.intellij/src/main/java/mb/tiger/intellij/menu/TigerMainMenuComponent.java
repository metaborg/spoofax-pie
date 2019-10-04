package mb.tiger.intellij.menu;

import mb.spoofax.intellij.IntellijLanguageComponent;
import mb.spoofax.intellij.menu.LanguageMenuBuilder;
import mb.spoofax.intellij.menu.MainMenuComponent;
import mb.tiger.intellij.TigerPlugin;


public final class TigerMainMenuComponent extends MainMenuComponent {

    protected TigerMainMenuComponent() {
        super(TigerPlugin.getComponent(),
                TigerPlugin.getComponent().getLanguageMenuBuilder());
    }

}
