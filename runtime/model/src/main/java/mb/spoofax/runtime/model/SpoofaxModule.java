package mb.spoofax.runtime.model;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import mb.spoofax.runtime.model.context.Context;
import mb.spoofax.runtime.model.context.ContextFactory;
import mb.spoofax.runtime.model.context.ContextImpl;

public class SpoofaxModule extends AbstractModule {
    @Override protected void configure() {
        bindContext();
    }


    protected void bindContext() {
        install(new FactoryModuleBuilder().implement(Context.class, ContextImpl.class).build(ContextFactory.class));
    }
}
