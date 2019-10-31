package mb.spoofax.compiler.spoofaxcore;

import mb.spoofax.compiler.util.BuilderBase;
import org.immutables.value.Value;

import java.util.Properties;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface AllCompilerInput {
    class Builder extends ImmutableAllCompilerInput.Builder implements BuilderBase {
        public Builder withPersistentProperties(Properties properties) {
            return this;
        }
    }

    static Builder builder() {
        return new Builder();
    }


    LanguageProjectCompilerInput languageProjectInput();

    ParserCompilerInput parserInput();
}
