package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.common.util.MapView;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;

public interface ComponentGroup {
    String getGroup();


    // Resource service component

    ResourceServiceComponent getResourceServiceComponent();

    // Language components

    Option<LanguageComponent> getLanguageComponent(Coordinate coordinate);

    CollectionView<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement);

    default Option<LanguageComponent> getOneLanguageComponent(CoordinateRequirement coordinateRequirement) {
        final CollectionView<LanguageComponent> languageComponents = getLanguageComponents(coordinateRequirement);
        if(languageComponents.size() == 1) {
            return Option.ofSome(languageComponents.iterator().next());
        } else {
            return Option.ofNone();
        }
    }

    CollectionView<LanguageComponent> getLanguageComponents();

    // PIE component

    PieComponent getPieComponent();

    // Components

    MapView<Coordinate, ? extends Component> getComponents();

    // Typed subcomponents

    <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentClass);

    <T> CollectionView<T> getSubcomponents(Class<T> subcomponentClass);

    default <T> Option<T> getOneSubcomponent(Class<T> subcomponentClass) {
        final CollectionView<T> languageComponents = getSubcomponents(subcomponentClass);
        if(languageComponents.size() == 1) {
            return Option.ofSome(languageComponents.iterator().next());
        } else {
            return Option.ofNone();
        }
    }

    <T> CollectionView<T> getSubcomponents(CoordinateRequirement coordinateRequirement, Class<T> subcomponentClass);

    default <T> Option<T> getOneSubcomponent(CoordinateRequirement coordinateRequirement, Class<T> subcomponentClass) {
        final CollectionView<T> languageComponents = getSubcomponents(coordinateRequirement, subcomponentClass);
        if(languageComponents.size() == 1) {
            return Option.ofSome(languageComponents.iterator().next());
        } else {
            return Option.ofNone();
        }
    }
}
