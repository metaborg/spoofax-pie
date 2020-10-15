package mb.spoofax2.common.primitive;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax2.common.primitive.generic.Spoofax2LanguageContext;

import java.util.List;

public class ProjectResourcesPrimitive extends AResourcesPrimitive {
    public ProjectResourcesPrimitive(LoggerFactory loggerFactory, ResourceService resourceService) {
        super("project_resources", loggerFactory, resourceService);
    }

    @Override protected List<HierarchicalResource> locations(Spoofax2LanguageContext context) {
        throw new UnsupportedOperationException("Attempting to get project resources, but project resources are not supported in Spoofax 3");
    }
}
