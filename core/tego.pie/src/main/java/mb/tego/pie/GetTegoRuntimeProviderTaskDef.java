package mb.tego.pie;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.OutTransient;
import mb.pie.api.OutTransientEquatableImpl;
import mb.pie.api.TaskDef;
import mb.tego.strategies.runtime.TegoRuntime;

import javax.inject.Provider;

/**
 * Abstract task definition that gets the Tego runtime provider.
 *
 * Implemented by languages to provide access to the Tego runtime provider, guarded by several dependencies that
 * ensures re-execution of Tego strategies when the runtime changes. The Spoofax 3 compiler normally generates this
 * implementation.
 */
public abstract class GetTegoRuntimeProviderTaskDef implements TaskDef<None, OutTransient<Provider<TegoRuntime>>> {
    protected abstract Provider<TegoRuntime> getTegoRuntimeProvider(ExecContext context) throws Exception;

    @Override public OutTransient<Provider<TegoRuntime>> exec(ExecContext context, None input) throws Exception {
        // Use OutTransientEquatableImpl with System.currentTimeMillis() as equatable value, to ensure that tasks that
        // depend on this task get re-executed whenever this task gets executed, because its timestamp will change.
        return new OutTransientEquatableImpl<>(getTegoRuntimeProvider(context), System.currentTimeMillis(), true);
    }
}
