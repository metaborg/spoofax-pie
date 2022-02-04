package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.common.util.MapView;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;

public interface Component {
    ResourceServiceComponent getResourceServiceComponent();

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

    MapView<Coordinate, LanguageComponent> getLanguageComponents();

    PieComponent getPieComponent();
}
