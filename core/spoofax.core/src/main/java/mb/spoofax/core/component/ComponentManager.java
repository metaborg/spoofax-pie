package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.common.util.MapView;
import mb.log.dagger.LoggerComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;

public interface ComponentManager extends AutoCloseable {
    LoggerComponent getLoggerComponent();

    PlatformComponent getPlatformComponent();


    // Components

    Option<? extends Component> getComponent(Coordinate coordinate);

    CollectionView<? extends Component> getComponents(CoordinateRequirement coordinateRequirement);

    default Option<? extends Component> getOneComponent(CoordinateRequirement coordinateRequirement) {
        final CollectionView<? extends Component> components = getComponents(coordinateRequirement);
        if(components.size() == 1) {
            return Option.ofSome(components.iterator().next());
        } else {
            return Option.ofNone();
        }
    }

    Option<? extends ComponentGroup> getComponentGroup(String group);

    MapView<String, ? extends ComponentGroup> getComponentGroups();


    // Language components (of components)

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


    // Typed subcomponents

    <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentType);

    <T> CollectionView<T> getSubcomponents(Class<T> subcomponentType);

    default <T> Option<T> getOneSubcomponent(Class<T> subcomponentType) {
        final CollectionView<T> languageComponents = getSubcomponents(subcomponentType);
        if(languageComponents.size() == 1) {
            return Option.ofSome(languageComponents.iterator().next());
        } else {
            return Option.ofNone();
        }
    }

    <T> CollectionView<T> getSubcomponents(CoordinateRequirement coordinateRequirement, Class<T> subcomponentType);

    default <T> Option<T> getOneSubcomponent(CoordinateRequirement coordinateRequirement, Class<T> subcomponentType) {
        final CollectionView<T> languageComponents = getSubcomponents(coordinateRequirement, subcomponentType);
        if(languageComponents.size() == 1) {
            return Option.ofSome(languageComponents.iterator().next());
        } else {
            return Option.ofNone();
        }
    }


    @Override void close();
}
