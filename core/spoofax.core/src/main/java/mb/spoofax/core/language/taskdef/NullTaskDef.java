package mb.spoofax.core.language.taskdef;

import mb.common.style.Styling;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * A task that always returns null.
 *
 * @param <I> the type of input of the task
 * @param <O> the type of output of the task
 */
@LanguageScope
public abstract class NullTaskDef<I extends Serializable, O extends @Nullable Serializable> implements TaskDef<I, @Nullable O> {
    protected NullTaskDef() {}

    @Override public String getId() {
        return getClass().getName();
    }

    @SuppressWarnings("NullableProblems") @Override
    public @Nullable O exec(ExecContext context, I input) throws Exception {
        return null;
    }
}
