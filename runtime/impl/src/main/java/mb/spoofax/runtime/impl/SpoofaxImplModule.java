package mb.spoofax.runtime.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import mb.spoofax.runtime.impl.esv.StylingRulesFromESV;

public class SpoofaxImplModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(StylingRulesFromESV.class).in(Singleton.class);
    }
}
