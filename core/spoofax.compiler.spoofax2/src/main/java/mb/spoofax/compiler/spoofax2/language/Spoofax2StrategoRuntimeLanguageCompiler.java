package mb.spoofax.compiler.spoofax2.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

@Value.Enclosing
public class Spoofax2StrategoRuntimeLanguageCompiler implements TaskDef<Spoofax2StrategoRuntimeLanguageCompiler.Input, None> {
    @Inject public Spoofax2StrategoRuntimeLanguageCompiler() {}


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        // TODO: copy resources from Spoofax 2 project (currently done in Gradle)
        return None.instance;
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public ListView<String> getCopyResources(Input input) {
        final ArrayList<String> copyResources = new ArrayList<>();
        if(input.copyCtree()) {
            copyResources.add(input.ctreeRelativePath());
        }
        return new ListView<>(copyResources);
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends Spoofax2StrategoRuntimeLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Whether to copy certain files from the Spoofax 2 project.

        @Value.Default default boolean copyCtree() { return false; }

        @Value.Default default String ctreeRelativePath() { return "target/metaborg/stratego.ctree"; }

        @Value.Default default boolean copyClasses() { return true; }


        default void syncTo(StrategoRuntimeLanguageCompiler.Input.Builder builder) {
            if(copyCtree()) {
                builder.addCtreeRelativePaths(ctreeRelativePath());
            }
        }
    }
}
