package mb.spoofax.generator;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface SpoofaxCoreGeneratorInput extends Serializable {
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


    /// Parse Table

    @Value.Default default String parseTableGeneratedClass() {
        return classSuffix() + "ParseTable";
    }

    Optional<String> parseTableCustomClass();

    @Value.Default default String parseTableResourcePath() {
        return packagePath() + "/target/metaborg/sdf.tbl";
    }


    /// Parser

    @Value.Default default String parserGeneratedClass() {
        return classSuffix() + "Parser";
    }

    Optional<String> parserCustomClass();


    /// Styling Rules

    @Value.Default default String stylingRulesGeneratedClass() {
        return classSuffix() + "StylingRules";
    }

    Optional<String> stylingRulesCustomClass();

    @Value.Default default String stylingRulesResourcePath() {
        return packagePath() + "/target/metaborg/editor.esv.af";
    }


    /// Styler

    @Value.Default default String stylerGeneratedClass() {
        return classSuffix() + "Styler";
    }

    Optional<String> stylerCustomClass();


    /// Stratego Runtime Builder


    default Properties savePersistentProperties() {
        final Properties properties = new Properties();
        properties.setProperty("classSuffix", classSuffix());
        properties.setProperty("parseTableGeneratedClass", parseTableGeneratedClass());
        return properties;
    }

    class Builder extends ImmutableSpoofaxCoreGeneratorInput.Builder {
        public Builder withPersistentProperties(Properties properties) {
            with(properties, "classSuffix", this::classSuffix);
            with(properties, "parseTableGeneratedClass", this::parseTableGeneratedClass);
            return this;
        }

        private void with(Properties properties, String id, Consumer<String> func) {
            final @Nullable String value = properties.getProperty(id);
            if(value != null) {
                func.accept(value);
            }
        }
    }

    static Builder builder() {
        return new Builder();
    }
}
