package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.MapView;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

public class CompositeComponentManager implements ComponentManager {
    private final ComponentManager baseComponentManager;
    private final ComponentManager extendComponentManager;


    public CompositeComponentManager(ComponentManager baseComponentManager, ComponentManager extendComponentManager) {
        this.baseComponentManager = baseComponentManager;
        this.extendComponentManager = extendComponentManager;
    }

    @Override
    public void close() {
        extendComponentManager.close();
        baseComponentManager.close();
    }


    @Override
    public Option<? extends Component> getComponent(Coordinate coordinate) {
        return baseComponentManager.getComponent(coordinate)
            .mapOrElse(Option::ofSome, () -> extendComponentManager.getComponent(coordinate));
    }

    @Override
    public Stream<? extends Component> getComponents() {
        final Stream<? extends Component> baseComponents = baseComponentManager.getComponents();
        final Stream<? extends Component> extendComponents = extendComponentManager.getComponents();
        return Stream.concat(baseComponents, extendComponents);
    }

    @Override
    public Stream<? extends Component> getComponents(CoordinateRequirement coordinateRequirement) {
        final Stream<? extends Component> baseComponents = baseComponentManager.getComponents(coordinateRequirement);
        final Stream<? extends Component> extendComponents = extendComponentManager.getComponents(coordinateRequirement);
        return Stream.concat(baseComponents, extendComponents);
    }

    @Override
    public Option<? extends ComponentGroup> getComponentGroup(String group) {
        return baseComponentManager.getComponentGroup(group)
            .mapOrElse(Option::ofSome, () -> extendComponentManager.getComponentGroup(group));
    }

    @Override
    public MapView<String, ? extends ComponentGroup> getComponentGroups() {
        final MapView<String, ? extends ComponentGroup> baseComponentGroups = baseComponentManager.getComponentGroups();
        final MapView<String, ? extends ComponentGroup> extendComponentGroups = extendComponentManager.getComponentGroups();
        final LinkedHashMap<String, ComponentGroup> componentGroups = new LinkedHashMap<>(baseComponentGroups.size(), extendComponentGroups.size());
        componentGroups.putAll(baseComponentGroups.asUnmodifiable());
        componentGroups.putAll(extendComponentGroups.asUnmodifiable());
        return MapView.of(componentGroups);
    }


    @Override
    public Option<LanguageComponent> getLanguageComponent(Coordinate coordinate) {
        return baseComponentManager.getLanguageComponent(coordinate)
            .mapOrElse(Option::ofSome, () -> extendComponentManager.getLanguageComponent(coordinate));
    }

    @Override
    public Stream<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement) {
        final Stream<LanguageComponent> baseComponents = baseComponentManager.getLanguageComponents(coordinateRequirement);
        final Stream<LanguageComponent> extendComponents = extendComponentManager.getLanguageComponents(coordinateRequirement);
        return Stream.concat(baseComponents, extendComponents);
    }


    @Override
    public <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentType) {
        return baseComponentManager.getSubcomponent(coordinate, subcomponentType)
            .mapOrElse(Option::ofSome, () -> extendComponentManager.getSubcomponent(coordinate, subcomponentType));
    }

    @Override
    public <T> Stream<T> getSubcomponents(Class<T> subcomponentType) {
        final Stream<T> baseSubcomponents = baseComponentManager.getSubcomponents(subcomponentType);
        final Stream<T> extendSubcomponents = extendComponentManager.getSubcomponents(subcomponentType);
        return Stream.concat(baseSubcomponents, extendSubcomponents);
    }

    @Override
    public <T> Stream<T> getSubcomponents(CoordinateRequirement coordinateRequirement, Class<T> subcomponentType) {
        final Stream<T> baseSubcomponents = baseComponentManager.getSubcomponents(coordinateRequirement, subcomponentType);
        final Stream<T> extendSubcomponents = extendComponentManager.getSubcomponents(coordinateRequirement, subcomponentType);
        return Stream.concat(baseSubcomponents, extendSubcomponents);
    }
}
