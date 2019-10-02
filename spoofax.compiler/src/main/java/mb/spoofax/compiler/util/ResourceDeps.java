package mb.spoofax.compiler.util;

import mb.resource.hierarchical.HierarchicalResource;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface ResourceDeps {
    List<HierarchicalResource> requiredResources();

    List<HierarchicalResource> providedResources();
}
