package mb.tiger.eclipse;

import mb.spoofax.eclipse.util.AbstractHandlerUtils;
import mb.spoofax.eclipse.util.NatureUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class TigerRemoveNatureHandler extends AbstractHandler {
    @Override public @Nullable Object execute(@NonNull ExecutionEvent event) throws ExecutionException {
        final @Nullable IProject project = AbstractHandlerUtils.toProject(event);
        if(project == null) return null;
        try {
            NatureUtils.removeFrom(TigerProjectNature.id, project, null);
        } catch(CoreException e) {
            throw new ExecutionException("Adding Tiger nature failed unexpectedly", e);
        }
        return null;
    }
}
