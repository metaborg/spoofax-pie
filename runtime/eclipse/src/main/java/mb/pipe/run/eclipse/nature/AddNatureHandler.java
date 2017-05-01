package mb.pipe.run.eclipse.nature;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.util.AbstractHandlerUtils;
import mb.pipe.run.eclipse.util.NatureUtils;

public class AddNatureHandler extends AbstractHandler {
    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final IProject project = AbstractHandlerUtils.toProject(event);
        if(project == null)
            return null;

        try {
            NatureUtils.addTo(PipePlugin.id, project, null);
        } catch(CoreException e) {
            throw new ExecutionException("Cannot add Pipe nature", e);
        }

        return null;
    }
}
