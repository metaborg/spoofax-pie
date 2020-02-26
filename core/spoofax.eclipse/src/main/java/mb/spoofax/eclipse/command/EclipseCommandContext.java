package mb.spoofax.eclipse.command;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import java.util.Optional;

public class EclipseCommandContext extends CommandContext {
    public @Nullable CommandContext getEnclosing(CommandContextType type) {
        if(type == CommandContextType.ProjectPath) {
            if(resourcePath == null) return null;
            final ResourcePath path = resourcePath.getPath();
            if(path instanceof EclipseResourcePath) {
                final EclipseResourcePath eclipseResourcePath = (EclipseResourcePath)path;
                final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                final Optional<IProject> project = resourcePath.caseOf()
                    .<Optional<IProject>>project(p -> {
                        final @Nullable String leaf = eclipseResourcePath.getLeaf();
                        if(leaf != null) {
                            return Optional.ofNullable(root.getProject(leaf));
                        }
                        return Optional.empty();
                    })
                    .directory(d -> {
                        final @Nullable IContainer container = root.getContainerForLocation(eclipseResourcePath.getEclipsePath());
                        if(container != null) {
                            return Optional.ofNullable(container.getProject());
                        }
                        return Optional.empty();
                    })
                    .file(f -> {
                        final @Nullable IFile file = root.getFileForLocation(eclipseResourcePath.getEclipsePath());
                        if(file != null) {
                            return Optional.ofNullable(file.getProject());
                        }
                        return Optional.empty();
                    });
                if(project.isPresent()) {
                    return CommandContext.ofProject(new EclipseResourcePath(project.get()));
                }
            }
        }
        return super.getEnclosing(type);
    }
}
