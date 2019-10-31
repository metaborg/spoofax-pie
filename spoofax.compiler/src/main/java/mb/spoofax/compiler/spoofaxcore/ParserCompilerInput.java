package mb.spoofax.compiler.spoofaxcore;

import mb.spoofax.compiler.util.BuilderBase;
import mb.spoofax.compiler.util.ClassKind;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Properties;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface ParserCompilerInput {
    class Builder extends ImmutableParserCompilerInput.Builder implements BuilderBase {
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


    Shared shared();

    JavaProject languageProject();


    @Value.Default default ClassKind classKind() {
        return ClassKind.Generated;
    }

    Optional<String> manualParserClass();

    Optional<String> manualParserFactoryClass();


    @Value.Default default String genTableClass() {
        return shared().classSuffix() + "ParseTable";
    }

    @Value.Derived default String genTablePath() {
        return genTableClass() + ".java";
    }

    @Value.Default default String genTableResourcePath() {
        return languageProject().packagePath() + "/target/metaborg/sdf.tbl";
    }

    @Value.Default default String genParserClass() {
        return shared().classSuffix() + "Parser";
    }

    @Value.Derived default String genParserPath() {
        return genParserClass() + ".java";
    }

    @Value.Default default String genParserFactoryClass() {
        return shared().classSuffix() + "ParserFactory";
    }

    @Value.Derived default String genParserFactoryPath() {
        return genParserFactoryClass() + ".java";
    }


    default void savePersistentProperties(Properties properties) {
        shared().savePersistentProperties(properties);
        properties.setProperty("genTableClass", genTableClass());
        properties.setProperty("genParserClass", genParserClass());
        properties.setProperty("genParserFactoryClass", genParserFactoryClass());
    }

    @Value.Check
    default void check() {
        final ClassKind kind = classKind();
        final boolean manual = kind.isManual();
        if(!manual) return;
        if(!manualParserClass().isPresent()) {
            throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParserClass' has not been set");
        }
        if(!manualParserFactoryClass().isPresent()) {
            throw new IllegalArgumentException("Kind '" + kind + "' indicates that a manual class will be used, but 'manualParserFactoryClass' has not been set");
        }
    }
}
