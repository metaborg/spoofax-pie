package mb.tiger.intellij;

import com.intellij.lang.Language;

import javax.inject.Singleton;

public final class TigerLanguage extends Language {
    static final TigerLanguage instance = new TigerLanguage();

    private TigerLanguage() {
        super("Tiger");
    }
}
