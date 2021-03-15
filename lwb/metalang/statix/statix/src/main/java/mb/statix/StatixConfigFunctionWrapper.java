package mb.statix;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@StatixScope
public class StatixConfigFunctionWrapper {
    private Function<ResourcePath, Result<Option<StatixConfig>, ?>> function;

    @Inject public StatixConfigFunctionWrapper() {
        function = DefaultConfigFunction.instance;
    }

    public Function<ResourcePath, Result<Option<StatixConfig>, ?>> get() {
        return function;
    }

    public void set(Function<ResourcePath, Result<Option<StatixConfig>, ?>> function) {
        this.function = function;
    }

    public void resetToDefault() {
        set(DefaultConfigFunction.instance);
    }

    private static class DefaultConfigFunction implements Function<ResourcePath, Result<Option<StatixConfig>, ?>> {
        private static final DefaultConfigFunction instance = new DefaultConfigFunction();

        private DefaultConfigFunction() {}

        @Override public Result<Option<StatixConfig>, ?> apply(ExecContext context, ResourcePath root) {
            return Result.ofOk(Option.ofSome(StatixConfig.createDefault(root)));
        }

        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() { return 0; }

        private Object readResolve() { return instance; }
    }
}
