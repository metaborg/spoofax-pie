package mb.spoofax.core.component;

import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class GroupedComponents implements Component {
    final ListView<Participant> participants;
    public final String group;
    public final ResourceServiceComponent resourceServiceComponent;
    public final MapView<Coordinate, LanguageComponent> languageComponents;
    public final PieComponent pieComponent;

    public GroupedComponents(
        ListView<Participant> participants,
        String group,
        ResourceServiceComponent resourceServiceComponent,
        MapView<Coordinate, LanguageComponent> languageComponents,
        PieComponent pieComponent
    ) {
        this.participants = participants;
        this.group = group;
        this.resourceServiceComponent = resourceServiceComponent;
        this.languageComponents = languageComponents;
        this.pieComponent = pieComponent;
    }

    public void close() {
        pieComponent.close();
        languageComponents.values().forEach(LanguageComponent::close);
        resourceServiceComponent.close();
        participants.forEach(Participant::close);
    }

    @Override public ResourceServiceComponent getResourceServiceComponent() {
        return resourceServiceComponent;
    }

    @Override public @Nullable LanguageComponent getLanguageComponent(Coordinate coordinate) {
        return languageComponents.get(coordinate);
    }

    @Override public MapView<Coordinate, LanguageComponent> getLanguageComponents() {
        return languageComponents;
    }

    @Override public PieComponent getPieComponent() {
        return pieComponent;
    }

    @Override public String toString() {
        return "GroupedComponents{" +
            "participants=" + participants +
            ", group='" + group + '\'' +
            ", resourceServiceComponent=" + resourceServiceComponent +
            ", languageComponents=" + languageComponents +
            ", pieComponent=" + pieComponent +
            '}';
    }
}
