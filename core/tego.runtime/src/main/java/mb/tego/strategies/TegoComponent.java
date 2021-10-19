package mb.tego.strategies;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.strategies.runtime.TegoRuntime;

@TegoScope
@Component(
    modules = {
        TegoModule.class
    },
    dependencies = {
        LoggerComponent.class,
    }
)
public interface TegoComponent {
    TegoRuntime getTegoRuntime();
    TegoEngine getTegoEngine();
}
