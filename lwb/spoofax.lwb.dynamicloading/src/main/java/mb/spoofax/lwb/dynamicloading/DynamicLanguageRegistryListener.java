package mb.spoofax.lwb.dynamicloading;

import mb.common.util.SetView;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface DynamicLanguageRegistryListener {
    void reload(@Nullable DynamicLanguage previousLanguage, DynamicLanguage language, SetView<String> removedFileExtensions, SetView<String> addedFileExtensions);

    void unload(DynamicLanguage language, SetView<String> removedFileExtensions);
}
