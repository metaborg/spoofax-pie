package mb.spoofax.compiler.spoofaxcore;

import org.immutables.value.Value;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface DependencyVersions {
    class Builder extends ImmutableDependencyVersions.Builder {}

    static Builder builder() {
        return new Builder();
    }


    @Value.Default default String spoofaxPie() {
        return "develop-SNAPSHOT";
    }

    @Value.Default default String spoofaxLegacy() {
        return "2.6.0-SNAPSHOT";
    }

    @Value.Default default String checkerFramework() {
        return "2.6.0";
    }
}
