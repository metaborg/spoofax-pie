package mb.spoofax.core.language.taskdef;

import mb.common.codecompletion.CodeCompletionResult;
import mb.completions.common.CompletionResult;
import mb.pie.api.Supplier;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

public class NullCodeCompleteTaskDef extends NullTaskDef<NullCodeCompleteTaskDef.Input, @Nullable CodeCompletionResult> {
    public static class Input implements Serializable {
        public final Supplier<@Nullable ?> astProvider;

        public Input(Supplier<?> astProvider) {
            this.astProvider = astProvider;
        }
    }

    @Inject public NullCodeCompleteTaskDef(@Named("packageId") String packageId) { super(packageId); }
}