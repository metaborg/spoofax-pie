package mb.spoofax.generator.spoofaxcore;

import mb.spoofax.generator.util.BuilderBase;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Properties;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface ParserInput {
    BasicInput basicInput();


    @Value.Default default GenerationKind genKind() {
        return GenerationKind.Generated;
    }

    Optional<String> manualClass();


    @Value.Default default String tableGenClass() {
        return basicInput().classSuffix() + "ParseTable";
    }

    @Value.Default default String tableResourcePath() {
        return basicInput().packagePath() + "/target/metaborg/sdf.tbl";
    }


    @Value.Default default String parserGenClass() {
        return basicInput().classSuffix() + "Parser";
    }


    default void savePersistentProperties(Properties properties) {
        properties.setProperty("tableGenClass", tableGenClass());
        properties.setProperty("parserGenClass", parserGenClass());
    }

    class Builder extends ImmutableParserInput.Builder implements BuilderBase {
        public Builder withPersistentProperties(Properties properties) {
            with(properties, "tableGenClass", this::tableGenClass);
            with(properties, "parserGenClass", this::parserGenClass);
            return this;
        }
    }

    static Builder builder() {
        return new Builder();
    }
}
