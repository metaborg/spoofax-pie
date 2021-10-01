package mb.tego.strategies;

import dagger.Binds;
import dagger.Module;
import mb.tego.strategies.runtime.EagerDebugTegoRuntimeImpl;
import mb.tego.strategies.runtime.TegoEngine;
import mb.tego.strategies.runtime.TegoRuntime;
import mb.tego.strategies.runtime.TegoRuntimeImpl;

@Module
public abstract class TegoModule {

    @Binds @TegoScope public abstract TegoRuntime bindTegoRuntime(TegoEngine engine);
    @Binds @TegoScope public abstract TegoEngine bindTegoEngine(EagerDebugTegoRuntimeImpl runtimeImplementation);
}
