package mb.spoofax.compiler.spoofaxcore;

import mb.spoofax.compiler.util.BuilderBase;
import org.immutables.value.Value;

@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE, overshadowImplementation = true)
@Value.Immutable
public interface AllCompilerInput {
    class Builder extends ImmutableAllCompilerInput.Builder implements BuilderBase {}

    static Builder builder() {
        return new Builder();
    }


    LanguageProjectCompilerInput languageProjectInput();

    ParserCompilerInput parserInput();
}
