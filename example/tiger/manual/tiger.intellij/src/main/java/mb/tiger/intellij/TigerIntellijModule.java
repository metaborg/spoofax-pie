package mb.tiger.intellij;

import com.intellij.openapi.util.IconLoader;
import dagger.Module;
import dagger.Provides;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.IntellijLanguage;

import javax.swing.*;

@Module
public class TigerIntellijModule {
    @Provides @LanguageScope
    IntellijLanguage provideSpoofaxLanguage(TigerLanguage language) {
        // Downcast because injections in spoofax.intellij require an IntellijLanguage, and dagger does not implicitly downcast.
        return language;
    }

    @Provides @LanguageScope
    Icon provideFileIcon() {
        return IconLoader.getIcon("/META-INF/fileIcon.svg");
    }
}
