package mb.stratego.pie;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.OutTransient;
import mb.pie.api.OutTransientImpl;
import mb.pie.api.TaskDef;
import mb.stratego.common.StrategoRuntime;

import javax.inject.Provider;

/**
 * Abstract task definition that gets the Stratego runtime provider.
 *
 * Implemented by languages to provide access to the Stratego runtime provider, guarded by several dependencies that
 * ensures re-execution of Stratego strategies when the runtime changes. The Spoofax 3 compiler normally generates this
 * implementation.
 */
public abstract class GetStrategoRuntimeProvider implements TaskDef<None, OutTransient<Provider<StrategoRuntime>>> {
    protected abstract Provider<StrategoRuntime> getStrategoRuntimeProvider(ExecContext context) throws Exception;

    @Override public OutTransient<Provider<StrategoRuntime>> exec(ExecContext context, None input) throws Exception {
        return new OutTransientImpl<>(getStrategoRuntimeProvider(context), true);
    }
}
