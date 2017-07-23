package mb.pipe.run.core;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import mb.pipe.run.core.model.Context;
import mb.pipe.run.core.model.ContextFactory;
import mb.pipe.run.core.model.ContextImpl;

public class PipeModule extends AbstractModule {
    @Override protected void configure() {
        bindContext();
    }


    protected void bindContext() {
        install(new FactoryModuleBuilder().implement(Context.class, ContextImpl.class).build(ContextFactory.class));
    }
}
