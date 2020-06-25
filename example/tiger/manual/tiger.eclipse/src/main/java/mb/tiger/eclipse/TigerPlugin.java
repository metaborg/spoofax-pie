package mb.tiger.eclipse;

import mb.pie.api.ExecException;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.util.StatusUtil;
import mb.tiger.spoofax.TigerModule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import java.io.IOException;

public class TigerPlugin extends AbstractUIPlugin {
    public static final String pluginId = "tiger.eclipse";

    private static @Nullable TigerEclipseComponent component;

    public static TigerEclipseComponent getComponent() {
        if(component == null) {
            throw new RuntimeException(
                "Cannot access TigerComponent; TigerPlugin has not been started yet, or has been stopped");
        }
        return component;
    }

    @Override public void start(@NonNull BundleContext context) throws Exception {
        super.start(context);
        component = DaggerTigerEclipseComponent
            .builder()
            .platformComponent(SpoofaxPlugin.getComponent())
            .tigerModule(new TigerModule())
            .tigerEclipseModule(new TigerEclipseModule())
            .build();

        component.getEditorTracker().register();

        final WorkspaceJob job = new WorkspaceJob("Tiger startup") {
            @Override public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                try {
                    SpoofaxPlugin.getComponent().getPieRunner().startup(component, monitor);
                } catch(IOException | ExecException | InterruptedException e) {
                    throw new CoreException(StatusUtil.error("Tiger startup job failed unexpectedly", e));
                }
                return StatusUtil.success();
            }
        };
        job.setRule(component.startupWriteLockRule());
        job.schedule();
    }

    @Override public void stop(@NonNull BundleContext context) throws Exception {
        super.stop(context);
    }
}
