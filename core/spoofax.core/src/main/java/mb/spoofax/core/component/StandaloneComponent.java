package mb.spoofax.core.component;

import mb.common.util.MapView;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StandaloneComponent implements Component {
    public final Coordinate coordinate;
    final Participant participant;
    public final ResourceServiceComponent resourceServiceComponent;
    public final @Nullable LanguageComponent languageComponent;
    public final PieComponent pieComponent;

    public StandaloneComponent(
        Participant participant,
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

    @Override public ResourceServiceComponent getResourceServiceComponent() {
        return resourceServiceComponent;
    }

    @Override public @Nullable LanguageComponent getLanguageComponent(Coordinate coordinate) {
        if(participant.getCoordinates().equals(coordinate)) {
            return languageComponent;
        }
        return null;
    }

    @Override public MapView<Coordinate, LanguageComponent> getLanguageComponents() {
        if(languageComponent != null) return MapView.of(coordinate, languageComponent);
        return MapView.of();
    }

    @Override public PieComponent getPieComponent() {
        return pieComponent;
    }

    @Override public String toString() {
        return "StandaloneComponent{" +
            "coordinate=" + coordinate +
            ", participant=" + participant +
            ", resourceServiceComponent=" + resourceServiceComponent +
            ", languageComponent=" + languageComponent +
            ", pieComponent=" + pieComponent +
            '}';
    }
}
