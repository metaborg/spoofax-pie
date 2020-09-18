package mb.spoofax.core.language.taskdef;

import mb.completions.common.CompletionResult;
import mb.pie.api.Supplier;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;

public class NullCompleteTaskDef extends NullTaskDef<NullCompleteTaskDef.Input, @Nullable CompletionResult> {
    public static class Input implements Serializable {
        public final Supplier<@Nullable ?> astProvider;

        public Input(Supplier<?> astProvider) {
            this.astProvider = astProvider;
        }
    }

    @Inject public NullCompleteTaskDef() {}
}
