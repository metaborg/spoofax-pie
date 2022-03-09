package mb.spoofax.lwb.dynamicloading.component;

import mb.common.util.SetView;

public interface DynamicComponentManagerListener {
    void load(DynamicComponent component, SetView<String> addedFileExtensions);

    void reload(DynamicComponent previousComponent, DynamicComponent component, SetView<String> removedFileExtensions, SetView<String> addedFileExtensions);

    void unload(DynamicComponent component, SetView<String> removedFileExtensions);
}
