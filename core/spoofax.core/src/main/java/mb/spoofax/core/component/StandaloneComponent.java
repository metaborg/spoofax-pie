package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.common.util.MapView;
import mb.log.dagger.LoggerComponent;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StandaloneComponent<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> implements Component {
    public final Coordinate coordinate;
    private final Participant<L, R, P> participant;
    public final ResourceServiceComponent resourceServiceComponent;
    public final @Nullable LanguageComponent languageComponent;
    public final PieComponent pieComponent;

    public StandaloneComponent(
        Participant<L, R, P> participant,
        ResourceServiceComponent resourceServiceComponent,
        @Nullable LanguageComponent languageComponent,
        PieComponent pieComponent
    ) {
        this.coordinate = participant.getCoordinates();
        this.participant = participant;
        this.resourceServiceComponent = resourceServiceComponent;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
    }

    public void close() {
        pieComponent.close();
        resourceServiceComponent.close();
        if(languageComponent != null) languageComponent.close();
        participant.close();
    }

    @Override
    public ResourceServiceComponent getResourceServiceComponent() {
        return resourceServiceComponent;
    }


    @Override
    public Option<LanguageComponent> getLanguageComponent(Coordinate coordinate) {
        if(this.coordinate.equals(coordinate)) {
            return Option.ofNullable(languageComponent);
        }
        return Option.ofNone();
    }

    @Override
    public CollectionView<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement) {
        if(languageComponent != null && coordinateRequirement.matches(this.coordinate)) {
            return CollectionView.of(languageComponent);
        }
        return CollectionView.of();
    }

    @Override
    public MapView<Coordinate, LanguageComponent> getLanguageComponents() {
        if(languageComponent != null) return MapView.of(coordinate, languageComponent);
        return MapView.of();
    }


    @Override
    public PieComponent getPieComponent() {
        return pieComponent;
    }


    @Override
    public String toString() {
        return "StandaloneComponent{" +
            "coordinate=" + coordinate +
            ", participant=" + participant +
            ", resourceServiceComponent=" + resourceServiceComponent +
            ", languageComponent=" + languageComponent +
            ", pieComponent=" + pieComponent +
            '}';
    }
}
