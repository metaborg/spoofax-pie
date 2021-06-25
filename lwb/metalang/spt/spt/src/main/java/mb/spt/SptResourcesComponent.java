package mb.spt;

import dagger.Component;
import mb.spt.resource.SptTestCaseResourceModule;
import mb.spt.resource.SptTestCaseResourceRegistry;

@SptResourcesScope
@Component(
    modules = {
        SptResourcesModule.class,
        SptTestCaseResourceModule.class,
    }
)
public interface SptResourcesComponent extends BaseSptResourcesComponent, AutoCloseable {
    SptTestCaseResourceRegistry getTestCaseResourceRegistry();

    @Override default void close() {
        getTestCaseResourceRegistry().close();
    }
}
