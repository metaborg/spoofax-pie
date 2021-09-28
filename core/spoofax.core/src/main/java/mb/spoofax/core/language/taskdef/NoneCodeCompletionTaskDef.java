package mb.spoofax.core.language.taskdef;

import mb.common.codecompletion.CodeCompletionResult;
import mb.pie.api.Supplier;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

public class NoneCodeCompletionTaskDef extends NoneTaskDef<NoneCodeCompletionTaskDef.Input, CodeCompletionResult> {
    public static class Input implements Serializable {
        public final Supplier<@Nullable ?> astProvider;

        public Input(Supplier<?> astProvider) {
            this.astProvider = astProvider;
        }
    }

    @Inject public NoneCodeCompletionTaskDef(@Named("packageId") String packageId) { super(packageId); }
}
