package mb.sdf3;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@Sdf3Scope
public class Sdf3SpecConfigFunctionWrapper {
    private Function<ResourcePath, Result<Option<Sdf3SpecConfig>, ?>> function;

    @Inject public Sdf3SpecConfigFunctionWrapper() {
        function = DefaultConfigFunction.instance;
    }

    public Function<ResourcePath, Result<Option<Sdf3SpecConfig>, ?>> get() {
        return function;
    }

    public void set(Function<ResourcePath, Result<Option<Sdf3SpecConfig>, ?>> function) {
        this.function = function;
    }

    public void resetToDefault() {
        set(DefaultConfigFunction.instance);
    }

    private static class DefaultConfigFunction implements Function<ResourcePath, Result<Option<Sdf3SpecConfig>, ?>> {
        private static final DefaultConfigFunction instance = new DefaultConfigFunction();

        private DefaultConfigFunction() {}

        @Override public Result<Option<Sdf3SpecConfig>, ?> apply(ExecContext context, ResourcePath root) {
            return Result.ofOk(Option.ofSome(Sdf3SpecConfig.createDefault(root)));
        }

        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public int hashCode() { return 0; }

        private Object readResolve() { return instance; }
    }
}
