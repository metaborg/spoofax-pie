package mb.spoofax2.common.primitive;

import mb.log.api.LoggerFactory;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax2.common.primitive.generic.Spoofax2LanguageContext;
import mb.spoofax2.common.primitive.generic.Spoofax2ProjectContext;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;

public class LanguageResourcesPrimitive extends AResourcesPrimitive {
    public LanguageResourcesPrimitive(LoggerFactory loggerFactory, ResourceService resourceService) {
        super("language_resources", loggerFactory, resourceService);
    }

    @Override protected ArrayList<HierarchicalResource> locations(Spoofax2LanguageContext languageContext, @Nullable Spoofax2ProjectContext projectContext) {
        final ArrayList<HierarchicalResource> paths = new ArrayList<>();
        try {
            final HierarchicalResource path = resourceService.getHierarchicalResource(languageContext.languagePath);
            paths.add(path);
        } catch(ResourceRuntimeException e) {
            log.error("Getting language resources failed unexpectedly", e);
        }
        return paths;
    }
}
