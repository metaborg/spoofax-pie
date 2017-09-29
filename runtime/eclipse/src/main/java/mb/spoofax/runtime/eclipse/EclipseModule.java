package mb.spoofax.runtime.eclipse;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import mb.spoofax.runtime.eclipse.build.WorkspaceUpdate;
import mb.spoofax.runtime.eclipse.build.WorkspaceUpdateFactory;
import mb.spoofax.runtime.eclipse.editor.Editors;
import mb.spoofax.runtime.eclipse.util.BuilderUtils;
import mb.spoofax.runtime.eclipse.util.ColorShare;
import mb.spoofax.runtime.eclipse.util.StyleUtils;

public class EclipseModule extends AbstractModule {
    @Override protected void configure() {
        bind(BuilderUtils.class).in(Singleton.class);
        bind(ColorShare.class).in(Singleton.class);
        bind(StyleUtils.class).in(Singleton.class);
        bind(Editors.class).in(Singleton.class);
        bind(WorkspaceUpdate.class);
        install(new FactoryModuleBuilder().build(WorkspaceUpdateFactory.class));
    }
}
