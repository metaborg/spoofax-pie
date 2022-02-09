package mb.tiger.eclipse;

import mb.pie.api.ExecException;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.eclipse.EclipseParticipant;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.spoofax.eclipse.EclipseResourceServiceComponent;
import mb.spoofax.eclipse.log.EclipseLoggerComponent;
import mb.spoofax.eclipse.resource.EclipseClassLoaderToNativeResolver;
import mb.spoofax.eclipse.resource.EclipseClassLoaderUrlResolver;
import mb.spoofax.eclipse.util.StatusUtil;
import mb.tiger.spoofax.TigerModule;
import mb.tiger.spoofax.TigerParticipant;
import mb.tiger.spoofax.TigerResourcesModule;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import java.io.IOException;

public class TigerEclipseParticipant extends TigerParticipant<EclipseLoggerComponent, EclipseResourceServiceComponent, EclipsePlatformComponent> implements EclipseParticipant {
    protected @Nullable TigerEclipseComponent eclipseComponent;
    protected @Nullable PieComponent pieComponent;


    public TigerEclipseComponent getComponent() {
        if(eclipseComponent == null) {
            throw new RuntimeException("TigerEclipseComponent has not been initialized yet or has been deinitialized");
        }
        return eclipseComponent;
    }

    public PieComponent getPieComponent() {
        if(pieComponent == null) {
            throw new RuntimeException("PieComponent has not been initialized yet or has been deinitialized");
        }
        return pieComponent;
    }


    @Override
    protected TigerResourcesModule createResourcesModule(
        EclipseLoggerComponent loggerComponent,
        EclipseResourceServiceComponent baseResourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        return new TigerResourcesModule(
            new EclipseClassLoaderUrlResolver(),
            new EclipseClassLoaderToNativeResolver(baseResourceServiceComponent.getEclipseResourceRegistry())
        );
    }


    @Override
    public TigerEclipseComponent getLanguageComponent(
        EclipseLoggerComponent loggerComponent,
        EclipseResourceServiceComponent baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent
    ) {
        if(eclipseComponent == null) {
            final TigerModule module = createModule(loggerComponent, baseResourceServiceComponent, resourceServiceComponent, platformComponent);
            customizeModule(loggerComponent, baseResourceServiceComponent, resourceServiceComponent, platformComponent, module);
            final DaggerTigerEclipseComponent.Builder builder = DaggerTigerEclipseComponent.builder()
                .tigerModule(module)
                .eclipseLoggerComponent(loggerComponent)
                .tigerResourcesComponent(getResourceRegistriesProvider(loggerComponent, baseResourceServiceComponent, platformComponent))
                .resourceServiceComponent(resourceServiceComponent)
                .eclipsePlatformComponent(platformComponent);
            eclipseComponent = builder.build();
            component = builder.build();
        }
        return eclipseComponent;
    }


    @Override public void start(
        EclipseLoggerComponent loggerComponent,
        EclipseResourceServiceComponent baseResourceServiceComponent, ResourceServiceComponent resourceServiceComponent,
        EclipsePlatformComponent platformComponent,
        PieComponent pieComponent
    ) {
        this.pieComponent = pieComponent;
        final TigerEclipseComponent component = getComponent();
        component.getEditorTracker().register();
        final WorkspaceJob job = new WorkspaceJob("TIGER startup") {
            @Override public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                try {
                    platformComponent.getPieRunner().startup(component, pieComponent, monitor);
                } catch(IOException | ExecException | InterruptedException e) {
                    throw new CoreException(StatusUtil.error("TIGER startup job failed unexpectedly", e));
                }
                return StatusUtil.success();
            }
        };
        job.setRule(MultiRule.combine(new ISchedulingRule[]{
            component.startupWriteLockRule(),
            // Require refresh rule for the workspace, as the startup job walks over all projects, which refreshes them.
            ResourcesPlugin.getWorkspace().getRuleFactory().refreshRule(ResourcesPlugin.getWorkspace().getRoot()),
        }));
        job.schedule();
    }

    @Override public void close() {
        pieComponent = null;
        if(eclipseComponent != null) {
            eclipseComponent.getEditorTracker().unregister();
            eclipseComponent.close();
            eclipseComponent = null;
        }
        super.close();
    }
}
