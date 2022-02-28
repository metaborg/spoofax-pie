package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.common.util.MapView;
import mb.common.util.StreamUtil;
import mb.log.dagger.LoggerComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;

import java.util.stream.Stream;

public interface ComponentManager extends AutoCloseable {
    LoggerComponent getLoggerComponent();

    PlatformComponent getPlatformComponent();


    // Components

    Option<? extends Component> getComponent(Coordinate coordinate);

    CollectionView<? extends Component> getComponents();

    Stream<? extends Component> getComponents(CoordinateRequirement coordinateRequirement);

    default Option<? extends Component> getOneComponent(CoordinateRequirement coordinateRequirement) {
        final Stream<? extends Component> components = getComponents(coordinateRequirement);
        return StreamUtil.findOne(components);
    }

    Option<? extends ComponentGroup> getComponentGroup(String group);

    MapView<String, ? extends ComponentGroup> getComponentGroups();


    // Language components (of components)

    Option<LanguageComponent> getLanguageComponent(Coordinate coordinate);

    Stream<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement);

    default Option<LanguageComponent> getOneLanguageComponent(CoordinateRequirement coordinateRequirement) {
        final Stream<LanguageComponent> languageComponents = getLanguageComponents(coordinateRequirement);
        return StreamUtil.findOne(languageComponents);
    }


    // Typed subcomponents

    <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentType);

    <T> Stream<T> getSubcomponents(Class<T> subcomponentType);

    default <T> Option<T> getOneSubcomponent(Class<T> subcomponentType) {
        final Stream<T> languageComponents = getSubcomponents(subcomponentType);
        return StreamUtil.findOne(languageComponents);
    }

    <T> Stream<T> getSubcomponents(CoordinateRequirement coordinateRequirement, Class<T> subcomponentType);

    default <T> Option<T> getOneSubcomponent(CoordinateRequirement coordinateRequirement, Class<T> subcomponentType) {
        final Stream<T> languageComponents = getSubcomponents(coordinateRequirement, subcomponentType);
        return StreamUtil.findOne(languageComponents);
    }


    @Override void close();
}
