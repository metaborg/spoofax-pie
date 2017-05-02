package mb.pipe.run.eclipse;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import mb.pipe.run.eclipse.build.Updater;
import mb.pipe.run.eclipse.editor.Editors;
import mb.pipe.run.eclipse.util.BuilderUtils;
import mb.pipe.run.eclipse.util.StyleUtils;

public class EclipseModule extends AbstractModule {
    @Override protected void configure() {
        bind(BuilderUtils.class).in(Singleton.class);
        bind(StyleUtils.class).in(Singleton.class);
        bind(Editors.class).in(Singleton.class);
        bind(Updater.class).in(Singleton.class);
    }
}
