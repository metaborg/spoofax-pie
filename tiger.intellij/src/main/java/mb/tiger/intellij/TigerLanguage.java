package mb.tiger.intellij;

import com.intellij.lang.Language;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class TigerLanguage extends Language {

    public static final TigerLanguage INSTANCE = new TigerLanguage();

    // Cannot be instantiated.
    private TigerLanguage() {
        super("Tiger");
    }
}
