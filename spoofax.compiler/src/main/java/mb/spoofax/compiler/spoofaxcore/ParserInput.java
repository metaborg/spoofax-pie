package mb.spoofax.compiler.spoofaxcore;

import mb.spoofax.compiler.util.BuilderBase;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Properties;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface ParserInput {
    BasicInput basicInput();


    @Value.Default default ClassKind kind() {
        return ClassKind.Generated;
    }

    Optional<String> manualParserClass();

    Optional<String> manualParserFactoryClass();


    @Value.Default default String genTableClass() {
        return basicInput().classSuffix() + "ParseTable";
    }

    @Value.Default default String genTableResourcePath() {
        return basicInput().packagePath() + "/target/metaborg/sdf.tbl";
    }

    @Value.Default default String genParserClass() {
        return basicInput().classSuffix() + "Parser";
    }

    @Value.Default default String genParserFactoryClass() {
        return basicInput().classSuffix() + "ParserFactory";
    }


    default void savePersistentProperties(Properties properties) {
        properties.setProperty("genTableClass", genTableClass());
        properties.setProperty("genParserClass", genParserClass());
        properties.setProperty("genParserFactoryClass", genParserFactoryClass());
    }

    class Builder extends ImmutableParserInput.Builder implements BuilderBase {
        public Builder withPersistentProperties(Properties properties) {
            with(properties, "genTableClass", this::genTableClass);
            with(properties, "genParserClass", this::genParserClass);
            with(properties, "genParserFactoryClass", this::genParserFactoryClass);
            return this;
        }
    }

    static Builder builder() {
        return new Builder();
    }

    @Value.Check
    default void check() {
        final ClassKind kind = kind();
        final boolean manual = kind == ClassKind.Manual || kind == ClassKind.Extended;
        if(!manual) return;
        if(!manualParserClass().isPresent()) {
            throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParserClass' has not been set");
        }
        if(!manualParserFactoryClass().isPresent()) {
            throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParserFactoryClass' has not been set");
        }
    }
}
