package mb.spoofax.compiler.spoofax2.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;

@Value.Enclosing
public class Spoofax2StylerLanguageCompiler implements TaskDef<Spoofax2StylerLanguageCompiler.Input, None> {
    @Inject public Spoofax2StylerLanguageCompiler() {}


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        // TODO: copy resources from Spoofax 2 project (currently done in Gradle)
        return None.instance;
    }


    public ListView<String> getCopyResources(Input input) {
        return ListView.of(input.packedEsvRelativePath());
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends Spoofax2StylerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /**
         * @return path to the packed ESV to copy, relative to the Spoofax 2 language specification project directory.
         */
        @Value.Default default String packedEsvRelativePath() {
            return "target/metaborg/editor.esv.af";
        }


        default void syncTo(StylerLanguageCompiler.Input.Builder builder) {
            builder.packedEsvRelativePath(packedEsvRelativePath());
        }
    }
}
