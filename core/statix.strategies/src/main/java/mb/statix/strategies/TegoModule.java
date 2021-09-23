package mb.statix.strategies;

import dagger.Binds;
import dagger.Module;
import mb.statix.strategies.runtime.TegoEngine;
import mb.statix.strategies.runtime.TegoRuntime;
import mb.statix.strategies.runtime.TegoRuntimeImpl;

@Module
public abstract class TegoModule {

    @Binds @TegoScope public abstract TegoRuntime bindTegoRuntime(TegoRuntimeImpl runtimeImplementation);
    @Binds @TegoScope public abstract TegoEngine bindTegoEngine(TegoRuntimeImpl runtimeImplementation);
}
