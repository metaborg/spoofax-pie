package mb.spoofax.runtime.eclipse;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import mb.spoofax.runtime.eclipse.build.Updater;
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
        bind(Updater.class).in(Singleton.class);
    }
}
