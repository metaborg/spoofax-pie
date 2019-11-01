package mb.spoofax.compiler.spoofaxcore;

import org.immutables.value.Value;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface LanguageProjectCompilerInput {
    class Builder extends ImmutableLanguageProjectCompilerInput.Builder {}

    static Builder builder() {
        return new Builder();
    }


    Shared shared();

    @Value.Default default JavaProject project() {
        final Shared shared = shared();
        final String artifactId = shared.defaultArtifactId();
        return JavaProject.builder()
            .coordinate(shared.defaultGroupId(), artifactId, shared.defaultVersion())
            .packageId(shared.basePackageId())
            .directory(shared.baseDirectory().appendSegment(artifactId))
            .build();
    }

    JavaDependency languageSpecificationDependency();

    @Value.Default default boolean includeStrategoClasses() { return false; }

    @Value.Default default boolean includeStrategoJavaStrategyClasses() { return false; }
}
