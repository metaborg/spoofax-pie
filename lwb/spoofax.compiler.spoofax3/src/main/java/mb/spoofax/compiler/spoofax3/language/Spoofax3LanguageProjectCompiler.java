package mb.spoofax.compiler.spoofax3.language;

import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.Serializable;

@Value.Enclosing
public class Spoofax3LanguageProjectCompiler implements TaskDef<Spoofax3LanguageProjectCompiler.Input, None> {
    @Inject public Spoofax3LanguageProjectCompiler() {}


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public None exec(ExecContext context, Input input) throws Exception {
        return None.instance;
    }


    @Value.Immutable public interface Input extends Serializable {
        class Builder extends Spoofax3LanguageProjectCompilerData.Input.Builder {}

        static Builder builder() { return new Input.Builder(); }
    }
}
