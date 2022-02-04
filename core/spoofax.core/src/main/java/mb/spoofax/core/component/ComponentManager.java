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


    Option<? extends Component> getComponent(Coordinate coordinate);

    CollectionView<Component> getComponents(CoordinateRequirement coordinateRequirement);

    default Option<Component> getOneComponent(CoordinateRequirement coordinateRequirement) {
        final CollectionView<Component> components = getComponents(coordinateRequirement);
        if(components.size() == 1) {
            return Option.ofSome(components.iterator().next());
        } else {
            return Option.ofNone();
        }
    }


    Option<StandaloneComponent<?, ?, ?>> getStandaloneComponent(Coordinate coordinate);

    MapView<Coordinate, ? extends StandaloneComponent<?, ?, ?>> getStandaloneComponents();

    Option<GroupedComponents<?, ?, ?>> getGroupedComponents(String group);

    MapView<String, ? extends GroupedComponents<?, ?, ?>> getGroupedComponents();


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


    @Override void close();
}
