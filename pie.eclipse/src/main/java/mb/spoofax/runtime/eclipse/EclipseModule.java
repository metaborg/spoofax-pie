package mb.spoofax.runtime.eclipse;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import mb.spoofax.runtime.eclipse.pipeline.*;
import mb.spoofax.runtime.eclipse.util.*;

public class EclipseModule extends AbstractModule {
    @Override protected void configure() {
        bind(BuilderUtils.class).in(Singleton.class);
        bind(ColorShare.class).in(Singleton.class);
        bind(StyleUtils.class).in(Singleton.class);
        bind(FileUtils.class).in(Singleton.class);

        bind(PipelinePathChanges.class).in(Singleton.class);
        bind(PipelineProjectManager.class).in(Singleton.class);
        bind(PipelineAdapter.class).to(BottomUpPipelineAdapter.class).in(Singleton.class);

        bind(WorkspaceUpdate.class);
        install(new FactoryModuleBuilder().build(WorkspaceUpdateFactory.class));
    }
}
