package mb.spoofax2.common.primitive;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax2.common.primitive.generic.Spoofax2Context;

import java.util.ArrayList;
import java.util.List;

public class ProjectResourcesPrimitive extends AResourcesPrimitive {
    public ProjectResourcesPrimitive(LoggerFactory loggerFactory, ResourceService resourceService) {
        super("project_resources", loggerFactory, resourceService);
    }

    @Override protected List<HierarchicalResource> locations(Spoofax2Context context) {
        log.warn("Attempting to get project resources, but project resources are not yet supported in Spoofax 3, trying to get language resources instead");
        final ArrayList<HierarchicalResource> paths = new ArrayList<>();
        try {
            final HierarchicalResource path = resourceService.getHierarchicalResource(context.languagePath);
            paths.add(path);
        } catch(ResourceRuntimeException e) {
            log.error("Getting project resources failed unexpectedly", e);
        }
        return paths;
    }
}
