package mb.spoofax.compiler.spoofaxcore;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.util.GradleConfiguredDependency;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

@Value.Enclosing
public class StrategoRuntimeAdapterCompiler implements TaskDef<StrategoRuntimeAdapterCompiler.Input, StrategoRuntimeAdapterCompiler.Output> {
    @Inject public StrategoRuntimeAdapterCompiler() {}


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Output exec(ExecContext context, Input input) throws IOException {
        // Nothing to generate for adapter project at the moment.
        return Output.builder().build();
    }


    public ListView<GradleConfiguredDependency> getDependencies(Input input) {
        return ListView.of(
            GradleConfiguredDependency.api(input.languageProjectInput().shared().strategoPieDep())
        );
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends StrategoRuntimeAdapterCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Automatically provided sub-inputs

        StrategoRuntimeLanguageCompiler.Input languageProjectInput();
    }

    @Value.Immutable public interface Output extends Serializable {
        class Builder extends StrategoRuntimeAdapterCompilerData.Output.Builder {}

        static Builder builder() { return new Builder(); }
    }
}
