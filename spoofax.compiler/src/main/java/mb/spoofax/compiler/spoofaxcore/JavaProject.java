package mb.spoofax.compiler.spoofaxcore;

import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.util.Conversion;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface JavaProject extends Serializable {
    class Builder extends ImmutableJavaProject.Builder {}

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
