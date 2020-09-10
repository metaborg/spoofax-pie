package mb.spoofax.compiler.spoofax2.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.language.MultilangAnalyzerLanguageCompiler;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;

@Value.Enclosing
public class Spoofax2MultilangAnalyzerLanguageCompiler implements TaskDef<Spoofax2MultilangAnalyzerLanguageCompiler.Input, None> {
    @Inject public Spoofax2MultilangAnalyzerLanguageCompiler() {}


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        // TODO: copy resources from Spoofax 2 project (currently done in Gradle)
        return None.instance;
    }


    public ListView<String> getCopyResources(Input input) {
        // TODO: requires constraint analyzer compiler with Statix enabled, which already copies Statix specs?
        return ListView.of();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax2MultilangAnalyzerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        default void syncTo(MultilangAnalyzerLanguageCompiler.Input.Builder builder) {
            // Nothing to sync right now.
        }
    }
}
