package mb.spoofax.core.language.taskdef;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * A task that always returns {@code null}.
 *
 * @param <I> the type of input of the task
 * @param <O> the type of output of the task
 */
public abstract class NullTaskDef<I extends Serializable, O extends @Nullable Serializable> implements TaskDef<I, @Nullable O> {
    private final String idPrefix;

    protected NullTaskDef(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    @Override public String getId() {
        return idPrefix + "-" + getClass().getName();
    }

    @Override public @Nullable O exec(ExecContext context, I input) throws Exception {
        return null;
    }
}
