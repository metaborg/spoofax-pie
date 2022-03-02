package mb.spoofax.core.component;

import mb.common.option.Option;
import mb.common.util.MapView;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ComponentImpl implements Component {
    public final Coordinate coordinate;
    private final boolean partOfGroup;
    private ResourceServiceComponent resourceServiceComponent;
    private @Nullable LanguageComponent languageComponent;
    private PieComponent pieComponent;
    private MapView<Class<?>, Object> subcomponents;
    private StartedParticipant startedParticipant;
    private boolean closed = false;

    public ComponentImpl(
        Coordinate coordinate,
        boolean partOfGroup,
        ResourceServiceComponent resourceServiceComponent,
        @Nullable LanguageComponent languageComponent,
        PieComponent pieComponent,
        MapView<Class<?>, Object> subcomponents,
        StartedParticipant startedParticipant
    ) {
        this.coordinate = coordinate;
        this.partOfGroup = partOfGroup;
        this.resourceServiceComponent = resourceServiceComponent;
        this.languageComponent = languageComponent;
        this.pieComponent = pieComponent;
        this.subcomponents = subcomponents;
        this.startedParticipant = startedParticipant;
    }

    public void started(StaticComponentManager staticComponentManager, ComponentManager componentManager) {
        startedParticipant.started(resourceServiceComponent, pieComponent, staticComponentManager, componentManager);
    }

    public void close() {
        if(closed) return;
        startedParticipant.close();
        startedParticipant = null;
        subcomponents = null;
        if(!partOfGroup) pieComponent.close();
        pieComponent = null;
        if(languageComponent != null) {
            languageComponent.close();
            languageComponent = null;
        }
        if(!partOfGroup) resourceServiceComponent.close();
        resourceServiceComponent = null;
        closed = true;
    }


    @Override
    public Coordinate getCoordinate() {
        return coordinate;
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
            ", resourceServiceComponent=" + resourceServiceComponent +
            ", languageComponent=" + languageComponent +
            ", pieComponent=" + pieComponent +
            ", subcomponents=" + subcomponents +
            ", participantCloseable=" + startedParticipant +
            ", closed=" + closed +
            '}';
    }
}
