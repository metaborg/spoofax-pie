package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.spoofax.core.Coordinate;

public interface ComponentDependencyResolver {
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
}
