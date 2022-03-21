package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.MapView;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.resource.ResourcesComponent;

import java.util.stream.Stream;

public class ComponentGroupImpl implements ComponentGroup {
    public final String group;
    public ResourceServiceComponent resourceServiceComponent;
    public PieComponent pieComponent;
    public MapView<Coordinate, ComponentImpl> components;
    private boolean closed = false;

    public ComponentGroupImpl(
        String group,
        ResourceServiceComponent resourceServiceComponent,
        PieComponent pieComponent,
        MapView<Coordinate, ComponentImpl> components
    ) {
        this.group = group;
        this.resourceServiceComponent = resourceServiceComponent;
        this.pieComponent = pieComponent;
        this.components = components;
    }

    public void close() {
        if(closed) return;
        pieComponent.close();
        pieComponent = null;
        resourceServiceComponent.close();
        resourceServiceComponent = null;
        closed = true;
    }


    @Override
    public String getGroup() {
        return group;
    }


    // Resources subcomponents

    @Override
    public Option<ResourcesComponent> getResourcesComponent(Coordinate coordinate) {
        if(closed)
            throw new IllegalStateException("Cannot get resources component, component group '" + group + "' has been closed");
        return Option.ofNullable(components.get(coordinate))
            .flatMap(ComponentImpl::getResourcesComponent);
    }

    @Override
    public Stream<ResourcesComponent> getResourcesComponents(CoordinateRequirement coordinateRequirement) {
        if(closed)
            throw new IllegalStateException("Cannot get resources components, component group '" + group + "' has been closed");
        return components.values().stream()
            .filter(c -> c.matchesCoordinate(coordinateRequirement))
            .flatMap(c -> c.getResourcesComponent().stream());
    }

    @Override
    public Stream<ResourcesComponent> getResourcesComponents() {
        if(closed)
            throw new IllegalStateException("Cannot get resources components, component group '" + group + "' has been closed");
        return components.values().stream()
            .flatMap(c -> c.getResourcesComponent().stream());
    }


    // Resource service component

    @Override
    public ResourceServiceComponent getResourceServiceComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get resource service component, component group '" + group + "' has been closed");
        return resourceServiceComponent;
    }

    // Language subcomponents

    @Override
    public Option<LanguageComponent> getLanguageComponent(Coordinate coordinate) {
        if(closed)
            throw new IllegalStateException("Cannot get language component, component group '" + group + "' has been closed");
        return Option.ofNullable(components.get(coordinate))
            .flatMap(ComponentImpl::getLanguageComponent);
    }

    @Override
    public Stream<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement) {
        if(closed)
            throw new IllegalStateException("Cannot get language components, component group '" + group + "' has been closed");
        return components.values().stream()
            .filter(c -> c.matchesCoordinate(coordinateRequirement))
            .flatMap(c -> c.getLanguageComponent().stream());
    }

    @Override
    public Stream<LanguageComponent> getLanguageComponents() {
        if(closed)
            throw new IllegalStateException("Cannot get language components, component group '" + group + "' has been closed");
        return components.values().stream()
            .flatMap(c -> c.getLanguageComponent().stream());
    }

    // PIE component

    @Override
    public PieComponent getPieComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get PIE component, component group '" + group + "' has been closed");
        return pieComponent;
    }

    // Components

    @Override public MapView<Coordinate, ? extends Component> getComponents() {
        if(closed)
            throw new IllegalStateException("Cannot get component, component group '" + group + "' has been closed");
        return components;
    }

    // Typed subcomponents

    @Override public <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentType) {
        if(closed)
            throw new IllegalStateException("Cannot get subcomponent, component group '" + group + "' has been closed");
        return Option.ofNullable(components.get(coordinate))
            .flatMap(c -> c.getSubcomponent(subcomponentType));
    }

    @Override public <T> Stream<T> getSubcomponents(Class<T> subcomponentType) {
        if(closed)
            throw new IllegalStateException("Cannot get subcomponents, component group '" + group + "' has been closed");
        return components.values().stream()
            .flatMap(c -> c.getSubcomponent(subcomponentType).stream());
    }

    @Override
    public <T> Stream<T> getSubcomponents(CoordinateRequirement coordinateRequirement, Class<T> subcomponentType) {
        if(closed)
            throw new IllegalStateException("Cannot get subcomponents, component group '" + group + "' has been closed");
        return components.values().stream()
            .filter(c -> c.matchesCoordinate(coordinateRequirement))
            .flatMap(c -> c.getSubcomponent(subcomponentType).stream());
    }
}
