package mb.spoofax2.common.primitive;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax2.common.primitive.generic.Spoofax2LanguageContext;
import mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ProjectResourcesPrimitive extends AResourcesPrimitive {
    public ProjectResourcesPrimitive(LoggerFactory loggerFactory, ResourceService resourceService) {
        super("project_resources", loggerFactory, resourceService);
    }

    @Override
    protected List<HierarchicalResource> locations(Spoofax2LanguageContext languageContext, @Nullable Spoofax2ProjectContext projectContext) {
        final ArrayList<HierarchicalResource> paths = new ArrayList<>();
        if(projectContext == null) {
            log.warn("No project context was set");
            return paths;
        }
        try {
            final HierarchicalResource path = resourceService.getHierarchicalResource(projectContext.projectPath);
            paths.add(path);
        } catch(ResourceRuntimeException e) {
            log.error("Getting project resources failed unexpectedly", e);
        }
        return paths;
    }
}
