package mb.tiger.intellij;

import com.intellij.openapi.util.IconLoader;
import dagger.Module;
import dagger.Provides;
import mb.spoofax.intellij.IntellijLanguage;
import mb.spoofax.intellij.editor.SpoofaxLexerFactory;
import mb.tiger.spoofax.TigerScope;

import javax.swing.*;

@Module
public abstract class TigerIntellijModule {
    @Provides @TigerScope
    static IntellijLanguage provideSpoofaxLanguage(TigerLanguage language) {
        // Downcast because injections in spoofax.intellij require an IntellijLanguage, and dagger does not implicitly downcast.
        return language;
    }

    @Provides @TigerScope
    static SpoofaxLexerFactory provideLexerFactory(TigerLexerFactory lexerFactory) {
        return lexerFactory;
    }

    @Provides @TigerScope
    static Icon provideFileIcon() {
        return IconLoader.getIcon("/META-INF/fileIcon.svg");
    }
}
