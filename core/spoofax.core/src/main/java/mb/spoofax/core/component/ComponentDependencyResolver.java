package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.common.util.StreamUtil;
import mb.spoofax.core.Coordinate;

import java.util.stream.Stream;

public interface ComponentDependencyResolver {
    <T> Option<T> getSubcomponent(Coordinate coordinate, Class<T> subcomponentType);

    <T> Stream<T> getSubcomponents(Class<T> subcomponentType);

    default <T> Option<T> getOneSubcomponent(Class<T> subcomponentType) {
        return StreamUtil.findOne(getSubcomponents(subcomponentType));
    }
}
