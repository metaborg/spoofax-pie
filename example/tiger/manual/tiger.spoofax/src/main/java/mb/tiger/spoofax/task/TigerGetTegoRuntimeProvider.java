package mb.tiger.spoofax.task;

import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.tego.pie.GetTegoRuntimeProvider;
import mb.tego.strategies.runtime.TegoRuntime;
import mb.tiger.TigerClassloaderResources;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;
import javax.inject.Provider;

// TODO: Make this a template
@TigerScope
public class TigerGetTegoRuntimeProvider extends GetTegoRuntimeProvider {
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
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        return tegoRuntimeProvider;
    }
}
