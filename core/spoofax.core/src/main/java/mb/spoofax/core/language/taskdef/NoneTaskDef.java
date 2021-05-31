package mb.spoofax.core.language.taskdef;

import mb.common.option.Option;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * A task that always returns {@code Option.ofNone()}.
 *
 * @param <I> the type of input of the task
 * @param <O> the type of option output of the task
 */
public abstract class NoneTaskDef<I extends Serializable, O extends Serializable> implements TaskDef<I, Option<O>> {
    private final String idPrefix;

    protected NoneTaskDef(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    @Override public String getId() {
        return idPrefix + "-" + getClass().getName();
    }

    @Override public @Nullable Option<O> exec(ExecContext context, I input) throws Exception {
        return Option.ofNone();
    }
}
