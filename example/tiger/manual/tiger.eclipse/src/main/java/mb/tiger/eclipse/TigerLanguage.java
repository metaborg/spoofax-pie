package mb.tiger.eclipse;

import mb.pie.api.ExecException;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.eclipse.EclipseLifecycleParticipant;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.EclipseResourceServiceComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.eclipse.resource.EclipseClassLoaderToNativeResolver;
import mb.spoofax.eclipse.resource.EclipseClassLoaderUrlResolver;
import mb.spoofax.eclipse.util.StatusUtil;
import mb.tiger.spoofax.DaggerTigerResourcesComponent;
import mb.tiger.spoofax.TigerResourcesComponent;
import mb.tiger.spoofax.TigerResourcesModule;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import java.io.IOException;

public class TigerLanguage implements EclipseLifecycleParticipant {
    private static @Nullable TigerLanguage instance;
    private @Nullable TigerResourcesComponent resourcesComponent;
    private @Nullable TigerEclipseComponent component;
    private @Nullable PieComponent pieComponent;

    private TigerLanguage() {}


    public static TigerLanguage getInstance() {
        if(instance == null) {
            instance = new TigerLanguage();
        }
        return instance;
    }


    public TigerResourcesComponent getResourcesComponent() {
        if(resourcesComponent == null) {
            throw new RuntimeException("TigerResourcesComponent has not been initialized yet or has been closed");
        }
        return resourcesComponent;
    }

    public TigerEclipseComponent getComponent() {
        if(component == null) {
            throw new RuntimeException("TigerEclipseComponent has not been initialized yet or has been closed");
        }
        return component;
    }

    public PieComponent getPieComponent() {
        if(pieComponent == null) {
            throw new RuntimeException("PieComponent of Tiger has not been initialized yet or has been closed");
        }
        return pieComponent;
    }


    @Override public TigerResourcesComponent getResourceRegistriesProvider(
        EclipseLoggerComponent loggerComponent,
        EclipseResourceServiceComponent baseResourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        if(resourcesComponent == null) {
            resourcesComponent = DaggerTigerResourcesComponent.builder()
                .tigerResourcesModule(new TigerResourcesModule(
                    new EclipseClassLoaderUrlResolver(),
                    new EclipseClassLoaderToNativeResolver(baseResourceServiceComponent.getEclipseResourceRegistry()))
                )
                .build();
        }
        return resourcesComponent;
    }

    @Override public TigerEclipseComponent getTaskDefsProvider(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        return getLanguageComponent(loggerComponent, resourceServiceComponent, platformComponent);
    }

    @Override public TigerEclipseComponent getLanguageComponent(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        if(component == null) {
            component = DaggerTigerEclipseComponent.builder()
                .eclipseLoggerComponent(loggerComponent)
                .tigerResourcesComponent(getResourcesComponent())
                .resourceServiceComponent(resourceServiceComponent)
                .eclipsePlatformComponent(platformComponent)
                .build();
        }
        return component;
    }

    @Override public void start(
        EclipseLoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent,
        PieComponent pieComponent
    ) {
        this.pieComponent = pieComponent;
        final TigerEclipseComponent component = getComponent();
        component.getEditorTracker().register();
        final WorkspaceJob job = new WorkspaceJob("Tiger startup") {
            @Override public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                try {
                    platformComponent.getPieRunner().startup(component, pieComponent, monitor);
                } catch(IOException | ExecException | InterruptedException e) {
                    throw new CoreException(StatusUtil.error("Tiger startup job failed unexpectedly", e));
                }
                return StatusUtil.success();
            }
        };
        job.setRule(component.startupWriteLockRule());
        job.schedule();
    }

    @Override public void close() {
        if(pieComponent != null) {
            // PIE component is closed by SpoofaxPlugin.
            pieComponent = null;
        }
        if(component != null) {
            component.close();
            component = null;
        }
        resourcesComponent = null;
    }
}
