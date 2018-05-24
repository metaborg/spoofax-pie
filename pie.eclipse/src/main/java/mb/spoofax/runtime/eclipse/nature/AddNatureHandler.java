package mb.spoofax.runtime.eclipse.nature;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import mb.spoofax.runtime.eclipse.util.AbstractHandlerUtils;
import mb.spoofax.runtime.eclipse.util.NatureUtils;

public class AddNatureHandler extends AbstractHandler {
    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final IProject project = AbstractHandlerUtils.toProject(event);
        if(project == null)
            return null;

        final String natureId = SpoofaxNature.id;
        try {
            NatureUtils.addTo(natureId, project, null);
        } catch(CoreException e) {
            throw new ExecutionException("Cannot add nature '" + natureId + "' to project " + project, e);
        }

        return null;
    }
}
