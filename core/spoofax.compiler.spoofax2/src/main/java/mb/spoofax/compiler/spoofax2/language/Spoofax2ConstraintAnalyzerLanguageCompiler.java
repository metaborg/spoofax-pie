package mb.spoofax.compiler.spoofax2.language;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;

@Value.Enclosing
public class Spoofax2ConstraintAnalyzerLanguageCompiler implements TaskDef<Spoofax2ConstraintAnalyzerLanguageCompiler.Input, None> {
    @Inject public Spoofax2ConstraintAnalyzerLanguageCompiler() {}


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        // TODO: copy resources from Spoofax 2 project (currently done in Gradle)
        return None.instance;
    }


    public ListView<String> getCopyResources(Input input) {
        if(input.copyStatix()) {
            return ListView.of(input.statixSpecificationRelativePath());
        }
        return ListView.of();
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax2ConstraintAnalyzerLanguageCompilerData.Input.Builder {}

        static Builder builder() { return new Builder(); }


        /// Whether to copy certain files from the Spoofax 2 project.

        @Value.Default default boolean copyStatix() { return false; }

        @Value.Default default String statixSpecificationRelativePath() { return "src-gen/statix/"; }


        default void setEnableStatix(ConstraintAnalyzerLanguageCompiler.Input.Builder builder) {
            builder.enableStatix(copyStatix());
        }
    }
}
