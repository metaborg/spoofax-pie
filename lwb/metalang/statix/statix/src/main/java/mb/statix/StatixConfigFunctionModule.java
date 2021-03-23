package mb.statix;

import dagger.Module;
import dagger.Provides;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

@Module
public class StatixConfigFunctionModule {
    private final Function<ResourcePath, Result<Option<StatixConfig>, ?>> function;

    public StatixConfigFunctionModule(Function<ResourcePath, Result<Option<StatixConfig>, ?>> function) {
        this.function = function;
    }

    public StatixConfigFunctionModule() {
        this(StatixDefaultConfigFunction.instance);
    }

    @Provides @StatixScope
    Function<ResourcePath, Result<Option<StatixConfig>, ?>> provideFunction() {
        return function;
    }

    private static class StatixDefaultConfigFunction implements Function<ResourcePath, Result<Option<StatixConfig>, ?>> {
        private static final StatixDefaultConfigFunction instance = new StatixDefaultConfigFunction();

        private StatixDefaultConfigFunction() {}

        @Override public Result<Option<StatixConfig>, ?> apply(ExecContext context, ResourcePath root) {
            return Result.ofOk(Option.ofSome(new StatixConfig(root)));
        }

        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() { return 0; }

        private Object readResolve() { return instance; }
    }
}
