package mb.pipe.run.core;

import com.google.inject.AbstractModule;

import mb.pipe.run.core.log.Logger;
import mb.pipe.run.core.log.SLF4JLogger;

public class LogModule extends AbstractModule {
    private final org.slf4j.Logger rootLogger;


    public LogModule(org.slf4j.Logger rootLogger) {
        this.rootLogger = rootLogger;
    }


    @Override protected void configure() {
        bind(Logger.class).toInstance(new SLF4JLogger(rootLogger));
    }
}
