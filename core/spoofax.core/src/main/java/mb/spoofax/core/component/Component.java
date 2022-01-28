package mb.spoofax.core.component;

import mb.common.util.MapView;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Component {
    ResourceServiceComponent getResourceServiceComponent();

    @Nullable LanguageComponent getLanguageComponent(Coordinate coordinate);

    MapView<Coordinate, LanguageComponent> getLanguageComponents();

    PieComponent getPieComponent();
}
