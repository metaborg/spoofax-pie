package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.Conversion;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(deepImmutablesDetection = true, visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
public interface JavaProject {
    class Builder extends ImmutableJavaProject.Builder implements BuilderBase {}

    static Builder builder() {
        return new Builder();
    }


    Coordinate coordinate();

    String packageId();

    @Value.Derived default String packagePath() {
        return Conversion.packageIdToPath(packageId());
    }

    ResourcePath directory();
}
