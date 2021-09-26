package mb.tiger.spoofax.task;

import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import mb.tego.pie.GetTegoRuntimeProviderTaskDef;
import mb.tego.strategies.runtime.TegoRuntime;
import mb.tiger.TigerClassloaderResources;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;
import javax.inject.Provider;

// TODO: Make this a template
@TigerScope
public class TigerGetTegoRuntimeProvider extends GetTegoRuntimeProviderTaskDef {
    private final TigerClassloaderResources classLoaderResources;
    private final Provider<TegoRuntime> tegoRuntimeProvider;

    @Inject
    public TigerGetTegoRuntimeProvider(
        TigerClassloaderResources classLoaderResources,
        Provider<TegoRuntime> tegoRuntimeProvider
    ) {
        this.classLoaderResources = classLoaderResources;
        this.tegoRuntimeProvider = tegoRuntimeProvider;
    }

    @Override public String getId() {
        return this.getClass().getName();
    }

    @Override protected Provider<TegoRuntime> getTegoRuntimeProvider(ExecContext context) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        return tegoRuntimeProvider;
    }
}
