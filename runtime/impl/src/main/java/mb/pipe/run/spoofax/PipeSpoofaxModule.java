package mb.pipe.run.spoofax;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import mb.pipe.run.spoofax.esv.StylingRulesFromESV;

public class PipeSpoofaxModule extends AbstractModule {
    @Override protected void configure() {
        bind(StylingRulesFromESV.class).in(Singleton.class);
    }
}
