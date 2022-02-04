package mb.spoofax.lwb.dynamicloading.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.component.ComponentManager;

public interface DynamicComponentManager extends ComponentManager {
    Option<DynamicComponent> getDynamicComponent(ResourcePath rootDirectory);

    Option<DynamicComponent> getDynamicComponent(Coordinate coordinate);

    CollectionView<DynamicComponent> getDynamicComponents(CoordinateRequirement coordinateRequirement);

    default Option<DynamicComponent> getOneDynamicComponent(CoordinateRequirement coordinateRequirement) {
        final CollectionView<DynamicComponent> components = getDynamicComponents(coordinateRequirement);
        if(components.size() == 1) {
            return Option.ofSome(components.iterator().next());
        } else {
            return Option.ofNone();
        }
    }

    Option<DynamicComponent> getDynamicComponent(String fileExtension);


    @Override void close();
}
