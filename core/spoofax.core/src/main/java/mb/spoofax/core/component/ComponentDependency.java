package mb.spoofax.core.component;

import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ComponentDependency {
    public final CoordinateRequirement coordinateRequirement;
    public final @Nullable Class<LanguageComponent> languageComponentClass;

    public ComponentDependency(CoordinateRequirement coordinateRequirement, @Nullable Class<LanguageComponent> languageComponentClass) {
        this.coordinateRequirement = coordinateRequirement;
        this.languageComponentClass = languageComponentClass;
    }

    @Override public String toString() {
        return "ComponentDependency{" +
            "coordinateRequirement=" + coordinateRequirement +
            ", languageComponentClass=" + languageComponentClass +
            '}';
    }
}
