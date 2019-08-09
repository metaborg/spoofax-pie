package mb.spoofax.core.language.command.arg;

import mb.common.region.Region;
import mb.common.util.MapView;
import mb.resource.ResourceKey;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.HashMap;

@Singleton
public class ArgConverters {
    private final ResourceService resourceService;


    @Inject public ArgConverters(ResourceService resourceService) {
        this.resourceService = resourceService;
    }


    public final ArgConverter<ResourceKey> resourceKeyConverter = new ArgConverter<ResourceKey>() {
        @Override public ResourceKey convert(String argStr) throws ResourceRuntimeException {
            return resourceService.getResourceKey(argStr);
        }

        @Override public Class<ResourceKey> getOutputClass() {
            return ResourceKey.class;
        }
    };

    public final ArgConverter<ResourcePath> resourcePathConverter = new ArgConverter<ResourcePath>() {
        @Override public ResourcePath convert(String argStr) throws ResourceRuntimeException {
            return resourceService.getResourcePath(argStr);
        }

        @Override public Class<ResourcePath> getOutputClass() {
            return ResourcePath.class;
        }
    };

    public final ArgConverter<Region> regionConverter = new ArgConverter<Region>() {
        @Override public Region convert(String argStr) throws IllegalArgumentException {
            return Region.fromString(argStr);
        }

        @Override public Class<Region> getOutputClass() {
            return Region.class;
        }
    };


    public final MapView<Class<? extends Serializable>, ArgConverter<?>> allConverters = MapView.of(() -> {
        final HashMap<Class<? extends Serializable>, ArgConverter<?>> map = new HashMap<>();
        map.put(resourceKeyConverter.getOutputClass(), resourceKeyConverter);
        map.put(resourcePathConverter.getOutputClass(), resourcePathConverter);
        map.put(regionConverter.getOutputClass(), regionConverter);
        return map;
    });
}
