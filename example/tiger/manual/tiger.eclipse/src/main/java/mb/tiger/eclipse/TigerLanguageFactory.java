package mb.tiger.eclipse;

import org.eclipse.core.runtime.IExecutableExtensionFactory;

public class TigerLanguageFactory implements IExecutableExtensionFactory {
    @Override public TigerLanguage create() {
        return TigerLanguage.getInstance();
    }
}
