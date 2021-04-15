package mb.esv.eclipse;

import mb.pie.dagger.DaggerPieComponent;
import mb.pie.dagger.PieComponent;
import mb.pie.runtime.store.InMemoryStore;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;

public class EsvLanguage extends BaseEsvLanguage {
    @Override public void start(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent,
        PieComponent pieComponent
    ) {
        // Create child PIE component to isolate it from shared one, such that changes to files (especially in editors)
        // do not trigger the LWB pipeline, because that is too slow.
        final PieComponent childPieComponent = DaggerPieComponent.builder()
            .pieModule(pieComponent.createChildModule().withStoreFactory((serde, resourceService, loggerFactory) -> new InMemoryStore()))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        super.start(loggerComponent, resourceServiceComponent, platformComponent, childPieComponent);
    }

    @Override public void close() {
        if(pieComponent != null) {
            pieComponent.close();
        }
        super.close();
    }
}
