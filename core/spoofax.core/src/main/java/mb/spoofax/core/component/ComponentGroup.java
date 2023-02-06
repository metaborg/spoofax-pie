package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.common.util.MapView;
import mb.common.util.StreamUtil;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.resource.ResourcesComponent;

import java.util.stream.Stream;

public interface ComponentGroup {
    String getGroup();


    // Resources component

    Option<ResourcesComponent> getResourcesComponent(Coordinate coordinate);

    Stream<ResourcesComponent> getResourcesComponents(CoordinateRequirement coordinateRequirement);

    default Option<ResourcesComponent> getOneResourcesComponent(CoordinateRequirement coordinateRequirement) {
        final Stream<ResourcesComponent> resourcesComponents = getResourcesComponents(coordinateRequirement);
        return StreamUtil.findOne(resourcesComponents);
    }

    Stream<ResourcesComponent> getResourcesComponents();

    // Resource service component

    ResourceServiceComponent getResourceServiceComponent();

    // Language components

    Option<LanguageComponent> getLanguageComponent(Coordinate coordinate);

    Stream<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement);

    default Option<LanguageComponent> getOneLanguageComponent(CoordinateRequirement coordinateRequirement) {
        final Stream<LanguageComponent> languageComponents = getLanguageComponents(coordinateRequirement);
        return StreamUtil.findOne(languageComponents);
    }

    Stream<LanguageComponent> getLanguageComponents();

    // PIE component

    PieComponent getPieComponent();

    // Components

    MapView<Coordinate, ? extends Component> getComponents();

    // Typed subcomponents

    <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentType);

    <T> Stream<T> getSubcomponents(Class<T> subcomponentType);

    default <T> Option<T> getOneSubcomponent(Class<T> subcomponentType) {
        final Stream<T> subcomponents = getSubcomponents(subcomponentType);
        return StreamUtil.findOne(subcomponents);
    }

    <T> Stream<T> getSubcomponents(CoordinateRequirement coordinateRequirement, Class<T> subcomponentType);

    default <T> Option<T> getOneSubcomponent(CoordinateRequirement coordinateRequirement, Class<T> subcomponentType) {
        final Stream<T> subcomponents = getSubcomponents(coordinateRequirement, subcomponentType);
        return StreamUtil.findOne(subcomponents);
    }
}
