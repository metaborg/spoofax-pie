package mb.pipe.run.cmd;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class PipeCmdModule extends AbstractModule {
    @Override protected void configure() {
        bind(Runner.class).in(Singleton.class);
    }
}
