package mb.spoofax.eclipse.command;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.EnclosingCommandContextType;
import mb.spoofax.core.language.command.ResourcePathWithKind;
import mb.spoofax.core.platform.Platform;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@PlatformScope
public class EnclosingCommandContextProvider {
    private final Logger logger;
    private final ResourceService resourceService;

    @Inject
    public EnclosingCommandContextProvider(LoggerFactory loggerFactory, ResourceService resourceService) {
        this.logger = loggerFactory.create(EnclosingCommandContextProvider.class);
        this.resourceService = resourceService;
    }

    public @Nullable CommandContext selectRequired(CommandContext context, Set<EnclosingCommandContextType> requiredTypes) {
        if(requiredTypes.isEmpty()) return context;
        for(EnclosingCommandContextType type : requiredTypes) {
            final @Nullable CommandContext enclosing = getEnclosing(context, type);
            if(enclosing != null) {
                context.setEnclosing(type, enclosing);
                return context;
            }
        }
        return null;
    }

    public Stream<CommandContext> filterRequired(Stream<CommandContext> contexts, Set<EnclosingCommandContextType> requiredTypes) {
        if(requiredTypes.isEmpty()) return contexts;
        return contexts.map(context -> selectRequired(context, requiredTypes)).filter(Objects::nonNull);
    }

    public @Nullable CommandContext getEnclosing(CommandContext context, EnclosingCommandContextType type) {
        switch(type) {
            case Project: {
                final @Nullable ResourcePathWithKind resourcePath = context.getResourcePathWithKind().orElse(null);
                if(resourcePath == null) return null;
                final ResourcePath path = resourcePath.getPath();
                if(path instanceof EclipseResourcePath) {
                    final EclipseResourcePath eclipseResourcePath = (EclipseResourcePath)path;
                    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                    final @Nullable IResource eclipseResource = root.findMember(eclipseResourcePath.getEclipsePath());
                    if(eclipseResource == null) return null;
                    final @Nullable IProject project = eclipseResource.getProject();
                    if(project == null) return null;
                    return CommandContext.ofProject(new EclipseResourcePath(project));
                }
                break;
            }
            case Directory: {
                final @Nullable ResourcePathWithKind resourcePath = context.getResourcePathWithKind().orElse(null);
                if(resourcePath == null) return null;
                final ResourcePath path = resourcePath.getPath();
                final @Nullable ResourcePath parent = path.getParent();
                if(parent == null) return null;
                try {
                    final HierarchicalResource directory = resourceService.getHierarchicalResource(parent);
                    if(!directory.isDirectory()) {
                        return null;
                    }
                    return CommandContext.ofDirectory(parent);
                } catch(ResourceRuntimeException | IOException e) {
                    logger.error("Failed to get enclosing directory of '{}'", e, path);
                    return null;
                }
            }
        }
        return null;
    }
}
