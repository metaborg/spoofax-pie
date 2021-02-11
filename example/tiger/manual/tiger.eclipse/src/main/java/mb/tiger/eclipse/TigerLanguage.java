package mb.tiger.eclipse;

import mb.pie.api.ExecException;
import mb.spoofax.core.platform.ResourceServiceComponent;
import mb.spoofax.eclipse.EclipseLanguage;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.util.StatusUtil;
import mb.tiger.spoofax.DaggerTigerResourcesComponent;
import mb.tiger.spoofax.TigerResourcesComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import java.io.IOException;

public class TigerLanguage implements EclipseLanguage {
    private static @Nullable TigerLanguage instance;
    private @Nullable TigerResourcesComponent resourcesComponent;
    private @Nullable TigerEclipseComponent component;

    private TigerLanguage() {}


    public static TigerLanguage getInstance() {
        if(instance == null) {
            instance = new TigerLanguage();
        }
        return instance;
    }


    public TigerResourcesComponent getResourcesComponent() {
        if(resourcesComponent == null) {
            throw new RuntimeException("TigerResourcesComponent has not been initialized yet");
        }
        return resourcesComponent;
    }

    public TigerEclipseComponent getComponent() {
        if(component == null) {
            throw new RuntimeException("TigerEclipseComponent has not been initialized yet");
        }
        return component;
    }


    @Override public TigerResourcesComponent createResourcesComponent() {
        if(resourcesComponent == null) {
            resourcesComponent = DaggerTigerResourcesComponent.create();
        }
        return resourcesComponent;
    }

    @Override public TigerEclipseComponent createComponent(
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        if(component == null) {
            component = DaggerTigerEclipseComponent.builder()
                .tigerResourcesComponent(createResourcesComponent())
                .resourceServiceComponent(resourceServiceComponent)
                .eclipsePlatformComponent(platformComponent)
                .build();
        }
        return component;
    }

    @Override public void start(
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        final TigerEclipseComponent component = getComponent();
        component.getEditorTracker().register();
        final WorkspaceJob job = new WorkspaceJob("Tiger startup") {
            @Override public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                try {
                    platformComponent.getPieRunner().startup(component, monitor);
                } catch(IOException | ExecException | InterruptedException e) {
                    throw new CoreException(StatusUtil.error("Tiger startup job failed unexpectedly", e));
                }
                return StatusUtil.success();
            }
        };
        job.setRule(component.startupWriteLockRule());
        job.schedule();
    }
}
