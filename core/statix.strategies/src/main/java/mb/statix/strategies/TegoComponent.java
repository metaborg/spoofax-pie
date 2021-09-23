package mb.statix.strategies;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.statix.strategies.runtime.TegoEngine;
import mb.statix.strategies.runtime.TegoRuntime;

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
