package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.log.dagger.LoggerComponent;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;

public class GroupedComponents<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> implements Component {
    final ListView<Participant<L, R, P>> participants;
    public final String group;
    public final ResourceServiceComponent resourceServiceComponent;
    public final MapView<Coordinate, LanguageComponent> languageComponents;
    public final PieComponent pieComponent;

    public GroupedComponents(
        ListView<Participant<L, R, P>> participants,
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


    @Override
    public ResourceServiceComponent getResourceServiceComponent() {
        return resourceServiceComponent;
    }


    @Override
    public Option<LanguageComponent> getLanguageComponent(Coordinate coordinate) {
        return Option.ofNullable(languageComponents.get(coordinate));
    }

    @Override
    public CollectionView<LanguageComponent> getLanguageComponents(CoordinateRequirement coordinateRequirement) {
        return CollectionView.of(languageComponents.values().stream().filter(l -> coordinateRequirement.matches(l.getLanguageInstance().getCoordinate())));
    }

    @Override
    public MapView<Coordinate, LanguageComponent> getLanguageComponents() {
        return languageComponents;
    }


    @Override
    public PieComponent getPieComponent() {
        return pieComponent;
    }


    @Override
    public String toString() {
        return "GroupedComponents{" +
            "participants=" + participants +
            ", group='" + group + '\'' +
            ", resourceServiceComponent=" + resourceServiceComponent +
            ", languageComponents=" + languageComponents +
            ", pieComponent=" + pieComponent +
            '}';
    }
}
