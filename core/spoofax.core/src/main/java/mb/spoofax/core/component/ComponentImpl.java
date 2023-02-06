package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.MapView;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.resource.ResourcesComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ComponentImpl implements Component {
    public final Coordinate coordinate;
    private final boolean partOfGroup;
    private @Nullable ResourcesComponent resourcesComponent;
    private ResourceServiceComponent resourceServiceComponent;
    private @Nullable LanguageComponent languageComponent;
    private PieComponent pieComponent;
    private MapView<Class<?>, Object> subcomponents;
    private @Nullable StartedParticipantFactory startedParticipantFactory;
    private StartedParticipant startedParticipant;
    private boolean closed = false;

    public ComponentImpl(
        Coordinate coordinate,
        boolean partOfGroup,
        @Nullable ResourcesComponent resourcesComponent,
        ResourceServiceComponent resourceServiceComponent,
        @Nullable LanguageComponent languageComponent,
        PieComponent pieComponent,
        MapView<Class<?>, Object> subcomponents,
        @Nullable StartedParticipantFactory startedParticipantFactory,
        StartedParticipant startedParticipant
    ) {
        this.coordinate = coordinate;
        this.partOfGroup = partOfGroup;
        this.resourcesComponent = resourcesComponent;
        this.resourceServiceComponent = resourceServiceComponent;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
        this.subcomponents = subcomponents;
        this.startedParticipantFactory = startedParticipantFactory;
        this.startedParticipant = startedParticipant;
    }

    public void started(StaticComponentManager staticComponentManager, ComponentManager componentManager) {
        startedParticipant.started(resourceServiceComponent, pieComponent, staticComponentManager, componentManager);
    }

    public void close() {
        if(closed) return;
        startedParticipant.close();
        startedParticipant = null;
        if(startedParticipantFactory != null) {
            startedParticipantFactory.close();
            startedParticipantFactory = null;
        }
        subcomponents = null;
        if(!partOfGroup) pieComponent.close();
        pieComponent = null;
        if(languageComponent != null) {
            languageComponent.close();
            languageComponent = null;
        }
        if(!partOfGroup) resourceServiceComponent.close();
        resourceServiceComponent = null;
        if(resourcesComponent != null) {
            resourcesComponent.close();
            resourcesComponent = null;
        }
        closed = true;
    }


    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public Option<ResourcesComponent> getResourcesComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get resources component, component '" + coordinate + "' has been closed");
        return Option.ofNullable(resourcesComponent);
    }

    @Override
    public ResourceServiceComponent getResourceServiceComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get resource service component, component '" + coordinate + "' has been closed");
        return resourceServiceComponent;
    }

    @Override
    public Option<LanguageComponent> getLanguageComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get language component, component '" + coordinate + "' has been closed");
        return Option.ofNullable(languageComponent);
    }

    @Override
    public PieComponent getPieComponent() {
        if(closed)
            throw new IllegalStateException("Cannot get PIE component, component '" + coordinate + "' has been closed");
        return pieComponent;
    }

    @Override public <T> Option<T> getSubcomponent(Class<T> subcomponentType) {
        if(closed)
            throw new IllegalStateException("Cannot get subcomponent, component '" + coordinate + "' has been closed");
        final @Nullable Object subcomponent = subcomponents.get(subcomponentType);
        if(subcomponent != null && subcomponentType.isAssignableFrom(subcomponent.getClass())) {
            @SuppressWarnings("unchecked") final T typed = (T)subcomponent;
            return Option.ofSome(typed);
        }
        return Option.ofNone();
    }


    @Override public String toString() {
        return "ComponentImpl{" +
            "coordinate=" + coordinate +
            ", partOfGroup=" + partOfGroup +
            ", resourcesComponent=" + resourcesComponent +
            ", resourceServiceComponent=" + resourceServiceComponent +
            ", languageComponent=" + languageComponent +
            ", pieComponent=" + pieComponent +
            ", subcomponents=" + subcomponents +
            ", startedParticipantFactory=" + startedParticipantFactory +
            ", startedParticipant=" + startedParticipant +
            ", closed=" + closed +
            '}';
    }
}
