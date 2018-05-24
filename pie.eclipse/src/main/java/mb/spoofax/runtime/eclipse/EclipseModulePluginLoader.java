package mb.spoofax.runtime.eclipse;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.plugin.IModulePluginLoader;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class EclipseModulePluginLoader implements IModulePluginLoader {
    private final String extensionPoint;


    public EclipseModulePluginLoader(String extensionPoint) {
        this.extensionPoint = extensionPoint;
    }


    @Override public Iterable<Module> modules() throws MetaborgException {
        final Collection<Module> modules = Lists.newLinkedList();
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        final IExtensionPoint point = registry.getExtensionPoint(extensionPoint);
        for(IConfigurationElement config : point.getConfigurationElements()) {
            try {
                final Object moduleObj = config.createExecutableExtension("class");
                if(moduleObj instanceof Module) {
                    final Module module = (Module) moduleObj;
                    modules.add(module);
                }
            } catch(CoreException e) {
                throw new MetaborgException("Unable to instantiate plugin module", e);
            }
        }
        return modules;
    }
}
