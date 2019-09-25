package mb.spoofax.generator.spoofaxcore;

import mb.spoofax.generator.util.BuilderBase;
import mb.spoofax.generator.util.Conversion;
import org.immutables.value.Value;

import java.util.Properties;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface BasicInput {
    String groupId();

    String id();

    String packageId();

    @Value.Derived default String packagePath() {
        return Conversion.packageIdToPath(packageId());
    }

    String name();


    @Value.Default default String classSuffix() {
        return name();
    }


    default void savePersistentProperties(Properties properties) {
        properties.setProperty("classSuffix", classSuffix());
    }

    class Builder extends ImmutableBasicInput.Builder implements BuilderBase {
        public Builder withPersistentProperties(Properties properties) {
            with(properties, "classSuffix", this::classSuffix);
            return this;
        }
    }

    static Builder builder() {
        return new Builder();
    }
}
