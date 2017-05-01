package mb.pipe.run.eclipse;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import mb.pipe.run.eclipse.util.BuilderUtils;

public class EclipseModule extends AbstractModule {
    @Override protected void configure() {
        bind(BuilderUtils.class).in(Singleton.class);
    }
}
