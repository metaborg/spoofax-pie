package mb.spoofax.compiler.spoofaxcore;

import org.immutables.value.Value;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface LanguageProjectInput {
    class Builder extends ImmutableLanguageProjectInput.Builder {}

    static Builder builder() {
        return new Builder();
    }


    Coordinates coordinates();

    DependencyVersions dependencyVersions();

    GradleDependency spoofaxCoreDependency();

    @Value.Default default boolean includeStrategoClasses() { return false; }

    @Value.Default default boolean includeStrategoJavastratClasses() { return false; }
}
