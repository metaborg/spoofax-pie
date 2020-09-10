package mb.spoofax.compiler.spoofax2.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;

@Value.Enclosing
public class Spoofax2ParserLanguageCompiler implements TaskDef<Spoofax2ParserLanguageCompiler.Input, None> {
    @Inject public Spoofax2ParserLanguageCompiler() {}


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        // TODO: copy resources from Spoofax 2 project (currently done in Gradle)
        return None.instance;
    }


    public ListView<String> getCopyResources(Input input) {
        return ListView.of(input.parseTableRelativePath());
    }


    @Value.Immutable
    public interface Input extends Serializable {
        class Builder extends Spoofax2ParserLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /**
         * @return path to the parse table to copy, relative to the Spoofax 2 language specification project directory.
         */
        @Value.Default default String parseTableRelativePath() {
            return "target/metaborg/sdf.tbl";
        }


        default void syncTo(ParserLanguageCompiler.Input.Builder builder) {
            builder.parseTableRelativePath(parseTableRelativePath());
        }
    }
}
