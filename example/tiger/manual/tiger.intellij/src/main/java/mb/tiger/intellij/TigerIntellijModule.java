package mb.tiger.intellij;

import com.intellij.openapi.util.IconLoader;
import dagger.Module;
import dagger.Provides;
import mb.pie.api.Pie;
import mb.spoofax.intellij.IntellijLanguage;
import mb.spoofax.intellij.menu.EditorContextLanguageAction;
import mb.spoofax.intellij.menu.LanguageActionGroup;
import mb.spoofax.intellij.pie.PieRunner;
import mb.spoofax.intellij.resource.IntellijResourceRegistry;
import mb.tiger.intellij.menu.TigerLanguageActionGroup;
import mb.tiger.spoofax.TigerScope;


import javax.swing.*;

@Module
public class TigerIntellijModule {
    @Provides @TigerScope
    static IntellijLanguage provideSpoofaxLanguage(TigerLanguage language) {
        // Downcast because injections in spoofax.intellij require an IntellijLanguage, and dagger does not implicitly downcast.
        return language;
    }

    @Provides @TigerScope
    static Icon provideFileIcon() {
        return IconLoader.getIcon("/META-INF/fileIcon.svg");
    }

    @Provides @TigerScope
    static LanguageActionGroup provideLanguageActionGroup() { return new TigerLanguageActionGroup(); }

    @Provides @TigerScope
    static EditorContextLanguageAction.Factory providesTigerEditorContextLanguageAction_Factory(IntellijResourceRegistry resourceRegistry, PieRunner pieRunner, Pie pie) {
        return new EditorContextLanguageAction.FactoryImpl(resourceRegistry, pieRunner, pie);
    }
}
