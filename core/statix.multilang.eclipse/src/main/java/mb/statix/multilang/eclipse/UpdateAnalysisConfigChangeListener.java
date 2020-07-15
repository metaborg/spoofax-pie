package mb.statix.multilang.eclipse;

import mb.log.api.Logger;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.TopDownSession;
import mb.spoofax.core.pie.PieProvider;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.Collections;

public class UpdateAnalysisConfigChangeListener implements ConfigChangeListener {
    private static final Logger logger = SpoofaxPlugin.getComponent().getLoggerFactory()
        .create(UpdateAnalysisConfigChangeListener.class);

    private final PieRunner pieRunner = SpoofaxPlugin.getComponent().getPieRunner();
    private final PieProvider pieProvider;
    private final EclipseLanguageComponent languageComponent;

    public UpdateAnalysisConfigChangeListener(EclipseLanguageComponent languageComponent) {
        this.pieProvider = languageComponent.getPieProvider();
        this.languageComponent = languageComponent;
    }

    @Override
    public void configChanged(IProject project, @Nullable IProgressMonitor monitor) {
        final EclipseResourcePath projectPath = new EclipseResourcePath(project.getFullPath());
        try(MixedSession session = pieProvider.getPie(projectPath).newSession()) {
            TopDownSession postSession = session.updateAffectedBy(Collections.singleton(projectPath));
            pieRunner.requireCheck(project, monitor, postSession, languageComponent);
        } catch(InterruptedException | ExecException e) {
            pieRunner.clearMessages(project, monitor, languageComponent);
            logger.error("Error executing analysis after configuration update", e);
        } catch(Throwable e) { // Clear invalid workspace state when unexpected error occurs
            pieRunner.clearMessages(project, monitor, languageComponent);
            throw e;
        }
    }
}
